package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import de.teamlapen.vampirism.common.core.ModActivities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.core.ModSensors;
import de.teamlapen.vampirism.common.world.entity.ai.activities.ActivityBuilder;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.RegenerationBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Phase3Activities extends IdleActivity {

    public static Set<SensorType<? extends Sensor<? super Dracula>>> SENSORS = Stream.of(
            IdleActivity.SENSORS.stream(),
            Stream.of(
                    ModSensors.NEAREST_ENTITY.get(),
                    ModSensors.DRACULA_RAGE_ACTION.get()
            )
    ).flatMap(x -> x).map(x -> (SensorType<? extends Sensor<? super Dracula>>)x).collect(Collectors.toUnmodifiableSet());

    public static Set<? extends MemoryModuleType<?>> MEMORY_MODULES = Stream.of(
            IdleActivity.MEMORY_MODULES.stream(),
            RegenerationBehavior.requires(),
            Stream.of(
                    ModMemoryTypes.Dracula.PHASE_3.get(),
                    MemoryModuleType.LOOK_TARGET,
                    MemoryModuleType.ATTACK_TARGET,
                    MemoryModuleType.ATTACK_COOLING_DOWN,
                    MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                    MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                    MemoryModuleType.ANGRY_AT,
                    ModMemoryTypes.NEAREST_VISIBLE_ATTACKABLE.get()
            )
    ).flatMap(x -> x).collect(Collectors.toUnmodifiableSet());

    public static void initActivity(Brain<Dracula> brain) {
        ActivityBuilder.<Dracula>create(ModActivities.DRACULA_PHASE_3)
                .add(StopAttackingIfTargetInvalid.create())
                .add(SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F))
                .add(StartAttacking.create(Phase3Activities::findNearestValidAttackTarget))
                .add(MeleeAttack.create(15))
                .add(createIdleLookBehaviors())
                .add(createIdleMovementBehaviors())
                .requires(ModMemoryTypes.Dracula.PHASE_3.get(), MemoryStatus.VALUE_PRESENT)
                .register(brain);
        initActions(brain);
    }

    public static void initActions(Brain<Dracula> brain) {
        IdleActivity.<Dracula>createAction(ModActivities.DRACULA_REGENERATION, ModMemoryTypes.Dracula.PHASE_3)
                .add(new RegenerationBehavior())
                .requires(ModMemoryTypes.Dracula.REGENERATION_ACTIVE.get(), MemoryStatus.VALUE_PRESENT)
                .requires(ModMemoryTypes.Dracula.REGENERATION_COOLDOWN.get(), MemoryStatus.VALUE_ABSENT)
                .register(brain);
    }

    public static Stream<Activity> getActivities() {
        return Stream.of(
                ModActivities.DRACULA_REGENERATION.get(),
                ModActivities.DRACULA_PHASE_3.get()
        );
    }

    protected static RunOne<Dracula> createIdleMovementBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(RandomStroll.stroll(0.4F), 1),
                        Pair.of(new DoNothing(10, 30), 1))
        );
    }

    protected static RunOne<LivingEntity> createIdleLookBehaviors() {
        return new RunOne<>(
                ImmutableList.<Pair<? extends BehaviorControl<? super LivingEntity>, Integer>>builder()
                        .addAll(createLookBehaviors())
                        .add(Pair.of(new DoNothing(10, 30), 1))
                        .build()
        );
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
