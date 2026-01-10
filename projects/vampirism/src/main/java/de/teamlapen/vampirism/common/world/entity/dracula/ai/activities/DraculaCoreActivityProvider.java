package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import de.teamlapen.vampirism.common.world.entity.ai.activities.ActivityBuilder;
import de.teamlapen.vampirism.common.world.entity.ai.system.AiActivityProvider;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Set;

public class DraculaCoreActivityProvider extends AiActivityProvider<Dracula> {

    public DraculaCoreActivityProvider() {
        super(Activity.CORE);
    }

    @Override
    protected void createActivity(ActivityBuilder<Dracula> builder) {
        builder.startPriority(0)
                .add(new Swim<>(0.8f))
                .add(new LookAtTargetSink(45, 90), lookSensors(), lookMemories())
                .add(new MoveToTargetSink(), moveSensors(), moveMemories());
    }

    private Set<SensorType<? extends Sensor<? super Dracula>>> moveSensors() {
        return Set.of(
                SensorType.NEAREST_LIVING_ENTITIES,
                SensorType.HURT_BY
        );
    }

    private Set<MemoryModuleType<?>> moveMemories() {
        return Set.of(
                MemoryModuleType.LOOK_TARGET,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                MemoryModuleType.PATH,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.HURT_BY,
                MemoryModuleType.HURT_BY_ENTITY);
    }

    private Set<SensorType<? extends Sensor<? super Dracula>>> lookSensors() {
        return Set.of();
    }

    private Set<MemoryModuleType<?>> lookMemories() {
        return Set.of(MemoryModuleType.LOOK_TARGET);
    }

}
