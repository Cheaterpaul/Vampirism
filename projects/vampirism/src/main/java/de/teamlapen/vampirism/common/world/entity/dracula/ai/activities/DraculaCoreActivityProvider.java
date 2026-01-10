package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import de.teamlapen.vampirism.common.world.entity.ai.system.AiActivityProvider;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.Set;

public class DraculaCoreActivityProvider extends AiActivityProvider<Dracula> {

    @Override
    public Set<SensorType<? extends Sensor<? super Dracula>>> getSensors() {
        return (Set<SensorType<? extends Sensor<? super Dracula>>>) CoreActivity.SENSORS;
    }

    @Override
    public Set<MemoryModuleType<?>> getMemoryModules() {
        return (Set<MemoryModuleType<?>>) CoreActivity.MEMORY_MODULES;
    }

    @Override
    public void initActivity(Brain<Dracula> brain) {
        CoreActivity.initActivity(brain);
    }
}
