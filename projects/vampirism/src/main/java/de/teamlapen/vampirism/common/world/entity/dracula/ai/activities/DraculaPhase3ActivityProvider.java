package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import de.teamlapen.vampirism.common.core.ModActivities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.core.ModSensors;
import de.teamlapen.vampirism.common.world.entity.ai.activities.ActivityBuilder;
import de.teamlapen.vampirism.common.world.entity.ai.system.AiActivityProvider;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.BloodProjectilesBehavior;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.MistFormBehavior;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.RegenerationBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.Optional;
import java.util.Set;

public class DraculaPhase3ActivityProvider extends AiActivityProvider<Dracula> {

    public DraculaPhase3ActivityProvider() {
        super(ModActivities.DRACULA_PHASE_3);
    }

    @Override
    protected void createActivity(ActivityBuilder<Dracula> builder) {
        builder
                .add(StopAttackingIfTargetInvalid.create(), Set.of(), Set.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE))
                .add(MistFormBehavior.create(0.6f), Set.of(), Set.of(MemoryModuleType.WALK_TARGET, MemoryModuleType.ATTACK_TARGET))
                .add(SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F), Set.of(), Set.of(MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES))
                .add(StartAttacking.create(DraculaPhase3ActivityProvider::findNearestValidAttackTarget), Set.of(ModSensors.NEAREST_ENTITY.get()), Set.of(MemoryModuleType.ATTACK_TARGET,MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, ModMemoryTypes.NEAREST_VISIBLE_ATTACKABLE.get(), MemoryModuleType.ANGRY_AT))
                .add(MeleeAttack.create(15), Set.of(SensorType.NEAREST_LIVING_ENTITIES), Set.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES))
                .add(DraculaIdleActivityProvider.createIdleLookBehaviors(), DraculaIdleActivityProvider.lookSensors(), DraculaIdleActivityProvider.lookMemories())
                .add(DraculaIdleActivityProvider.createIdleMovementBehaviors(0.4f), DraculaIdleActivityProvider.movementSensors(), DraculaIdleActivityProvider.movementMemories())
                .requires(ModMemoryTypes.Dracula.PHASE_3, MemoryStatus.VALUE_PRESENT);

        var actions = builder.useActions();

        actions.addAction(ModActivities.DRACULA_REGENERATION, action -> action
                .activeMemory(ModMemoryTypes.Dracula.REGENERATION_ACTIVE)
                .cooldown(ModMemoryTypes.Dracula.REGENERATION_COOLDOWN, () -> 60 * 20)
                .addLast(RegenerationBehavior.create(), RegenerationBehavior.sensors(), RegenerationBehavior.memories())
                .canActivate((level, dracula) -> {
                    float v = (dracula.getHealth() / dracula.getMaxHealth());
                    float gate = 1 - RegenerationBehavior.HEALTH_PERCENTAGE;
                    if (v >= gate) {
                        return false;
                    }
                    return dracula.getRandom().nextFloat() < ((1 - v) / gate);
                }));

        actions.addAction(ModActivities.DRACULA_BLOOD_PROJECTILES, action -> action
                .activeMemory(ModMemoryTypes.Dracula.BLOOD_PROJECTILES_ACTIVE)
                .cooldown(ModMemoryTypes.Dracula.BLOOD_PROJECTILES_COOLDOWN, () -> 30 * 20)
                .addLast(BloodProjectilesBehavior.create(), Set.of(), Set.of(ModMemoryTypes.Dracula.BLOOD_PROJECTILES_ACTIVE.get(), ModMemoryTypes.Dracula.BLOOD_PROJECTILES_COOLDOWN.get()))
                .canActivate((level, dracula) -> {
                    float healthPercent = dracula.getHealth() / dracula.getMaxHealth();
                    return healthPercent >= 0.4f && healthPercent <= 0.8f;
                }));
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(ServerLevel level, Dracula dracula) {
        Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(dracula, MemoryModuleType.ANGRY_AT);
        if (optional.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(level, dracula, optional.get())) {
            return optional;
        } else {
            return dracula.getBrain().getMemory(ModMemoryTypes.NEAREST_VISIBLE_ATTACKABLE.get());
        }
    }
}
