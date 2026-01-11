package de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors;

import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class NearbyKnockbackBehavior {

    public static BehaviorControl<Dracula> create(double radius, double strength) {
        return BehaviorBuilder.create(inst -> inst.group(
                inst.registered(MemoryModuleType.ATTACK_TARGET)
        ).apply(inst, (target) -> (level, dracula, gameTime) -> {
            if (gameTime % 20 == 0) {
                level.getEntitiesOfClass(LivingEntity.class, dracula.getBoundingBox().inflate(radius)).forEach(entity -> {
                    if (entity != dracula) {
                        entity.knockback(strength, entity.getX() - dracula.getX(), entity.getZ() - dracula.getZ());
                    }
                });
                return true;
            }
            return false;
        }));
    }
}
