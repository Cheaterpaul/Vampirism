package de.teamlapen.vampirism.common.world.entity.ai.activities;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ActionHandler<E extends LivingEntity> implements BehaviorControl<E> {

    private final List<ActionBuilder<E>> actions;
    private Behavior.Status status = Behavior.Status.STOPPED;

    public ActionHandler(List<ActionBuilder<E>> actions) {
        this.actions = actions;
    }

    @Override
    public Behavior.@NonNull Status getStatus() {
        return this.status;
    }

    @Override
    public void tickOrStop(@NonNull ServerLevel level, E entity, long gameTime) {
        this.doStop(level, entity, gameTime);
    }

    @Override
    public final boolean tryStart(@NonNull ServerLevel level, E entity, long gameTime) {
        Brain<E> brain = (Brain<E>) entity.getBrain();
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
                    this.status = Behavior.Status.RUNNING;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public final void doStop(@NonNull ServerLevel level, E entity, long gameTime) {
        this.status = Behavior.Status.STOPPED;
    }

    @Override
    public @NonNull String debugString() {
        return "ActionHandler";
    }
}
