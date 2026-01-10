package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import de.teamlapen.vampirism.common.core.ModActivities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.core.ModSensors;
import de.teamlapen.vampirism.common.world.entity.ai.system.AiActivityProvider;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.SummonProtectorsBehavior;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.passive.SummonAction;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class DraculaPhase1ActivityProvider extends AiActivityProvider<Dracula> {

    public DraculaPhase1ActivityProvider() {
        addSensor(ModSensors.DRACULA_PASSIVE_ACTION.get());

        addMemory(ModMemoryTypes.Dracula.PHASE_1.get());
        addMemory(ModMemoryTypes.Dracula.SUMMON_PROTECTOR_COOLDOWN.get());
        addMemory(ModMemoryTypes.Dracula.SUMMON_PROTECTOR_ACTIVE.get());
        addMemory(ModMemoryTypes.SUMMONS.get());

        addAction(new SummonAction());

        createActivity(ModActivities.DRACULA_PHASE_1)
                .add(SummonProtectorsBehavior.create())
                .add(DraculaIdleActivityProvider.createIdleLookBehaviors())
                .add(DraculaIdleActivityProvider.createIdleMovementBehaviors(0.3f))
                .requires(ModMemoryTypes.Dracula.PHASE_1, MemoryStatus.VALUE_PRESENT);
    }
}
