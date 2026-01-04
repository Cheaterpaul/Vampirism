package de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor;

import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.List;

public interface IDraculaAction {

    boolean canActivate(ServerLevel level, Dracula entity, Brain<Dracula> brain);

    default List<MemoryModuleType<?>> required() {
        return List.of(getActionMemory(), getCooldownMemory());
    }

    MemoryModuleType<Unit> getActionMemory();

    MemoryModuleType<Unit> getCooldownMemory();

}
