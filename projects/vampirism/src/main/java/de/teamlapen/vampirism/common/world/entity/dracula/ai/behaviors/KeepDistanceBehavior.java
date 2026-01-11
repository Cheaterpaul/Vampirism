package de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors;

import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;

public class KeepDistanceBehavior {

    public static BehaviorControl<Dracula> create(float speed, int minDistance, int maxDistance) {
        return BehaviorBuilder.create(inst -> inst.group(
                inst.present(MemoryModuleType.ATTACK_TARGET),
                inst.registered(MemoryModuleType.WALK_TARGET)
        ).apply(inst, (target, walkTarget) -> (level, dracula, gameTime) -> {
            LivingEntity targetEntity = inst.get(target);
            double distSq = dracula.distanceToSqr(targetEntity);
            if (distSq < minDistance * minDistance) {
                // Too close, move away
                Vec3 away = dracula.position().subtract(targetEntity.position()).normalize().scale(maxDistance);
                walkTarget.set(new WalkTarget(dracula.position().add(away), speed, 0));
                return true;
            } else if (distSq > maxDistance * maxDistance) {
                // Too far, move closer
                walkTarget.set(new WalkTarget(targetEntity.position(), speed, minDistance));
                return true;
            }
            return false;
        }));
    }
}
