package de.teamlapen.vampirism.common.world.entity.ai.activities;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class ActionBuilder<E extends LivingEntity> {

    private final ActivityBuilder<E> parent;
    private Activity activity;
    private final Set<Pair<MemoryModuleType<?>, MemoryStatus>> requirements = new HashSet<>();
    private final List<BehaviorControl<? super E>> behaviors = new ArrayList<>();
    private final Set<SensorType<? extends Sensor<? super E>>> sensors = new HashSet<>();
    private final Set<MemoryModuleType<?>> memories = new HashSet<>();
    private final Set<MemoryModuleType<?>> actionMemories = new HashSet<>();
    private BiPredicate<ServerLevel, E> canActivate;
    private int startPriority = 10;

    public ActionBuilder(ActivityBuilder<E> parent, Activity activity) {
        this.parent = parent;
        this.activity = activity;
    }

    public ActionBuilder<E> addAction(Activity activity) {
        ActionBuilder<E> builder = new ActionBuilder<>(this.parent, activity);
        if (this.parent != null) {
            builder.addRequirements(this.parent.getRequirements());
            this.parent.getActionBuilders().add(builder);
        }
        return builder;
    }

    public ActionBuilder<E> addAction(Supplier<Activity> activity) {
        return addAction(activity.get());
    }

    public ActionBuilder<E> requires(MemoryModuleType<?> memory, MemoryStatus status) {
        this.requirements.add(Pair.of(memory, status));
        return this;
    }

    public <T> ActionBuilder<E> requires(Supplier<MemoryModuleType<T>> memory, MemoryStatus status) {
        return requires(memory.get(), status);
    }

    public ActionBuilder<E> addRequirements(Set<Pair<MemoryModuleType<?>, MemoryStatus>> requirements) {
        if (requirements != null) {
            this.requirements.addAll(requirements);
        }
        return this;
    }

    public ActionBuilder<E> add(BehaviorControl<? super E> control) {
        this.behaviors.add(control);
        if (control instanceof IInformativeBehavior informative) {
            //noinspection unchecked
            this.sensors.addAll((Set<? extends SensorType<? extends Sensor<? super E>>>) (Set<?>) informative.getSensors());
            this.memories.addAll(informative.getMemories());
        }
        return this;
    }

    public ActionBuilder<E> add(BehaviorControl<? super E> control, Set<SensorType<? extends Sensor<? super E>>> sensors, Set<MemoryModuleType<?>> memories) {
        this.behaviors.add(control);
        this.sensors.addAll(sensors);
        this.memories.addAll(memories);
        return this;
    }

    public ActionBuilder<E> canActivate(BiPredicate<ServerLevel, E> canActivate) {
        this.canActivate = canActivate;
        return this;
    }

    public ActionBuilder<E> actionMemory(MemoryModuleType<?> actionMemory) {
        this.actionMemories.add(actionMemory);
        return this;
    }

    public <T> ActionBuilder<E> actionMemory(Supplier<MemoryModuleType<T>> actionMemory) {
        return actionMemory(actionMemory.get());
    }

    public ActionBuilder<E> cooldownMemory(MemoryModuleType<?> cooldownMemory) {
        this.requirements.add(Pair.of(cooldownMemory, MemoryStatus.VALUE_ABSENT));
        return this;
    }

    public <T> ActionBuilder<E> cooldownMemory(Supplier<MemoryModuleType<T>> cooldownMemory) {
        return cooldownMemory(cooldownMemory.get());
    }

    public Activity getActivity() {
        return activity;
    }

    public Set<Pair<MemoryModuleType<?>, MemoryStatus>> getRequirements() {
        return requirements;
    }

    public Set<SensorType<? extends Sensor<? super E>>> getSensors() {
        return sensors;
    }

    public Set<MemoryModuleType<?>> getMemories() {
        return memories;
    }

    public Set<MemoryModuleType<?>> getActionMemories() {
        return actionMemories;
    }

    public BiPredicate<ServerLevel, E> getCanActivate() {
        return canActivate;
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
