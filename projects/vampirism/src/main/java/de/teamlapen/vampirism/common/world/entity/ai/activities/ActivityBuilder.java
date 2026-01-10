package de.teamlapen.vampirism.common.world.entity.ai.activities;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActivityBuilder<E extends LivingEntity> {

    private final Activity activity;
    private final Set<Pair<MemoryModuleType<?>, MemoryStatus>> requirements = new HashSet<>();
    private final List<BehaviorControl<? super E>> behaviors = new ArrayList<>();
    private final Set<SensorType<? extends Sensor<? super E>>> sensors = new HashSet<>();
    private final Set<MemoryModuleType<?>> memories = new HashSet<>();
    private final ActionsBuilder<E> actionBuilders = new ActionsBuilder<>();

    private int startPriority = 20;

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

    public ActionsBuilder<E> useActions() {
        this.memories.add(ModMemoryTypes.Dracula.ACTION_ACTIVE.get());
        this.memories.add(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get());
        return this.actionBuilders;
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

    private ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> buildBehaviors() {
        ImmutableList.Builder<Pair<Integer, ? extends BehaviorControl<? super E>>> builder = ImmutableList.builder();

        int priority = this.startPriority;

        if (!this.actionBuilders.actions().isEmpty()) {
            builder.add(Pair.of(priority++, new ActionBehavior<>(this.actionBuilders.actions())));
        }

        for (BehaviorControl<? super E> behavior : behaviors) {
            builder.add(Pair.of(priority++, behavior));
        }

        return builder.build();
    }

    //</editor-fold>

    public ActivityEntry<E> build() {
        return new ActivityEntry<>(this.activity,
                Stream.concat(this.actionBuilders.actions().stream().flatMap(x -> x.sensors().stream()), this.sensors.stream()).collect(Collectors.toSet()),
                Stream.concat(this.actionBuilders.actions().stream().flatMap(a -> a.memories().stream()), this.memories.stream()).collect(Collectors.toSet()),
                this.requirements,
                buildBehaviors(),
                this.actionBuilders.actions()
        );
    }

    public record ActivityEntry<E extends LivingEntity>(
            Activity activity,
            Set<SensorType<? extends Sensor<? super E>>> sensors,
            Set<MemoryModuleType<?>> memories,
            Set<Pair<MemoryModuleType<?>, MemoryStatus>> requirements,
            ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> behaviors,
            List<ActionBuilder.Action<E>> actions
    ) {
        public void register(Brain<E> brain) {
            brain.addActivityWithConditions(this.activity, this.behaviors, this.requirements);
            for (ActionBuilder.Action<E> actionBuilder : this.actions) {
                actionBuilder.register(brain, this.requirements);
            }
        }

        public Stream<Activity> activities() {
            return Stream.concat(this.actions.stream().map(ActionBuilder.Action::activity), Stream.of(this.activity));
        }
    }
}
