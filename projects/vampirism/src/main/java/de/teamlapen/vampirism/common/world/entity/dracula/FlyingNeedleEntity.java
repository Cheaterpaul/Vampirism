package de.teamlapen.vampirism.common.world.entity.dracula;

import de.teamlapen.vampirism.common.core.ModEntities;
import de.teamlapen.vampirism.common.util.DamageHandler;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class FlyingNeedleEntity extends Projectile {

    private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(FlyingNeedleEntity.class, EntityDataSerializers.BOOLEAN);

    private int lifeTicks = 0;
    private static final int MAX_LIFE = 100;
    private float damage = 4.0f;

    public FlyingNeedleEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noPhysics = true;
    }

    public FlyingNeedleEntity(Level level, LivingEntity owner, float damage) {
        this(ModEntities.FLYING_NEEDLE.get(), level);
        this.setOwner(owner);
        this.damage = damage;
        this.setPos(owner.getX(), owner.getEyeY(), owner.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FLYING, false);
    }

    public void setFlying(boolean flying) {
        this.entityData.set(FLYING, flying);
    }

    public boolean isFlying() {
        return this.entityData.get(FLYING);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isFlying()) {
            this.lifeTicks++;
            if (this.lifeTicks > MAX_LIFE) {
                this.discard();
                return;
            }

            Vec3 movement = this.getDeltaMovement();
            Vec3 nextPos = this.position().add(movement);

            EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(this.level(), this, this.position(), nextPos, this.getBoundingBox().expandTowards(movement).inflate(1.0D), this::canHitEntity);
            if (entityHitResult != null) {
                onHit(entityHitResult);
            }

            this.setPos(nextPos);

            // Keep the rotation towards movement
            if (movement.lengthSqr() > 1.0E-7D) {
                this.setRot((float) (Math.atan2(movement.x, movement.z) * (180 / Math.PI)), (float) (Math.asin(movement.y / movement.length()) * (180 / Math.PI)));
            }
        } else {
            // While not flying, it's managed by the behavior or just stays still
            // But we might want it to follow the owner if not fired yet.
            if (this.getOwner() != null && this.getOwner().isAlive()) {
                // The behavior will update its position to orbit Dracula
            } else if (!this.level().isClientSide()) {
                this.discard();
            }
        }
    }

    public void shoot(LivingEntity target) {
        this.setFlying(true);
        Vec3 direction = target.position().add(0, target.getBbHeight() / 2, 0).subtract(this.position()).normalize();
        this.setDeltaMovement(direction.scale(1.5));
        this.setRot((float) (Math.atan2(direction.x, direction.z) * (180 / Math.PI)), (float) (Math.asin(direction.y) * (180 / Math.PI)));
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        Entity entity = pResult.getEntity();
        if (entity instanceof LivingEntity living && !this.level().isClientSide()) {
            if (living != this.getOwner()) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    DamageHandler.hurtVanilla(serverLevel, living, damageSources -> damageSources.mobAttack((LivingEntity) this.getOwner()), damage);
                    this.discard();
                }
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        return super.canHitEntity(pTarget) && pTarget != this.getOwner() && this.isFlying();
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.lifeTicks = input.getIntOr("LifeTicks", 0);
        this.damage = input.getFloatOr("Damage", 4.0f);
        this.setFlying(input.getBooleanOr("Flying", false));
    }

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("LifeTicks", this.lifeTicks);
        output.putFloat("Damage", this.damage);
        output.putBoolean("Flying", this.isFlying());
    }
}
