package de.teamlapen.vampirism.common.world.entity.ai.activities.actions;

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

/**
 * Behavior to select action to execute
 */
public class ActionBehavior<E extends LivingEntity> implements BehaviorControl<E> {

    private final List<Action<E>> actions;
    private Behavior.Status status = Behavior.Status.STOPPED;

    public ActionBehavior(List<Action<E>> builder) {
        this.actions = builder;
    }

    @Override
    public Behavior.Status getStatus() {
        return this.status;
    }

    @Override
    public void tickOrStop(ServerLevel level, E entity, long gameTime) {
        doStop(level, entity, gameTime);
    }

    @Override
    public final boolean tryStart(ServerLevel level, E entity, long gameTime) {
        //noinspection unchecked
        Brain<E> brain = (Brain<E>) entity.getBrain();

        // check if an action is already active or actions are on cooldown
        if (brain.hasMemoryValue(ModMemoryTypes.ACTION_ACTIVE.get()) || brain.hasMemoryValue(ModMemoryTypes.ACTION_COOLDOWN.get())) {
            return false;
        }

        // randomize actions
        var actions = new ArrayList<>(this.actions);
        Collections.shuffle(actions);

        // select the first activable action
        for (Action<E> action : actions) {
            if (brain.hasMemoryValue(action.cooldownMemory().memory())) {
                continue;
            }

            // activate
            if (action.requirements().stream().allMatch(pair -> brain.checkMemory(pair.getFirst(), pair.getSecond())) && action.precondition().test(level, entity)) {
                brain.setMemory(ModMemoryTypes.ACTION_ACTIVE.get(), net.minecraft.util.Unit.INSTANCE);
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
        this.status = Behavior.Status.STOPPED;
    }

    @Override
    public String debugString() {
        return "ActionBehavior";
    }


}
