package de.teamlapen.vampirism.common.world.entity.ai.system;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import net.minecraft.world.entity.schedule.Activity;

import java.util.List;

public interface AiAction<E extends LivingEntity> {

    boolean canActivate(ServerLevel level, E entity, Brain<E> brain);

    default List<MemoryModuleType<?>> required() {
        return List.of(getActionMemory(), getCooldownMemory());
    }

    MemoryModuleType<Unit> getActionMemory();

    MemoryModuleType<Unit> getCooldownMemory();

    Activity getActivity();
}
