package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.ai.activities.ActivityBuilder;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class IdleActivity {

    public static Set<SensorType<? extends Sensor<? super Dracula>>> SENSORS = Set.of(
            SensorType.NEAREST_LIVING_ENTITIES
    );
    public static List<? extends MemoryModuleType<?>> MEMORY_MODULES = List.of(
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET
    );

    public static void initActivity(Brain<Dracula> brain) {
        ActivityBuilder.<Dracula>create(Activity.IDLE)
                .add(new RunOne<>(
                        ImmutableList.of(
                                Pair.of(createIdleLookBehaviors(), 2),
                                Pair.of(createIdleMovementBehaviors(), 1)
                        ))).register(brain);
    }

    public static Stream<Activity> getActivities() {
        return Stream.of(
                Activity.IDLE
        );
    }

    protected static <E extends LivingEntity> ActivityBuilder<E> createAction(Activity activity, MemoryModuleType<?> phase) {
        return new ActivityBuilder<E>(activity)
                .requires(ModMemoryTypes.Dracula.ACTION_ACTIVE.get(), MemoryStatus.VALUE_PRESENT)
                .requires(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get(), MemoryStatus.VALUE_ABSENT)
                .requires(phase, MemoryStatus.VALUE_PRESENT);
    }

    protected static <E extends LivingEntity> ActivityBuilder<E> createAction(Supplier< ? extends Activity> activitySupplier, Supplier<? extends MemoryModuleType<?>> phase) {
        return createAction(activitySupplier.get(), phase.get());
    }

    protected static RunOne<Dracula> createIdleMovementBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(RandomStroll.stroll(0.2F), 1),
                        Pair.of(new DoNothing(30, 60), 1))
        );
    }

    protected static RunOne<LivingEntity> createIdleLookBehaviors() {
        return new RunOne<>(
                ImmutableList.<Pair<? extends BehaviorControl<? super LivingEntity>, Integer>>builder()
                        .addAll(createLookBehaviors())
                        .add(Pair.of(new DoNothing(30, 60), 1))
                        .build()
        );
    }

    protected static ImmutableList<Pair<OneShot<LivingEntity>, Integer>> createLookBehaviors() {
        return ImmutableList.of(
                Pair.of(SetEntityLookTarget.create(8.0F), 1)
        );
    }
}
