package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import de.teamlapen.vampirism.common.world.entity.ai.system.AiActivityProvider;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class DraculaIdleActivityProvider extends AiActivityProvider<Dracula> {

    public DraculaIdleActivityProvider() {
        addSensor(SensorType.NEAREST_LIVING_ENTITIES);

        addMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        addMemory(MemoryModuleType.LOOK_TARGET);
        addMemory(MemoryModuleType.WALK_TARGET);

        createActivity(Activity.IDLE)
                .add(new RunOne<>(
                        ImmutableList.of(
                                Pair.of(createIdleLookBehaviors(), 2),
                                Pair.of(createIdleMovementBehaviors(0.2f), 1)
                        )));
    }

    public static RunOne<Dracula> createIdleMovementBehaviors(float speed) {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(RandomStroll.stroll(speed), 1),
                        Pair.of(new DoNothing(30, 60), 1))
        );
    }

    public static RunOne<Dracula> createIdleLookBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(SetEntityLookTarget.create(8.0F), 1),
                        Pair.of(new DoNothing(30, 60), 1)
                )
        );
    }
}
