package de.teamlapen.vampirism.common.world.entity.dracula.ai.behaviors.flyingsword;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.item.ItemStack;

public class UnEquipSword {
    public static OneShot<Dracula> create() {
        return BehaviorBuilder.create(inst -> inst.group(
                inst.present(ModMemoryTypes.Dracula.FLYING_SWORD_EQUIPPED.get()),
                inst.absent(ModMemoryTypes.Dracula.FLYING_SWORD_ACTIVE.get())
        ).apply(inst, (equipped, active) ->
                (level, dracula, gameTime) -> {
            dracula.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            equipped.erase();
            return true;
        }));
    }
}
