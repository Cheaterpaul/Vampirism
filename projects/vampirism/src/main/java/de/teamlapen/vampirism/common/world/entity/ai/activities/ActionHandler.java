package de.teamlapen.vampirism.common.world.entity.ai.activities;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.List;

public class ActionHandler<E extends LivingEntity> implements BehaviorControl<E> {

    private final List<ActionBuilder<E>> actions;
    private net.minecraft.world.entity.ai.behavior.Status status = net.minecraft.world.entity.ai.behavior.Status.STOPPED;

    public ActionHandler(List<ActionBuilder<E>> actions) {
        this.actions = actions;
    }

    @Override
    public net.minecraft.world.entity.ai.behavior.Status getStatus() {
        return this.status;
    }

    @Override
    public void tickOrStop(ServerLevel level, E entity, long gameTime) {
        this.doStop(level, entity, gameTime);
    }

    @Override
    public boolean tryStart(ServerLevel level, E entity, long gameTime) {
        Brain<?> brain = entity.getBrain();
        if (brain.hasMemoryValue(ModMemoryTypes.Dracula.ACTION_ACTIVE.get()) || brain.hasMemoryValue(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get())) {
            return false;
        }

        for (ActionBuilder<E> action : actions) {
            if (action.getRequirements().stream().allMatch(pair -> brain.checkMemory(pair.getFirst(), pair.getSecond()))) {
                if (action.getCanActivate() == null || action.getCanActivate().test(level, entity)) {
                    brain.setMemory(ModMemoryTypes.Dracula.ACTION_ACTIVE.get(), net.minecraft.util.Unit.INSTANCE);
                    action.getActionMemories().forEach(memory -> brain.setMemory((MemoryModuleType<net.minecraft.util.Unit>) memory, net.minecraft.util.Unit.INSTANCE));
                    if (action.getActivity() != null) {
                        brain.setActiveActivityIfPossible(action.getActivity());
                    }
                    this.status = net.minecraft.world.entity.ai.behavior.Status.RUNNING;
                    return true;
                }
            }
        }
        return false;
    }

    public void tick(ServerLevel level, E entity, long gameTime) {
    }

    public void doStop(ServerLevel level, E entity, long gameTime) {
        this.status = net.minecraft.world.entity.ai.behavior.Status.STOPPED;
    }

    @Override
    public String debugString() {
        return "ActionHandler";
    }
}
