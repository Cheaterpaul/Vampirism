package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import de.teamlapen.vampirism.common.world.entity.ai.system.AiActivityProvider;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class DraculaCoreActivityProvider extends AiActivityProvider<Dracula> {

    public DraculaCoreActivityProvider() {
        addSensor(SensorType.HURT_BY);
        addSensor(SensorType.NEAREST_LIVING_ENTITIES);

        addMemory(MemoryModuleType.LOOK_TARGET);
        addMemory(MemoryModuleType.WALK_TARGET);
        addMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        addMemory(MemoryModuleType.PATH);
        addMemory(MemoryModuleType.ATTACK_TARGET);
        addMemory(MemoryModuleType.HURT_BY);
        addMemory(MemoryModuleType.HURT_BY_ENTITY);

        createActivity(Activity.CORE)
                .add(new Swim<>(0.8f))
                .add(new LookAtTargetSink(45, 90))
                .add(new MoveToTargetSink());
    }
}
