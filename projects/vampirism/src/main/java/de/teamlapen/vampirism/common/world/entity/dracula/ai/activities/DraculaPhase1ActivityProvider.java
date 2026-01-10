package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import de.teamlapen.vampirism.common.core.ModActivities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.ai.system.AiActivityProvider;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.SummonProtectorsBehavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Collections;
import java.util.List;

public class DraculaPhase1ActivityProvider extends AiActivityProvider<Dracula> {
    
    public DraculaPhase1ActivityProvider() {
        var activity = createActivity(ModActivities.DRACULA_PHASE_1)
                .add(DraculaIdleActivityProvider.createIdleLookBehaviors())
                .add(DraculaIdleActivityProvider.createIdleMovementBehaviors(0.3f))
                .requires(ModMemoryTypes.Dracula.PHASE_1, MemoryStatus.VALUE_PRESENT);

        activity.addAction(ModActivities.DRACULA_SUMMON_PROTECTOR)
                .actionMemory(ModMemoryTypes.Dracula.SUMMON_PROTECTOR_ACTIVE)
                .cooldownMemory(ModMemoryTypes.Dracula.SUMMON_PROTECTOR_COOLDOWN)
                .add(SummonProtectorsBehavior.create(), Collections.emptySet(), SummonProtectorsBehavior.memories())
                .canActivate((level, dracula) -> dracula.getBrain().getMemory(ModMemoryTypes.SUMMONS.get()).orElseGet(List::of).size() < SummonProtectorsBehavior.MAX_SUMMONS * 0.7);
    }
}
