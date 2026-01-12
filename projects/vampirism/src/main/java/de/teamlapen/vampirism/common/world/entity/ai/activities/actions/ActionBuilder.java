package de.teamlapen.vampirism.common.world.entity.ai.activities.actions;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActionBuilder<E extends LivingEntity> {

    private final Activity activity;
    private final Supplier<Integer> cooldownSupplier;
    private final Set<Pair<MemoryModuleType<?>, MemoryStatus>> requirements = new HashSet<>();
    private final List<BehaviorControl<? super E>> behaviors = new ArrayList<>();
    @Nullable
    private BehaviorControl<? super E> lastBehavior;
    private final Set<SensorType<? extends Sensor<? super E>>> sensors = new HashSet<>();
    private final Set<MemoryModuleType<?>> memories = new HashSet<>();
    @UnknownNullability
    private MemoryModuleType<Unit> activeMemory;
    @UnknownNullability
    private Cooldown cooldown;
    private BiPredicate<ServerLevel, E> canActivate = (a,b) -> true;
    private int startPriority = 10;

    public ActionBuilder(Activity activity, Supplier<Integer> cooldownSupplier) {
        this.activity = activity;
        this.cooldownSupplier = cooldownSupplier;
    }

    //<editor-fold desc="Requirements">

    public ActionBuilder<E> requires(MemoryModuleType<?> memory, MemoryStatus status) {
        this.requirements.add(Pair.of(memory, status));
        this.memories.add(memory);
        return this;
    }

    public <T> ActionBuilder<E> requires(Supplier<MemoryModuleType<T>> memory, MemoryStatus status) {
        return requires(memory.get(), status);
    }

    //</editor-fold>

    //<editor-fold desc="Behaviors">

    public ActionBuilder<E> add(BehaviorControl<? super E> control) {
        return this.add(control, Set.of(), Set.of());
    }

    public ActionBuilder<E> add(BehaviorControl<? super E> control, Set<? extends SensorType<? extends Sensor<? super E>>> sensors, Set<MemoryModuleType<?>> memories) {
        this.behaviors.add(control);
        this.sensors.addAll(sensors);
        this.memories.addAll(memories);
        return this;
    }

    public ActionBuilder<E> addLast(BehaviorControl<? super E> control, Set<? extends SensorType<? extends Sensor<? super E>>> sensors, Set<MemoryModuleType<?>> memories) {
        this.lastBehavior = control;
        this.sensors.addAll(sensors);
        this.memories.addAll(memories);
        return this;
    }

    ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> buildBehaviors() {
        ImmutableList.Builder<Pair<Integer, ? extends BehaviorControl<? super E>>> builder = ImmutableList.builder();
        int priority = this.startPriority;
        for (BehaviorControl<? super E> behavior : this.behaviors) {
            builder.add(Pair.of(priority++, behavior));
        }
        return builder.build();
    }

    //</editor-fold>

    //<editor-fold desc="Preconditions">

    @SuppressWarnings("UnusedReturnValue")
    public ActionBuilder<E> canActivate(BiPredicate<ServerLevel, E> canActivate) {
        this.canActivate = canActivate;
        return this;
    }

    //</editor-fold>

    //<editor-fold desc="Memories">

    public ActionBuilder<E> activeMemory(MemoryModuleType<Unit> actionMemory) {
        this.activeMemory = actionMemory;
        this.memories.add(actionMemory);
        return this;
    }

    public ActionBuilder<E> activeMemory(Supplier<MemoryModuleType<Unit>> actionMemory) {
        return activeMemory(actionMemory.get());
    }

    public ActionBuilder<E> cooldown(MemoryModuleType<Unit> cooldownMemory, Supplier<Integer> cooldown) {
        this.cooldown = new Cooldown(cooldownMemory, cooldown);
        this.memories.add(cooldownMemory);
        return this;
    }

    public ActionBuilder<E> cooldown(Supplier<MemoryModuleType<Unit>> cooldownMemory, Supplier<Integer> cooldown) {
        return cooldown(cooldownMemory.get(), cooldown);
    }

    //</editor-fold>

    //<editor-fold desc="Priority">

    public ActionBuilder<E> priority(int priority) {
        this.startPriority = priority;
        return this;
    }

    //</editor-fold>

    Action<E> build() {
        Preconditions.checkNotNull(this.activeMemory, "No active memory defined");
        Preconditions.checkNotNull(this.cooldown, "No cooldown memory defined");
        Preconditions.checkNotNull(this.lastBehavior, "No last behavior defined");

        this.behaviors.add(new LastBehavior<>(this.lastBehavior, this.activeMemory, this.cooldown, this.cooldownSupplier));
        return new Action<>(activity, sensors, memories, activeMemory, cooldown, requirements, canActivate, buildBehaviors());
    }

    public record Action<E extends LivingEntity>(
            Activity activity,
            Collection<SensorType<? extends Sensor<? super E>>> sensors,
            Collection<MemoryModuleType<?>> memories,
            MemoryModuleType<Unit> activeMemory,
            Cooldown cooldownMemory,
            Set<Pair<MemoryModuleType<?>, MemoryStatus>> requirements,
            BiPredicate<ServerLevel, E> precondition,
            ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> behaviors
    ) {
        public void register(Brain<E> brain, Set<Pair<MemoryModuleType<?>, MemoryStatus>> requirements) {
            Stream<Stream<Pair<MemoryModuleType<?>, MemoryStatus>>> stream = Stream.of(requirements.stream(), this.requirements.stream(), Stream.of(Pair.of(activeMemory, MemoryStatus.VALUE_PRESENT), Pair.of(cooldownMemory.memory, MemoryStatus.VALUE_ABSENT)));
            brain.addActivityWithConditions(this.activity, this.behaviors, stream.flatMap(x -> x).collect(Collectors.toUnmodifiableSet()));
        }

    }

    record Cooldown(MemoryModuleType<Unit> memory, Supplier<Integer> cooldownSupplier) {
        public int cooldown() {
            return this.cooldownSupplier.get();
        }
    }

    private record LastBehavior<E extends LivingEntity>(BehaviorControl<E> original,
                                                        MemoryModuleType<Unit> activeMemory,
                                                        Cooldown cooldownMemory,
                                                        Supplier<Integer> cooldownSupplier) implements BehaviorControl<E> {

        @Override
            public Behavior.Status getStatus() {
                return this.original.getStatus();
            }

            @Override
            public boolean tryStart(ServerLevel level, E entity, long gameTime) {
                return this.original.tryStart(level, entity, gameTime);
            }

            @Override
            public void tickOrStop(ServerLevel level, E entity, long gameTime) {
                this.original.tickOrStop(level, entity, gameTime);
                if (this.original.getStatus() == Behavior.Status.STOPPED) {
                    stopAction(entity);
                }
            }

            @Override
            public void doStop(ServerLevel level, E entity, long gameTime) {
                this.original.doStop(level, entity, gameTime);
                stopAction(entity);
            }

            private void stopAction(E entity) {
                Brain<?> brain = entity.getBrain();
                brain.eraseMemory(activeMemory);
                brain.eraseMemory(ModMemoryTypes.ACTION_ACTIVE.get());
                brain.setMemoryWithExpiry(cooldownMemory.memory, Unit.INSTANCE, cooldownMemory.cooldown());
                brain.setMemoryWithExpiry(ModMemoryTypes.ACTION_COOLDOWN.get(), Unit.INSTANCE, cooldownSupplier.get());
            }

            @Override
            public String debugString() {
                return this.original.debugString();
            }
        }
}
