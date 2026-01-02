package de.teamlapen.vampirism.common.world.entity.dracula;

import de.teamlapen.vampirism.common.core.ModEntities;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Dracula extends Mob implements GeoAnimatable, IDraculaAnimations {

    public static final EntityDataAccessor<FightStage> FIGHT_STAGE = SynchedEntityData.defineId(Dracula.class, ModEntities.DRACULA_FIGHT_STAGE.get());

    public Dracula(EntityType<? extends Dracula> type, Level level) {
        super(type, level);
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    //<editor-fold desc="Data">

    public FightStage getFightStage() {
        return this.entityData.get(FIGHT_STAGE);
    }
    private void setFightStage(FightStage stage) {
        this.entityData.set(FIGHT_STAGE, stage);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FIGHT_STAGE, FightStage.NONE);
    }

    //</editor-fold>

    //<editor-fold desc="Attribute Values">

    private static double createMaxHealth(FightStage stage) {
        return switch (stage) {
            case PHASE_2 -> 100;
            case PHASE_3 -> 200;
            default -> 50;
        };
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private static double createMovementSpeed(FightStage stage) {
        return switch (stage) {
            case PHASE_3 -> 0.8d;
            default -> 0.7d;
        };
    }

    private static double createKnockbackResistance(FightStage stage) {
        return switch (stage) {
            default-> 1d;
        };
    }

    private static double createAttackKnockback(FightStage stage) {
        return switch (stage) {
            case PHASE_2 -> 0.5d;
            case PHASE_3 -> 2d;
            default -> 0;
        };
    }

    private static double createAttackDamage(FightStage stage) {
        return switch (stage) {
            case NONE, PHASE_1 -> 5d;
            case PHASE_2 -> 10d;
            case PHASE_3 -> 20d;
        };
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private static double createExplosionKnockbackResistance(FightStage stage) {
        return switch (stage) {
            case PHASE_3 -> 0.2d;
            default -> 1;
        };
    }

    @SuppressWarnings("SameReturnValue")
    private static double createArmor(FightStage stage) {
        return switch (stage) {
            case PHASE_2 -> 40;
            case PHASE_3 -> 40;
            default -> 40;
        };
    }

    private static double createArmorToughness(FightStage stage) {
        return switch (stage) {
            case PHASE_2 -> 15;
            case PHASE_3 -> 6;
            default -> 15;
        };
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes()
                .add(Attributes.MAX_HEALTH, createMaxHealth(FightStage.NONE))
                .add(Attributes.ATTACK_DAMAGE, createAttackDamage(FightStage.NONE))
                .add(Attributes.ATTACK_KNOCKBACK, createAttackKnockback(FightStage.NONE))
                .add(Attributes.KNOCKBACK_RESISTANCE, createKnockbackResistance(FightStage.NONE))
                .add(Attributes.MOVEMENT_SPEED, createMovementSpeed(FightStage.NONE))
                .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, createKnockbackResistance(FightStage.NONE))
                .add(Attributes.BURNING_TIME, 0.1f)
                .add(Attributes.ARMOR, createArmor(FightStage.NONE))
                .add(Attributes.ARMOR_TOUGHNESS, createArmorToughness(FightStage.NONE));
    }

    //</editor-fold>

    //<editor-fold desc="Animation">

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("Walk/Run/Idle", test -> {
            if (test.isMoving()) {
                return test.setAndContinue(isSprinting() ? DefaultAnimations.RUN : DefaultAnimations.WALK);
            }
            return test.setAndContinue(DefaultAnimations.IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    //</editor-fold>

    //<editor-fold desc="Serialization">

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setFightStage(input.read("fight_stage", FightStage.CODEC).orElse(FightStage.NONE));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("fight_stage", FightStage.CODEC, getFightStage());
    }

    //</editor-fold>
}
