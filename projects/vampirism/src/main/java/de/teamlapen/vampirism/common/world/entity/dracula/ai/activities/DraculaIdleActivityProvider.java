package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import de.teamlapen.vampirism.common.world.entity.ai.system.AiActivityProvider;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Set;
import java.util.stream.Stream;

public class DraculaIdleActivityProvider extends AiActivityProvider<Dracula> {

    @Override
    public Set<SensorType<? extends Sensor<? super Dracula>>> getSensors() {
        return (Set<SensorType<? extends Sensor<? super Dracula>>>) IdleActivity.SENSORS;
    }

    @Override
    public Set<MemoryModuleType<?>> getMemoryModules() {
        return (Set<MemoryModuleType<?>>) IdleActivity.MEMORY_MODULES;
    }

    @Override
    public void initActivity(Brain<Dracula> brain) {
        IdleActivity.initActivity(brain);
    }

    @Override
    public Stream<Activity> getActiveActivities() {
        return IdleActivity.getActivities();
    }
}
