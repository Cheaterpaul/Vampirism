package de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.dracula.BloodProjectileEntity;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BloodProjectilesBehavior extends Behavior<Dracula> {

    private int ticks = 0;
    private final List<BloodProjectileEntity> spawnedProjectiles = new ArrayList<>();

    public static BloodProjectilesBehavior create() {
        return new BloodProjectilesBehavior();
    }

    public BloodProjectilesBehavior() {
        super(Map.of(
                ModMemoryTypes.Dracula.BLOOD_PROJECTILES_COOLDOWN.get(), MemoryStatus.VALUE_ABSENT,
                ModMemoryTypes.Dracula.BLOOD_PROJECTILES_ACTIVE.get(), MemoryStatus.VALUE_PRESENT
        ), 120);
    }

    @Override
    protected void start(ServerLevel level, Dracula entity, long gameTime) {
        this.ticks = 0;
        this.spawnedProjectiles.clear();
        entity.hurtServer(level, level.damageSources().magic(), entity.getMaxHealth() * 0.05f);
        entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected void tick(ServerLevel level, Dracula entity, long gameTime) {
        this.ticks++;
        entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        entity.getNavigation().stop();

        if (ticks <= 100 && ticks % 10 == 0) {
            double angle = (ticks / 10.0) * (Math.PI * 2);
            double x = entity.getX() + Math.cos(angle) * 2;
            double z = entity.getZ() + Math.sin(angle) * 2;
            double y = entity.getY() + 1.5;

            BloodProjectileEntity projectile = new BloodProjectileEntity(level, entity, Vec3.ZERO);
            projectile.setPos(x, y, z);
            projectile.setNoGravity(true);
            projectile.setMaxTicks(200);
            level.addFreshEntity(projectile);
            spawnedProjectiles.add(projectile);
        }

        if (ticks == 100) {
            for (BloodProjectileEntity projectile : spawnedProjectiles) {
                if (projectile.isAlive()) {
                    Vec3 direction = projectile.position().subtract(entity.position().add(0, 1.5, 0)).normalize();
                    projectile.setDeltaMovement(direction.scale(0.5));
                    projectile.setMotionFactor(1.0f);
                }
            }
        }

        if (ticks > 115) {
            entity.getBrain().eraseMemory(ModMemoryTypes.Dracula.BLOOD_PROJECTILES_ACTIVE.get());
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Dracula entity, long gameTime) {
        return entity.getBrain().hasMemoryValue(ModMemoryTypes.Dracula.BLOOD_PROJECTILES_ACTIVE.get());
    }

    @Override
    protected void stop(ServerLevel level, Dracula entity, long gameTime) {
        entity.getBrain().setMemoryWithExpiry(ModMemoryTypes.Dracula.BLOOD_PROJECTILES_COOLDOWN.get(), Unit.INSTANCE, 400);
        entity.getBrain().eraseMemory(ModMemoryTypes.Dracula.BLOOD_PROJECTILES_ACTIVE.get());
        entity.getBrain().eraseMemory(ModMemoryTypes.Dracula.ACTION_ACTIVE.get());
        entity.getBrain().setMemoryWithExpiry(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get(), Unit.INSTANCE, 100);
        this.spawnedProjectiles.clear();
    }
}
