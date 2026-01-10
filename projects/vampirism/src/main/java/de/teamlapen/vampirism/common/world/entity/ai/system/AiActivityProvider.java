package de.teamlapen.vampirism.common.world.entity.ai.system;

import com.mojang.datafixers.util.Pair;
import de.teamlapen.vampirism.common.world.entity.ai.activities.ActionBuilder;
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
    protected final List<ActionBuilder<E>> actionBuilders = new ArrayList<>();

    public List<ActionBuilder<E>> getActionBuilders() {
        return actionBuilders;
    }

    public List<ActivityBuilder<E>> getBuilders() {
        return builders;
    }

    /**
     * Returns the sensors required specifically for this activity set.
     */
    public Set<SensorType<? extends Sensor<? super E>>> getSensors() {
        Set<SensorType<? extends Sensor<? super E>>> allSensors = new HashSet<>();
        for (ActivityBuilder<E> builder : builders) {
            allSensors.addAll(builder.getSensors());
            for (ActionBuilder<E> actionBuilder : builder.getActionBuilders()) {
                allSensors.addAll(actionBuilder.getSensors());
            }
        }
        for (ActionBuilder<E> builder : actionBuilders) {
            allSensors.addAll(builder.getSensors());
        }
        return allSensors;
    }

    /**
     * Returns the memory modules required specifically for this activity set.
     */
    public Set<MemoryModuleType<?>> getMemoryModules() {
        Set<MemoryModuleType<?>> allMemories = new HashSet<>();
        for (ActivityBuilder<E> builder : builders) {
            allMemories.addAll(builder.getMemories());
            for (ActionBuilder<E> actionBuilder : builder.getActionBuilders()) {
                allMemories.addAll(actionBuilder.getMemories());
                allMemories.addAll(actionBuilder.getRequirements().stream().map(Pair::getFirst).collect(Collectors.toSet()));
            }
        }
        for (ActionBuilder<E> builder : actionBuilders) {
            allMemories.addAll(builder.getMemories());
            allMemories.addAll(builder.getRequirements().stream().map(Pair::getFirst).collect(Collectors.toSet()));
        }
        return allMemories;
    }

    /**
     * Registers the activities and behaviors into the entity's brain.
     */
    public void initActivity(Brain<E> brain) {
        builders.forEach(builder -> {
            builder.register(brain);
            builder.getActionBuilders().forEach(actionBuilder -> actionBuilder.register(brain));
        });
        actionBuilders.forEach(builder -> builder.register(brain));
    }

    /**
     * Optional: Returns activities that should be checked for activation.
     */
    public Stream<Activity> getActiveActivities() {
        return Stream.concat(builders.stream().map(ActivityBuilder::getActivity), actionBuilders.stream().map(ActionBuilder::getActivity)).filter(Objects::nonNull);
    }

    protected ActivityBuilder<E> createActivity(Activity activity) {
        ActivityBuilder<E> builder = ActivityBuilder.create(activity);
        this.builders.add(builder);
        return builder;
    }

    protected ActivityBuilder<E> createActivity(Supplier<Activity> activitySupplier) {
        return createActivity(activitySupplier.get());
    }

    protected ActionBuilder<E> createAction(Activity activity) {
        ActionBuilder<E> builder = new ActionBuilder<>(null, activity);
        this.actionBuilders.add(builder);
        return builder;
    }

    protected ActionBuilder<E> createAction(Supplier<Activity> activitySupplier) {
        return createAction(activitySupplier.get());
    }
}
