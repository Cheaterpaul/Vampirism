package de.teamlapen.vampirism.common.world.entity.ai.activities;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.util.StreamUtil;
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
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ActivityBuilder<E extends LivingEntity> {

    private final Activity activity;
    private final Set<Pair<MemoryModuleType<?>, MemoryStatus>> requirements = new HashSet<>();
    private final List<BehaviorControl<? super E>> behaviors = new ArrayList<>();
    private final Set<SensorType<? extends Sensor<? super E>>> sensors = new HashSet<>();
    private final Set<MemoryModuleType<?>> memories = new HashSet<>();
    private int startPriority = 10;
    private final List<ActionBuilder<E>> actionBuilders = new ArrayList<>();
    private BehaviorControl<? super E> actionHandler;

    public ActivityBuilder(Activity activity) {
        this.activity = activity;
    }

    //<editor-fold desc="Requirements">

    public ActivityBuilder<E> requires(MemoryModuleType<?> memory, MemoryStatus status) {
        this.requirements.add(Pair.of(memory, status));
        this.memories.add(memory);
        return this;
    }

    public <T> ActivityBuilder<E> requires(Supplier<MemoryModuleType<T>> memory, MemoryStatus status) {
        return requires(memory.get(), status);
    }

    //</editor-fold>

    //<editor-fold desc="Priorities">

    public ActivityBuilder<E> startPriority(int priority) {
        this.startPriority = priority;
        return this;
    }

    //</editor-fold>

    //<editor-fold desc="Actions">

    public ActionBuilder<E> useActions() {
        this.actionHandler = new ActionHandler<>(this.actionBuilders);
        this.memories.add(ModMemoryTypes.Dracula.ACTION_ACTIVE.get());
        this.memories.add(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get());
        return new ActionBuilder<>(this, null);
    }

    //</editor-fold>

    //<editor-fold desc="Behaviors">

    public ActivityBuilder<E> add(BehaviorControl<? super E> control) {
        if (control instanceof IInformativeBehavior<?> informative) {
            //noinspection unchecked
            return this.add(control, (Set<SensorType<? extends Sensor<? super E>>>) (Set<?>) informative.getSensors(), informative.getMemories());
        }
        return this.add(control, Set.of(), Set.of());
    }

    public ActivityBuilder<E> add(BehaviorControl<? super E> control, Set<SensorType<? extends Sensor<? super E>>> sensors, Set<MemoryModuleType<?>> memories) {
        this.behaviors.add(control);
        this.sensors.addAll(sensors);
        this.memories.addAll(memories);
        return this;
    }

    public ActivityBuilder<E> add(BehaviorBuilder<E> builder) {
        this.behaviors.addAll(builder.getBehaviors());
        return this;
    }

    //</editor-fold>

    //<editor-fold desc="Getters">

    public Activity getActivity() {
        return activity;
    }

    public Stream<Activity> activities() {
        return StreamUtil.append(this.actionBuilders.stream().map(ActionBuilder::getActivity), this.activity);
    }

    public Set<MemoryModuleType<?>> getMemories() {
        Set<MemoryModuleType<?>> allMemories = new HashSet<>(memories);
        for (ActionBuilder<E> actionBuilder : actionBuilders) {
            allMemories.addAll(actionBuilder.getMemories());
        }
        return allMemories;
    }

    public Set<SensorType<? extends Sensor<? super E>>> getSensors() {
        Set<SensorType<? extends Sensor<? super E>>> allSensors = new HashSet<>(sensors);
        for (ActionBuilder<E> actionBuilder : actionBuilders) {
            allSensors.addAll(actionBuilder.getSensors());
        }
        return allSensors;
    }

    public List<ActionBuilder<E>> actionBuilder() {
        return actionBuilders;
    }

    public BehaviorControl<? super E> actionHandler() {
        return actionHandler;
    }

    //</editor-fold>

    //<editor-fold desc="Register">

    public void register(Brain<E> brain) {
        brain.addActivityWithConditions(this.activity, buildBehaviors(), this.requirements);
        for (ActionBuilder<E> actionBuilder : actionBuilders) {
            actionBuilder.register(brain);
        }
    }

    private ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> buildBehaviors() {
        ImmutableList.Builder<Pair<Integer, ? extends BehaviorControl<? super E>>> builder = ImmutableList.builder();

        int priority = this.startPriority;

        if (this.actionHandler != null) {
            builder.add(Pair.of(priority++, this.actionHandler));
        }

        for (BehaviorControl<? super E> behavior : behaviors) {
            builder.add(Pair.of(priority++, behavior));
        }

        return builder.build();
    }

    //</editor-fold>

}
