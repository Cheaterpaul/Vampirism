package de.teamlapen.vampirism.common.world.entity.ai.system;

import com.mojang.datafixers.util.Pair;
import de.teamlapen.vampirism.common.world.entity.ai.activities.ActivityBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Encapsulates specific AI behaviors, sensors, and memories for an entity.
 */
public abstract class AiActivityProvider<E extends LivingEntity> {

    protected final List<ActivityBuilder<E>> builders = new ArrayList<>();
    protected final Set<SensorType<? extends Sensor<? super E>>> sensors = new HashSet<>();
    protected final Set<MemoryModuleType<?>> memories = new HashSet<>();
    protected final List<AiAction<E>> actions = new ArrayList<>();

    public List<AiAction<E>> getActions() {
        return actions;
    }

    protected void addAction(AiAction<E> action) {
        this.actions.add(action);
    }

    /**
     * Returns the sensors required specifically for this activity set.
     */
    public Set<SensorType<? extends Sensor<? super E>>> getSensors() {
        return sensors;
    }

    /**
     * Returns the memory modules required specifically for this activity set.
     */
    public Set<MemoryModuleType<?>> getMemoryModules() {
        Set<MemoryModuleType<?>> allMemories = new HashSet<>(memories);
        for (ActivityBuilder<E> builder : builders) {
            allMemories.addAll(builder.getRequirements().stream().map(Pair::getFirst).collect(Collectors.toSet()));
        }
        return allMemories;
    }

    /**
     * Registers the activities and behaviors into the entity's brain.
     */
    public void initActivity(Brain<E> brain) {
        builders.forEach(builder -> builder.register(brain));
    }

    /**
     * Optional: Returns activities that should be checked for activation.
     */
    public Stream<Activity> getActiveActivities() {
        return builders.stream().map(ActivityBuilder::getActivity);
    }

    protected ActivityBuilder<E> createActivity(Activity activity) {
        ActivityBuilder<E> builder = ActivityBuilder.create(activity);
        this.builders.add(builder);
        return builder;
    }

    protected ActivityBuilder<E> createActivity(Supplier<Activity> activitySupplier) {
        return createActivity(activitySupplier.get());
    }

    protected void addSensor(SensorType<? extends Sensor<? super E>> sensor) {
        this.sensors.add(sensor);
    }

    protected void addMemory(MemoryModuleType<?> memory) {
        this.memories.add(memory);
    }
}
