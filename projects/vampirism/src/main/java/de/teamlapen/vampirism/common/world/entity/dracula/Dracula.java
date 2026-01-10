package de.teamlapen.vampirism.common.world.entity.dracula;

import com.mojang.serialization.Dynamic;
import de.teamlapen.vampirism.common.core.ModAttachments;
import de.teamlapen.vampirism.common.core.ModEntities;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.DraculaAiSystem;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.DraculaState;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Dracula extends PathfinderMob implements GeoAnimatable, IDraculaAnimations {

    public static final EntityDataAccessor<DraculaState> FIGHT_STAGE = SynchedEntityData.defineId(Dracula.class, ModEntities.DRACULA_STATE.get());

    private long transformationStart;

    public Dracula(EntityType<? extends Dracula> type, Level level) {
        super(type, level);
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    public FightStage getStage() {
        return getState().stage;
    }

    @Override
    public void tick() {
        super.tick();
        tickTransformation();
    }

    @Override
    public boolean isInvulnerable() {
        return super.isInvulnerable() || this.isTransforming();
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource damageSource) {
        return super.isInvulnerableTo(level, damageSource) || (!damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && this.isTransforming());
    }

    protected void updateEvent() {
        if (this.level() instanceof ServerLevel serverLevel) {
            DraculaFightData data = serverLevel.getData(ModAttachments.DRACULA_FIGHT_DATA.get());
            data.getEvent().update(this);
        }
    }

    protected void addPlayerToEvent(ServerPlayer player) {
        if (this.level() instanceof ServerLevel serverLevel) {
            DraculaFightData data = serverLevel.getData(ModAttachments.DRACULA_FIGHT_DATA.get());
            data.getEvent().addPlayer(player);
        }
    }

    //<editor-fold desc="Data">

    private boolean isTransforming() {
        return this.getState().isTransforming;
    }

    public DraculaState getState() {
        return this.entityData.get(FIGHT_STAGE);
    }
    private void setState(DraculaState stage) {
        this.entityData.set(FIGHT_STAGE, stage);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FIGHT_STAGE, DraculaState.DEFAULT);
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
            case PHASE_3 -> 0.75d;
            default -> 0.7d;
        };
    }

    private static double createKnockbackResistance(FightStage stage) {
        //noinspection SwitchStatementWithTooFewBranches
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

    @SuppressWarnings("DataFlowIssue")
    private void updateAttributes(FightStage fightStage) {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(createMaxHealth(fightStage));
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(createAttackDamage(fightStage));
        this.getAttribute(Attributes.ATTACK_KNOCKBACK).setBaseValue(createAttackKnockback(fightStage));
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(createKnockbackResistance(fightStage));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(createMovementSpeed(fightStage));
        this.getAttribute(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE).setBaseValue(createExplosionKnockbackResistance(fightStage));
        this.getAttribute(Attributes.ARMOR).setBaseValue(createArmor(fightStage));
        this.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(createArmorToughness(fightStage));
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
            if (Dracula.this.getState().isTransforming) {
                return PlayState.STOP;
            }
            if (test.isMoving()) {
                return test.setAndContinue(isSprinting() ? DefaultAnimations.RUN : DefaultAnimations.WALK);
            }
            return test.setAndContinue(DefaultAnimations.IDLE);
        }));
        controllers.add(new AnimationController<>("Attack", test -> {
            DraculaState state = Dracula.this.getState();
            if (state.isTransforming) {
                return PlayState.STOP;
            }
            if (Dracula.this.swinging) {
                return switch (state.stage) {
                    case PHASE_3 -> {
                        var animation = test.controller().getCurrentRawAnimation();
                        if (animation == null) {
                            animation = Dracula.this.random.nextBoolean() ? IDraculaAnimations.PHASE_3_ATTACK_1 : IDraculaAnimations.PHASE_3_ATTACK_2;
                        }
                        yield test.setAndContinue(animation);
                    }
                    default -> PlayState.STOP;
                };
            }
            return PlayState.STOP;
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
        setState(input.read("fight_stage", DraculaState.CODEC).orElse(DraculaState.DEFAULT));
        this.transformationStart = input.getLongOr("transformation_start", -1);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("fight_stage", DraculaState.CODEC, getState());
        if (this.transformationStart != -1) {
            output.putLong("transformation_start", this.transformationStart);
        }
    }

    //</editor-fold>

    //<editor-fold desc="Brain">

    @SuppressWarnings("unchecked")
    @Override
    public Brain<Dracula> getBrain() {
        return (Brain<Dracula>) super.getBrain();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return DraculaAiSystem.AI.initializeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    protected Brain.Provider<Dracula> brainProvider() {
        return DraculaAiSystem.AI.brainProvider();
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (!this.isTransforming()) {
            this.getBrain().tick(level, this);
            DraculaAiSystem.AI.tick(level, this);
        }
    }

    @Override
    public void aiStep() {
        this.updateSwingTime();
        if (!this.isTransforming()) {
            super.aiStep();
        }
    }

    //</editor-fold>

    //<editor-fold desc="Transformation">

    protected void tickTransformation() {
        if (!this.isTransforming() || !(this.level() instanceof ServerLevel serverLevel)) return;


        var percentage = ((serverLevel.getGameTime() - this.transformationStart) / (float) getState().transformTime);


        setHealth(Math.max(1, getMaxHealth() * percentage));

        if (percentage >= 1) {
            finishTransformation();
        }
        updateEvent();
    }

    private void finishTransformation() {
        var stage = switch (this.getStage()) {
            case PHASE_2 -> DraculaState.RANGED;
            case PHASE_3 -> DraculaState.RAGED;
            default -> throw new IllegalStateException("Unexpected value: " + this.getStage());
        };
        this.setState(stage);
        this.transformationStart = -1;
    }

    private void startTransformation() {
        var nextStage = switch (this.getStage()) {
            case PHASE_1 -> DraculaState.TRANSFORMING_TO_RANGED;
            case PHASE_2 -> DraculaState.TRANSFORMING_TO_RAGED;
            default -> throw new IllegalStateException("Unexpected value: " + this.getStage());
        };

        this.transformationStart = this.level().getGameTime();
        this.setState(nextStage);
        updateAttributes(getStage());
        if (level() instanceof ServerLevel serverLevel) {
            DraculaAiSystem.AI.stop(serverLevel, this);
        }
        updateEvent();
    }

    @Override
    public void setHealth(float health) {
        if (this.getState() != DraculaState.RAGED && this.damageContainers != null && !this.damageContainers.empty()) {
            DamageContainer peek = this.damageContainers.peek();
            if (!peek.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) && health <= 0) {
                health = 1;
                startTransformation();
            }
        }

        super.setHealth(health);
    }

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource damageSource, float amount) {
        super.actuallyHurt(level, damageSource, amount);
        if (damageSource.getEntity() instanceof ServerPlayer player) {
            addPlayerToEvent(player);
        }
        if (getState() == DraculaState.DEFAULT) {
            setState(DraculaState.PASSIVE);
        }
        updateEvent();
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.getData(ModAttachments.DRACULA_FIGHT_DATA.get()).getEvent().clear();
        }
    }

    //</editor-fold>
}
