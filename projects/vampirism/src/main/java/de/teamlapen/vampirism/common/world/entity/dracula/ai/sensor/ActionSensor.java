package de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.DraculaState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ActionSensor extends Sensor<Dracula> {

    private final DraculaState state;
    private final List<IDraculaAction> actions = new ArrayList<>();

    public ActionSensor(DraculaState state) {
        this.state = state;
    }
    @Override
    protected void doTick(ServerLevel level, Dracula entity) {
        if (entity.getState() == state) {
            Brain<Dracula> brain = entity.getBrain();
            if (!brain.hasMemoryValue(ModMemoryTypes.Dracula.ACTION_ACTIVE.get()) && !brain.hasMemoryValue(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get())) {
                checkActions(level, entity, brain);
            }
        }
    }

    protected void checkActions(ServerLevel level, Dracula entity, Brain<Dracula> brain) {
        Optional<IDraculaAction> first = this.actions.stream().filter(x -> !brain.hasMemoryValue(x.getCooldownMemory())).filter(x -> x.canActivate(level, entity, brain)).findFirst();
        first.ifPresent(action -> {
            brain.setMemory(action.getActionMemory(), Unit.INSTANCE);
            brain.setMemory(ModMemoryTypes.Dracula.ACTION_ACTIVE.get(), Unit.INSTANCE);
        });
    }

    protected void addAction(IDraculaAction action) {
        this.actions.add(action);
    }

    @Override
    public final Set<MemoryModuleType<?>> requires() {
        return this.actions.stream().flatMap(x -> x.required().stream()).collect(Collectors.toUnmodifiableSet());
    }
}
