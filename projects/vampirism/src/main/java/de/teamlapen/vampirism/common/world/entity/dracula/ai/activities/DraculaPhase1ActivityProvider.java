package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import de.teamlapen.vampirism.common.core.ModActivities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.ai.system.AiActivityProvider;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.SummonProtectorsBehavior;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.passive.SummonAction;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Collections;

public class DraculaPhase1ActivityProvider extends AiActivityProvider<Dracula> {

    public DraculaPhase1ActivityProvider() {
        addAction(new SummonAction());

        createActivity(ModActivities.DRACULA_PHASE_1)
                .add(SummonProtectorsBehavior.create(), Collections.emptySet(), SummonProtectorsBehavior.memories())
                .add(DraculaIdleActivityProvider.createIdleLookBehaviors())
                .add(DraculaIdleActivityProvider.createIdleMovementBehaviors(0.3f))
                .requires(ModMemoryTypes.Dracula.PHASE_1, MemoryStatus.VALUE_PRESENT);
    }
}
