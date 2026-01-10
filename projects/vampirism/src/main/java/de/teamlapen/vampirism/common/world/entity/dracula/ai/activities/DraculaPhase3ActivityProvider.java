package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import de.teamlapen.vampirism.common.core.ModActivities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.core.ModSensors;
import de.teamlapen.vampirism.common.world.entity.ai.system.AiActivityProvider;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.RegenerationBehavior;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.rage.RegenerationAction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.Optional;

public class DraculaPhase3ActivityProvider extends AiActivityProvider<Dracula> {

    public DraculaPhase3ActivityProvider() {
        addSensor(ModSensors.NEAREST_ENTITY.get());
        addSensor(ModSensors.DRACULA_RAGE_ACTION.get());

        addMemory(ModMemoryTypes.Dracula.PHASE_3.get());
        addMemory(MemoryModuleType.LOOK_TARGET);
        addMemory(MemoryModuleType.ATTACK_TARGET);
        addMemory(MemoryModuleType.ATTACK_COOLING_DOWN);
        addMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        addMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        addMemory(MemoryModuleType.ANGRY_AT);
        addMemory(ModMemoryTypes.NEAREST_VISIBLE_ATTACKABLE.get());
        addMemory(ModMemoryTypes.Dracula.REGENERATION_ACTIVE.get());
        addMemory(ModMemoryTypes.Dracula.REGENERATION_COOLDOWN.get());

        addAction(new RegenerationAction());

        createActivity(ModActivities.DRACULA_PHASE_3)
                .add(StopAttackingIfTargetInvalid.create())
                .add(SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F))
                .add(StartAttacking.create(DraculaPhase3ActivityProvider::findNearestValidAttackTarget))
                .add(MeleeAttack.create(15))
                .add(DraculaIdleActivityProvider.createIdleLookBehaviors())
                .add(DraculaIdleActivityProvider.createIdleMovementBehaviors(0.4f))
                .requires(ModMemoryTypes.Dracula.PHASE_3, MemoryStatus.VALUE_PRESENT);

        createActivity(ModActivities.DRACULA_REGENERATION)
                .requires(ModMemoryTypes.Dracula.ACTION_ACTIVE.get(), MemoryStatus.VALUE_PRESENT)
                .requires(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get(), MemoryStatus.VALUE_ABSENT)
                .requires(ModMemoryTypes.Dracula.PHASE_3, MemoryStatus.VALUE_PRESENT)
                .requires(ModMemoryTypes.Dracula.REGENERATION_ACTIVE.get(), MemoryStatus.VALUE_PRESENT)
                .requires(ModMemoryTypes.Dracula.REGENERATION_COOLDOWN.get(), MemoryStatus.VALUE_ABSENT)
                .add(new RegenerationBehavior());
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
