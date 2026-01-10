package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import de.teamlapen.vampirism.common.core.ModActivities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.ai.activities.ActivityBuilder;
import de.teamlapen.vampirism.common.world.entity.ai.system.AiActivityProvider;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.SummonProtectorsBehavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.List;
import java.util.Set;

public class DraculaPhase1ActivityProvider extends AiActivityProvider<Dracula> {

    public DraculaPhase1ActivityProvider() {
        super(ModActivities.DRACULA_PHASE_1);
    }

    public void createActivity(ActivityBuilder<Dracula> builder) {
        builder.add(DraculaIdleActivityProvider.createIdleLookBehaviors(), Set.of(SensorType.NEAREST_LIVING_ENTITIES), Set.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES))
                .add(DraculaIdleActivityProvider.createIdleMovementBehaviors(0.3f), Set.of(), Set.of(MemoryModuleType.WALK_TARGET))
                .requires(ModMemoryTypes.Dracula.PHASE_1, MemoryStatus.VALUE_PRESENT);

        var actions = builder.useActions();

        actions.addAction(ModActivities.DRACULA_SUMMON_PROTECTOR, action -> action
                .activeMemory(ModMemoryTypes.Dracula.SUMMON_PROTECTOR_ACTIVE)
                .cooldown(ModMemoryTypes.Dracula.SUMMON_PROTECTOR_COOLDOWN, () -> 20 * 20)
                .add(SummonProtectorsBehavior.create())
                .canActivate((level, dracula) -> dracula.getBrain().getMemory(ModMemoryTypes.SUMMONS.get()).map(List::size).orElse(0) < SummonProtectorsBehavior.MAX_SUMMONS * 0.7));
    }
}
