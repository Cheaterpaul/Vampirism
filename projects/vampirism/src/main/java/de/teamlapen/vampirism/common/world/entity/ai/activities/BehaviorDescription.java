package de.teamlapen.vampirism.common.world.entity.ai.activities;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.Set;

public record BehaviorDescription<E extends LivingEntity>(BehaviorControl<? super E> control, Set<SensorType<? extends Sensor<? super E>>> sensors, Set<MemoryModuleType<?>> memories) {

}
