package de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.rage;

import de.teamlapen.vampirism.common.core.ModActivities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.RegenerationBehavior;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.IDraculaAction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;

public class RegenerationAction implements IDraculaAction {

    @Override
    public boolean canActivate(ServerLevel level, Dracula entity, Brain<Dracula> brain) {
        float v = (entity.getHealth() / entity.getMaxHealth());
        float gate = 1 - RegenerationBehavior.HEALTH_PERCENTAGE;
        if (v >= gate) {
            return false;
        }
        return entity.getRandom().nextFloat() < ((1-v) / gate);
    }

    @Override
    public MemoryModuleType<Unit> getActionMemory() {
        return ModMemoryTypes.Dracula.REGENERATION_ACTIVE.get();
    }

    @Override
    public MemoryModuleType<Unit> getCooldownMemory() {
        return ModMemoryTypes.Dracula.REGENERATION_COOLDOWN.get();
    }

    @Override
    public Activity getActivity() {
        return ModActivities.DRACULA_REGENERATION.get();
    }
}
