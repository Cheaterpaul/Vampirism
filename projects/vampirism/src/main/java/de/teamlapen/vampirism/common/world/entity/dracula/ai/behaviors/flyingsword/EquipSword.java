package de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingsword;

import de.teamlapen.vampirism.common.core.ModItems;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;

public class EquipSword {
    public static OneShot<Dracula> create() {
        return BehaviorBuilder.create(inst -> inst.group(
                inst.absent(ModMemoryTypes.Dracula.FLYING_SWORD_EQUIPPED.get()),
                inst.present(ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE.get())
        ).apply(inst, (equipped, active) ->
                (level, dracula, gameTime) -> {
            dracula.setItemInHand(InteractionHand.MAIN_HAND, ModItems.HEART_SEEKER_ULTIMATE.toStack());
            equipped.set(Unit.INSTANCE);
            return true;
        }));
    }
}
