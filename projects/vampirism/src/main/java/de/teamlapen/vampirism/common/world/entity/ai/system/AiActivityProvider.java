package de.teamlapen.vampirism.common.world.entity.ai.system;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Encapsulates specific AI behaviors, sensors, and memories for an entity.
 */
public abstract class AiActivityProvider<E extends LivingEntity> {
    /**
     * Returns the sensors required specifically for this activity set.
     */
    public abstract Set<SensorType<? extends Sensor<? super E>>> getSensors();

    /**
     * Returns the memory modules required specifically for this activity set.
     */
    public abstract Set<MemoryModuleType<?>> getMemoryModules();

    /**
     * Registers the activities and behaviors into the entity's brain.
     */
    public abstract void initActivity(Brain<E> brain);

    /**
     * Optional: Returns activities that should be checked for activation.
     */
    public Stream<Activity> getActiveActivities() {
        return Stream.empty();
    }
}
