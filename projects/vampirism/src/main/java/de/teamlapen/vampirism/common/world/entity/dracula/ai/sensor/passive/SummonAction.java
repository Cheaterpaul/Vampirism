package de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.passive;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.SummonProtectorsBehavior;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.IDraculaAction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.List;

public class SummonAction implements IDraculaAction {

    @Override
    public boolean canActivate(ServerLevel level, Dracula entity, Brain<Dracula> brain) {
        return brain.getMemory(ModMemoryTypes.SUMMONS.get()).orElseGet(List::of).size()<SummonProtectorsBehavior.MAX_SUMMONS * 0.7;
    }

    @Override
    public List<MemoryModuleType<?>> required() {
        return List.of(getActionMemory(), getCooldownMemory(), ModMemoryTypes.SUMMONS.get());
    }

    @Override
    public MemoryModuleType<Unit> getActionMemory() {
        return ModMemoryTypes.Dracula.SUMMON_PROTECTOR_ACTIVE.get();
    }

    @Override
    public MemoryModuleType<Unit> getCooldownMemory() {
        return ModMemoryTypes.Dracula.SUMMON_PROTECTOR_COOLDOWN.get();
    }
}
