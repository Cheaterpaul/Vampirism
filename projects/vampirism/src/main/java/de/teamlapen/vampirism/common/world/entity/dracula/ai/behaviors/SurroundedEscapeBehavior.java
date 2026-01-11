package de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SurroundedEscapeBehavior {

    public static BehaviorControl<Dracula> create(double radius, int thresholdTicks, int minAttackers) {
        return BehaviorBuilder.create(inst -> inst.group(
                inst.registered(ModMemoryTypes.Dracula.SURROUNDED_TIMER.get()),
                inst.registered(ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE.get()),
                inst.registered(ModMemoryTypes.Dracula.FLYING_SWORD_COOLDOWN.get()),
                inst.registered(ModMemoryTypes.Dracula.ACTION_ACTIVE.get()),
                inst.registered(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get()),
                inst.registered(MemoryModuleType.ATTACK_TARGET)
        ).apply(inst, (timer, swordActive, swordCooldown, actionActive, actionCooldown, attackTarget) -> (level, dracula, gameTime) -> {
            List<LivingEntity> attackers = level.getEntitiesOfClass(LivingEntity.class, dracula.getBoundingBox().inflate(radius), e -> e != dracula && e.isAlive());

            if (attackers.size() >= minAttackers) {
                int currentTimer = inst.tryGet(timer).orElse(0) + 1;
                if (currentTimer >= thresholdTicks) {
                    // Trigger escape
                    attackers.forEach(entity -> {
                        entity.knockback(1.5, entity.getX() - dracula.getX(), entity.getZ() - dracula.getZ());
                    });

                    // Teleport to safety
                    Vec3 avgAttackerPos = getAvgPos(attackers);
                    Vec3 safePos = DefaultRandomPos.getPosAway(dracula, 20, 7, avgAttackerPos);
                    if (safePos != null) {
                        dracula.teleportTo(safePos.x, safePos.y, safePos.z);
                    }

                    // Force Flying Sword Attack
                    timer.erase();
                    swordCooldown.erase();
                    actionCooldown.erase();
                    actionActive.erase();
                    swordActive.set(Unit.INSTANCE);

                    return true;
                } else {
                    timer.set(currentTimer);
                }
            } else {
                timer.erase();
            }
            return false;
        }));
    }

    private static Vec3 getAvgPos(List<LivingEntity> entities) {
        double x = 0;
        double y = 0;
        double z = 0;
        for (LivingEntity entity : entities) {
            x += entity.getX();
            y += entity.getY();
            z += entity.getZ();
        }
        return new Vec3(x / entities.size(), y / entities.size(), z / entities.size());
    }
}
