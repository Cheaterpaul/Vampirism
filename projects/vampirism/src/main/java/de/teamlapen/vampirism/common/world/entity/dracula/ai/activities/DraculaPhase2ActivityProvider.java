package de.teamlapen.vampirism.common.world.entity.dracula.ai.activities;

import de.teamlapen.vampirism.common.core.ModActivities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.ai.activities.ActivityBuilder;
import de.teamlapen.vampirism.common.world.entity.ai.system.AiActivityProvider;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.*;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingneedle.FlyingNeedleAttack;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingsword.EquipSword;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingsword.FlyingSwordAttack;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingsword.UnEquipSword;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Set;

public class DraculaPhase2ActivityProvider extends AiActivityProvider<Dracula> {

    public DraculaPhase2ActivityProvider() {
        super(ModActivities.DRACULA_PHASE_2);
    }

    @Override
    protected void createActivity(ActivityBuilder<Dracula> builder) {
        builder
                .add(DraculaIdleActivityProvider.createIdleLookBehaviors(), DraculaIdleActivityProvider.lookSensors(), DraculaIdleActivityProvider.lookMemories())
                .add(KeepDistanceBehavior.create(0.4f, 10, 15), Set.of(), Set.of(MemoryModuleType.WALK_TARGET, MemoryModuleType.ATTACK_TARGET))
                .add(NearbyKnockbackBehavior.create(5, 0.5), Set.of(), Set.of(MemoryModuleType.ATTACK_TARGET))
                .add(SurroundedEscapeBehavior.create(4.0, 60, 2), Set.of(), Set.of(ModMemoryTypes.Dracula.SURROUNDED_TIMER.get(), ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE.get(), ModMemoryTypes.Dracula.FLYING_SWORD_COOLDOWN.get(), ModMemoryTypes.Dracula.ACTION_ACTIVE.get(), ModMemoryTypes.Dracula.ACTION_COOLDOWN.get(), MemoryModuleType.ATTACK_TARGET))
                .requires(ModMemoryTypes.Dracula.PHASE_2, MemoryStatus.VALUE_PRESENT);

        var actionBuilder = builder.useActions();
        actionBuilder.addAction(ModActivities.DRACULA_SUMMON_BATS, action -> action
                .activeMemory(ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_ACTIVE)
                .cooldown(ModMemoryTypes.Dracula.SUMMON_VAMPIRE_BATS_COOLDOWN, () -> 20 * 20)
                .addLast(SummonVampireBats.create(), SummonVampireBats.sensors(), SummonVampireBats.memories())
                .canActivate((level, dracula) -> dracula.getHealth() < (dracula.getMaxHealth() * 0.7)));

        actionBuilder.addAction(ModActivities.DRACULA_FLYING_SWORD, action -> action
                .activeMemory(ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE)
                .cooldown(ModMemoryTypes.Dracula.FLYING_SWORD_COOLDOWN, () -> 5 * 20)
                .add(EquipSword.create(), EquipSword.sensors(), EquipSword.memories())
                .add(FlyingSwordAttack.create(), FlyingSwordAttack.sensors(), FlyingSwordAttack.memories())
                .addLast(UnEquipSword.create(), UnEquipSword.sensors(), UnEquipSword.memories()));

        actionBuilder.addAction(ModActivities.DRACULA_FLYING_NEEDLE, action -> action
                .activeMemory(ModMemoryTypes.Dracula.FLYING_NEEDLE_ACTIVE)
                .cooldown(ModMemoryTypes.Dracula.FLYING_NEEDLE_COOLDOWN, () -> 20 * 20)
                .addLast(FlyingNeedleAttack.create(), FlyingNeedleAttack.sensors(), FlyingNeedleAttack.memories()));

        actionBuilder.addAction(ModActivities.DRACULA_BACKSTAB, action -> action
                .activeMemory(ModMemoryTypes.Dracula.BACKSTAB_ACTIVE)
                .cooldown(ModMemoryTypes.Dracula.BACKSTAB_COOLDOWN, () -> 15 * 20)
                .addLast(BackstabBehavior.create(), Set.of(), Set.of(MemoryModuleType.ATTACK_TARGET, ModMemoryTypes.Dracula.BACKSTAB_ACTIVE.get(), ModMemoryTypes.Dracula.BACKSTAB_COOLDOWN.get()))
                .canActivate((level, dracula) -> dracula.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)));
    }
}
