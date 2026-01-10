package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import de.teamlapen.vampirism.common.core.ModActivities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.ai.activities.ActivityBuilder;
import de.teamlapen.vampirism.common.world.entity.ai.system.AiActivityProvider;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.SummonVampireBats;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingneedle.FlyingNeedleAttack;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingsword.EquipSword;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingsword.FlyingSwordAttack;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingsword.UnEquipSword;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class DraculaPhase2ActivityProvider extends AiActivityProvider<Dracula> {

    public DraculaPhase2ActivityProvider() {
        super(ModActivities.DRACULA_PHASE_2);
    }

    @Override
    protected void createActivity(ActivityBuilder<Dracula> builder) {
        builder
                .add(DraculaIdleActivityProvider.createIdleLookBehaviors())
                .add(DraculaIdleActivityProvider.createIdleMovementBehaviors(0.3f))
                .requires(ModMemoryTypes.Dracula.PHASE_2, MemoryStatus.VALUE_PRESENT);

        var actionBuilder = builder.useActions();
        actionBuilder.addAction(ModActivities.DRACULA_SUMMON_BATS)
                .actionMemory(ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_ACTIVE)
                .cooldownMemory(ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_COOLDOWN)
                .add(SummonVampireBats.create())
                .canActivate((level, dracula) -> dracula.getHealth() < (dracula.getMaxHealth() * 0.7));

        actionBuilder.addAction(ModActivities.DRACULA_FLYING_SWORD)
                .actionMemory(ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE)
                .cooldownMemory(ModMemoryTypes.Dracula.FLYING_SWORD_COOLDOWN)
                .add(EquipSword.create())
                .add(new FlyingSwordAttack())
                .add(UnEquipSword.create())
                .canActivate((level, dracula) -> true);

        actionBuilder.addAction(ModActivities.DRACULA_FLYING_NEEDLE)
                .actionMemory(ModMemoryTypes.Dracula.FLYING_NEEDLE_ACTIVE)
                .cooldownMemory(ModMemoryTypes.Dracula.FLYING_NEEDLE_COOLDOWN)
                .add(new FlyingNeedleAttack())
                .canActivate((level, dracula) -> true);
    }
}
