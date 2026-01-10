package de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.ranged;

import de.teamlapen.vampirism.common.core.ModActivities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.IDraculaAction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;

public class SummonVampiricBatsAction implements IDraculaAction {
    @Override
    public boolean canActivate(ServerLevel level, Dracula entity, Brain<Dracula> brain) {
        return entity.getHealth() < (entity.getMaxHealth() * 0.7);
    }

    @Override
    public MemoryModuleType<Unit> getActionMemory() {
        return ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_ACTIVE.get();
    }

    @Override
    public MemoryModuleType<Unit> getCooldownMemory() {
        return ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_COOLDOWN.get();
    }

    @Override
    public Activity getActivity() {
        return ModActivities.DRACULA_PHASE_2.get();
    }
}
