package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import de.teamlapen.vampirism.common.core.ModActivities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.ai.system.AiActivityProvider;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.SummonVampireBats;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingneedle.FlyingNeedleAttack;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingsword.EquipSword;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingsword.FlyingSwordAttack;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingsword.UnEquipSword;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Set;

public class DraculaPhase2ActivityProvider extends AiActivityProvider<Dracula> {

    public DraculaPhase2ActivityProvider() {
        var activity = createActivity(ModActivities.DRACULA_PHASE_2)
                .add(DraculaIdleActivityProvider.createIdleLookBehaviors())
                .add(DraculaIdleActivityProvider.createIdleMovementBehaviors(0.3f))
                .requires(ModMemoryTypes.Dracula.PHASE_2, MemoryStatus.VALUE_PRESENT);

        activity.addAction(ModActivities.DRACULA_SUMMON_BATS)
                .actionMemory(ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_ACTIVE)
                .cooldownMemory(ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_COOLDOWN)
                .add(SummonVampireBats.create(), Set.of(), SummonVampireBats.memories())
                .canActivate((level, dracula) -> dracula.getHealth() < (dracula.getMaxHealth() * 0.7));

        activity.addAction(ModActivities.DRACULA_FLYING_SWORD)
                .actionMemory(ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE)
                .cooldownMemory(ModMemoryTypes.Dracula.FLYING_SWORD_COOLDOWN)
                .add(EquipSword.create(), Set.of(), Set.of(ModMemoryTypes.Dracula.FLYING_SWORD_EQUIPPED.get(), ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE.get()))
                .add(new FlyingSwordAttack())
                .add(UnEquipSword.create(), Set.of(), Set.of(ModMemoryTypes.Dracula.FLYING_SWORD_EQUIPPED.get(), ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE.get()))
                .canActivate((level, dracula) -> true);

        activity.addAction(ModActivities.DRACULA_FLYING_NEEDLE)
                .actionMemory(ModMemoryTypes.Dracula.FLYING_NEEDLE_ACTIVE)
                .cooldownMemory(ModMemoryTypes.Dracula.FLYING_NEEDLE_COOLDOWN)
                .add(new FlyingNeedleAttack())
                .canActivate((level, dracula) -> true);
    }
}
