package de.teamlapen.vampirism.common.world.entity.ai.activities;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ActivityBuilder<E extends LivingEntity> {

    private final Activity activity;
    private final Set<Pair<MemoryModuleType<?>, MemoryStatus>> requirements = new HashSet<>();
    private final List<BehaviorControl<? super E>> behaviors = new ArrayList<>();
    private int startPriority = 10;

    public ActivityBuilder(Activity activity) {
        this.activity = activity;
    }

    public ActivityBuilder(Supplier<Activity> activitySupplier)
    {
        this(activitySupplier.get());
    }

    public static <E extends LivingEntity> ActivityBuilder<E> create(Activity activity) {
        return new ActivityBuilder<>(activity);
    }

    public static <E extends LivingEntity> ActivityBuilder<E> create(Supplier<Activity> activitySupplier) {
        return create(activitySupplier.get());
    }

    public ActivityBuilder<E> requires(MemoryModuleType<?> memory, MemoryStatus status) {
        this.requirements.add(Pair.of(memory, status));
        return this;
    }

    public <T> ActivityBuilder<E> requires(Supplier<MemoryModuleType<T>> memory, MemoryStatus status) {
        return requires(memory.get(), status);
    }

    public ActivityBuilder<E> startPriority(int priority) {
        this.startPriority = priority;
        return this;
    }

    public ActivityBuilder<E> addRequirements(Set<Pair<MemoryModuleType<?>, MemoryStatus>> requirements) {
        this.requirements.addAll(requirements);
        return this;
    }

    public <T extends BehaviorControl<? super E>> ActivityBuilder<E> add(T control) {
        this.behaviors.add(control);
        return this;
    }

    public ActivityBuilder<E> add(BehaviorBuilder<E> builder) {
        this.behaviors.addAll(builder.getBehaviors());
        return this;
    }

    public void register(Brain<E> brain) {
        brain.addActivityWithConditions(this.activity, buildBehaviors(), this.requirements);
    }

    private ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> buildBehaviors() {
        ImmutableList.Builder<Pair<Integer, ? extends BehaviorControl<? super E>>> builder = ImmutableList.builder();

        int priority = this.startPriority;

        for (BehaviorControl<? super E> behavior : behaviors) {
            builder.add(Pair.of(priority++, behavior));
        }

        return builder.build();
    }

}
