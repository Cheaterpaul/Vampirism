package de.teamlapen.vampirism.common.world.entity.ai.system;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AiActionSensor<E extends LivingEntity> extends Sensor<E> {

    private final Predicate<E> canTick;
    private final List<AiAction<E>> actions = new ArrayList<>();

    public AiActionSensor(Predicate<E> canTick) {
        this.canTick = canTick;
    }

    @Override
    protected void doTick(ServerLevel level, E entity) {
        if (canTick.test(entity)) {
            Brain<E> brain = (Brain<E>) entity.getBrain();
            if (!brain.hasMemoryValue(ModMemoryTypes.Dracula.ACTION_ACTIVE.get()) && !brain.hasMemoryValue(ModMemoryTypes.Dracula.ACTION_COOLDOWN.get())) {
                checkActions(level, entity, brain);
            }
        }
    }

    protected void checkActions(ServerLevel level, E entity, Brain<E> brain) {
        Optional<AiAction<E>> first = this.actions.stream().filter(x -> !brain.hasMemoryValue(x.getCooldownMemory())).filter(x -> x.canActivate(level, entity, brain)).findFirst();
        first.ifPresent(action -> {
            brain.setMemory(action.getActionMemory(), Unit.INSTANCE);
            brain.setMemory(ModMemoryTypes.Dracula.ACTION_ACTIVE.get(), Unit.INSTANCE);
        });
    }

    public void addAction(AiAction<E> action) {
        this.actions.add(action);
    }

    @Override
    public final Set<MemoryModuleType<?>> requires() {
        return this.actions.stream().flatMap(x -> x.required().stream()).collect(Collectors.toUnmodifiableSet());
    }
}
