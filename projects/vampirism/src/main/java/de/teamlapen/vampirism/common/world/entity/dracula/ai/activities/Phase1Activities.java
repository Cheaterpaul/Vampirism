package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import de.teamlapen.vampirism.common.core.ModActivities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.core.ModSensors;
import de.teamlapen.vampirism.common.world.entity.ai.activities.ActivityBuilder;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.SummonProtectorsBehavior;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Phase1Activities extends IdleActivity {

    public static Set<SensorType<? extends Sensor<? super Dracula>>> SENSORS = Stream.of(
            IdleActivity.SENSORS.stream(),
            Stream.of(
                    ModSensors.DRACULA_PASSIVE_ACTION.get()
            )
    ).flatMap(x -> x).collect(Collectors.toUnmodifiableSet());

    public static Set<? extends MemoryModuleType<?>> MEMORY_MODULES = Stream.of(
            IdleActivity.MEMORY_MODULES.stream(),
            SummonProtectorsBehavior.requires(),
            Stream.of(
                    ModMemoryTypes.Dracula.PHASE_1.get()
            )
    ).flatMap(x -> x).collect(Collectors.toUnmodifiableSet());

    public static void initActivity(Brain<Dracula> brain) {
        ActivityBuilder.<Dracula>create(ModActivities.DRACULA_PHASE_1)
                .add(SummonProtectorsBehavior.create())
                .add(createIdleLookBehaviors())
                .add(createIdleMovementBehaviors())
                .requires(ModMemoryTypes.Dracula.PHASE_1, MemoryStatus.VALUE_PRESENT)
                .register(brain);
    }

    public static Stream<Activity> getActivities() {
        return Stream.of(

        );
    }
}
