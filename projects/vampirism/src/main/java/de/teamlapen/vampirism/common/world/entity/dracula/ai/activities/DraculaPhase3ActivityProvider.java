package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import de.teamlapen.vampirism.common.core.ModActivities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.ai.system.AiActivityProvider;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.RegenerationBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.Optional;

public class DraculaPhase3ActivityProvider extends AiActivityProvider<Dracula> {

    public DraculaPhase3ActivityProvider() {
        var activity = createActivity(ModActivities.DRACULA_PHASE_3)
                .add(StopAttackingIfTargetInvalid.create())
                .add(SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F))
                .add(StartAttacking.create(DraculaPhase3ActivityProvider::findNearestValidAttackTarget))
                .add(MeleeAttack.create(15))
                .add(DraculaIdleActivityProvider.createIdleLookBehaviors())
                .add(DraculaIdleActivityProvider.createIdleMovementBehaviors(0.4f))
                .requires(ModMemoryTypes.Dracula.PHASE_3, MemoryStatus.VALUE_PRESENT);

        activity.addAction(ModActivities.DRACULA_REGENERATION)
                .actionMemory(ModMemoryTypes.Dracula.REGENERATION_ACTIVE)
                .cooldownMemory(ModMemoryTypes.Dracula.REGENERATION_COOLDOWN)
                .add(new RegenerationBehavior())
                .canActivate((level, dracula) -> {
                    float v = (dracula.getHealth() / dracula.getMaxHealth());
                    float gate = 1 - RegenerationBehavior.HEALTH_PERCENTAGE;
                    if (v >= gate) {
                        return false;
                    }
                    return dracula.getRandom().nextFloat() < ((1 - v) / gate);
                });
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
