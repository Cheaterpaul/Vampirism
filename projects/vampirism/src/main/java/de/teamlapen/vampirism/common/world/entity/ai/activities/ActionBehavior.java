package de.teamlapen.vampirism.common.world.entity.ai.activities;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActionBehavior<E extends LivingEntity> implements BehaviorControl<E> {

    private final List<ActionBuilder.Action<E>> actions;
    private Behavior.Status status = Behavior.Status.STOPPED;

    ActionBehavior(List<ActionBuilder.Action<E>> builder) {
        this.actions = builder;
    }

    @Override
    public Behavior.Status getStatus() {
        return this.status;
    }

    @Override
    public void tickOrStop(ServerLevel level, E entity, long gameTime) {

    }

    @Override
    public final boolean tryStart(ServerLevel level, E entity, long gameTime) {
        //noinspection unchecked
        Brain<E> brain = (Brain<E>) entity.getBrain();
        if (brain.hasMemoryValue(ModMemoryTypes.Dracula.ACTION_ACTIVE.get()) || brain.hasMemoryValue(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get())) {
            return false;
        }

        var actions = new ArrayList<>(this.actions);
        Collections.shuffle(actions);

        for (ActionBuilder.Action<E> action : actions) {
            if (brain.hasMemoryValue(action.cooldownMemory().memory())) {
                continue;
            }

            if (action.requirements().stream().allMatch(pair -> brain.checkMemory(pair.getFirst(), pair.getSecond())) && action.precondition().test(level, entity)) {
                brain.setMemory(ModMemoryTypes.Dracula.ACTION_ACTIVE.get(), net.minecraft.util.Unit.INSTANCE);
                brain.setMemory(action.activeMemory(), Unit.INSTANCE);
                brain.setActiveActivityIfPossible(action.activity());
                this.status = Behavior.Status.RUNNING;
                return true;
            }
        }
        return false;
    }

    @Override
    public final void doStop(ServerLevel level, E entity, long gameTime) {
        Brain<?> brain = entity.getBrain();
        brain.eraseMemory(ModMemoryTypes.Dracula.ACTION_ACTIVE.get());
        brain.setMemoryWithExpiry(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get(), Unit.INSTANCE, 100);
        for (ActionBuilder.Action<E> action : this.actions) {
            if (brain.hasMemoryValue(action.activeMemory())) {
                brain.eraseMemory(action.activeMemory());
                ActionBuilder.Cooldown cooldown = action.cooldownMemory();
                brain.setMemoryWithExpiry(cooldown.memory(), Unit.INSTANCE, cooldown.cooldown());
            }
        }
        this.status = Behavior.Status.STOPPED;
    }

    @Override
    public String debugString() {
        return "ActionBehavior";
    }


}
