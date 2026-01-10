package de.teamlapen.vampirism.common.world.entity.ai.activities;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.Collections;
import java.util.Set;

/**
 * Interface for behaviors that can provide information about their required sensors and memories.
 */
public interface IInformativeBehavior {

    default Set<SensorType<? extends Sensor<?>>> getSensors() {
        return Collections.emptySet();
    }

    default Set<MemoryModuleType<?>> getMemories() {
        return Collections.emptySet();
    }
}
