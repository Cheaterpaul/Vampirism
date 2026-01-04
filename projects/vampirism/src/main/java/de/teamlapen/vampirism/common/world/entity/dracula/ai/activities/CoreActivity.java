package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;
import java.util.Set;

public class CoreActivity {

    public static Set<? extends SensorType<? extends Sensor<? super Dracula>>> SENSORS = Set.of(
            SensorType.HURT_BY,
            SensorType.NEAREST_LIVING_ENTITIES
    );

    public static List<? extends MemoryModuleType<?>> MEMORY_MODULES = List.of(
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY
    );

    public static void initActivity(Brain<Dracula> brain) {
        brain.addActivityWithConditions(Activity.CORE, createBehaviors(), Set.of());
    }

    private static ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super Dracula>>> createBehaviors() {
        return ImmutableList.of(
                Pair.of(0, new Swim<>(0.8f)),
                Pair.of(1, new LookAtTargetSink(45, 90)),
                Pair.of(2, createMoveToTargetSink()));
    }

    private static MoveToTargetSink createMoveToTargetSink() {
        return new MoveToTargetSink();
    }
}
