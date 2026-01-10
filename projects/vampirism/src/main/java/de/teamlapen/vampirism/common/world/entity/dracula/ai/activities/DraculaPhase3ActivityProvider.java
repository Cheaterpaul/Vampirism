package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import de.teamlapen.vampirism.common.core.ModActivities;
import de.teamlapen.vampirism.common.world.entity.ai.system.AiActivityProvider;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Set;
import java.util.stream.Stream;

public class DraculaPhase3ActivityProvider extends AiActivityProvider<Dracula> {

    @Override
    public Set<SensorType<? extends Sensor<? super Dracula>>> getSensors() {
        return (Set<SensorType<? extends Sensor<? super Dracula>>>) Phase3Activities.SENSORS;
    }

    @Override
    public Set<MemoryModuleType<?>> getMemoryModules() {
        return (Set<MemoryModuleType<?>>) Phase3Activities.MEMORY_MODULES;
    }

    @Override
    public void initActivity(Brain<Dracula> brain) {
        Phase3Activities.initActivity(brain);
    }

    @Override
    public Stream<Activity> getActiveActivities() {
        return Stream.concat(Phase3Activities.getActivities(), Stream.of(ModActivities.DRACULA_PHASE_3.get()));
    }
}
