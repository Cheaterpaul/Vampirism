package de.teamlapen.vampirism.common.world.entity.dracula;

import software.bernie.geckolib.animation.RawAnimation;

public interface IDraculaAnimations {

    RawAnimation PHASE_1_TRANSITION = RawAnimation.begin().thenPlay("phase.2.transition");

    RawAnimation PHASE_3_ATTACK_1 = RawAnimation.begin().thenPlay("attack.melee.1");
    RawAnimation PHASE_3_ATTACK_2 = RawAnimation.begin().thenPlay("attack.melee.2");

}
