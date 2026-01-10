package de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.ranged;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.IDraculaAction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class FlyingSwordAction implements IDraculaAction {
    @Override
    public boolean canActivate(ServerLevel level, Dracula entity, Brain<Dracula> brain) {
        return true; //entity.getHealth() < (entity.getMaxHealth() * 0.9);
    }

    @Override
    public MemoryModuleType<Unit> getActionMemory() {
        return ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE.get();
    }

    @Override
    public MemoryModuleType<Unit> getCooldownMemory() {
        return ModMemoryTypes.Dracula.FLYING_SWORD_COOLDOWN.get();
    }
}
