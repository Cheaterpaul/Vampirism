package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import de.teamlapen.vampirism.common.core.ModActivities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.core.ModSensors;
import de.teamlapen.vampirism.common.world.entity.ai.system.AiActivityProvider;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.SummonVampireBats;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingneedle.FlyingNeedleAttack;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingsword.EquipSword;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingsword.FlyingSwordAttack;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingsword.UnEquipSword;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.ranged.FlyingNeedleAction;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.ranged.FlyingSwordAction;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.ranged.SummonVampiricBatsAction;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class DraculaPhase2ActivityProvider extends AiActivityProvider<Dracula> {

    public DraculaPhase2ActivityProvider() {
        addSensor(ModSensors.DRACULA_RANGED_ACTION.get());

        addMemory(ModMemoryTypes.Dracula.PHASE_2.get());
        addMemory(ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_COOLDOWN.get());
        addMemory(ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_ACTIVE.get());
        addMemory(ModMemoryTypes.Dracula.FLYING_SWORD_COOLDOWN.get());
        addMemory(ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE.get());
        addMemory(ModMemoryTypes.Dracula.FLYING_NEEDLE_COOLDOWN.get());
        addMemory(ModMemoryTypes.Dracula.FLYING_NEEDLE_ACTIVE.get());

        addAction(new SummonVampiricBatsAction());
        addAction(new FlyingSwordAction());
        addAction(new FlyingNeedleAction());

        createActivity(ModActivities.DRACULA_PHASE_2)
                .add(SummonVampireBats.create())
                .add(DraculaIdleActivityProvider.createIdleLookBehaviors())
                .add(DraculaIdleActivityProvider.createIdleMovementBehaviors(0.3f))
                .requires(ModMemoryTypes.Dracula.PHASE_2, MemoryStatus.VALUE_PRESENT);

        createActivity(ModActivities.DRACULA_FLYING_SWORD)
                .requires(ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE, MemoryStatus.VALUE_PRESENT)
                .add(EquipSword.create())
                .add(new FlyingSwordAttack())
                .add(UnEquipSword.create());

        createActivity(ModActivities.DRACULA_FLYING_NEEDLE)
                .requires(ModMemoryTypes.Dracula.FLYING_NEEDLE_ACTIVE, MemoryStatus.VALUE_PRESENT)
                .add(new FlyingNeedleAttack());
    }
}
