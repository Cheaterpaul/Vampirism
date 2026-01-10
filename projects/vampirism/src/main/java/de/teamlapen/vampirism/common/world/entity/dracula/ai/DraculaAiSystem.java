package de.teamlapen.vampirism.common.world.entity.dracula.ai;

import com.mojang.datafixers.kinds.Const;
import com.mojang.datafixers.kinds.IdF;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.ai.system.AiActivityProvider;
import de.teamlapen.vampirism.common.world.entity.ai.system.AiSystem;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.activities.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DraculaAiSystem extends AiSystem<Dracula> {

    public DraculaAiSystem(Dracula entity) {
        super(entity);
    }

    @Override
    protected List<AiActivityProvider<Dracula>> createActivityProviders() {
        return List.of(
                new DraculaCoreActivityProvider(),
                new DraculaIdleActivityProvider(),
                new DraculaPhase1ActivityProvider(),
                new DraculaPhase2ActivityProvider(),
                new DraculaPhase3ActivityProvider()
        );
    }

    @Override
    public void update(ServerLevel level) {
        this.updateMemories();
        this.updateActivity(level);
    }

    private void updateMemories() {
        Brain<Dracula> brain = this.entity.getBrain();
        Set<MemoryModuleType<Unit>> stageMemories = Stream.of(ModMemoryTypes.Dracula.PHASE_1.get(), ModMemoryTypes.Dracula.PHASE_2.get(), ModMemoryTypes.Dracula.PHASE_3.get()).collect(Collectors.toSet());
        MemoryModuleType<Unit> unitMemoryModuleType = memoryForStage(this.entity.getState());

        if (unitMemoryModuleType != null) {
            stageMemories.remove(unitMemoryModuleType);
            brain.setMemory(unitMemoryModuleType, Unit.INSTANCE);
        }
        stageMemories.forEach(brain::eraseMemory);
    }

    private void updateActivity(ServerLevel level) {
        Brain<Dracula> brain = this.entity.getBrain();
        brain.setActiveActivityToFirstValid(
                this.activityProviders.stream().flatMap(AiActivityProvider::getActiveActivities).toList()
        );
    }

    @Nullable
    private static MemoryModuleType<Unit> memoryForStage(DraculaState state) {
        if (state == DraculaState.TRANSFORMING_TO_RANGED) {
            return null;
        } else if (state == DraculaState.RANGED) {
            return ModMemoryTypes.Dracula.PHASE_2.get();
        } else if (state == DraculaState.TRANSFORMING_TO_RAGED) {
            return null;
        } else if (state == DraculaState.RAGED) {
            return ModMemoryTypes.Dracula.PHASE_3.get();
        } else if (state == DraculaState.PASSIVE) {
            return ModMemoryTypes.Dracula.PHASE_1.get();
        } else {
            return null;
        }
    }

    public static void setActionCooldown(MemoryAccessor<Const.Mu<com.mojang.datafixers.util.Unit>, net.minecraft.util.Unit> cooldown, MemoryAccessor<IdF.Mu, net.minecraft.util.Unit> active, MemoryAccessor<Const.Mu<com.mojang.datafixers.util.Unit>, net.minecraft.util.Unit> actionCooldown, MemoryAccessor<IdF.Mu, net.minecraft.util.Unit> actionActive, int actionCooldownTicks) {
        active.erase();
        cooldown.setWithExpiry(net.minecraft.util.Unit.INSTANCE, 20 * 20);
        actionActive.erase();
        actionCooldown.setWithExpiry(net.minecraft.util.Unit.INSTANCE, actionCooldownTicks);
    }
}
