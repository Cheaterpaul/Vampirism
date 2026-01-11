package de.teamlapen.vampirism.common.world.entity.dracula;

import software.bernie.geckolib.animation.RawAnimation;

public interface IDraculaAnimations {

    RawAnimation PHASE_1_TRANSITION = RawAnimation.begin().thenPlay("phase.2.transition");

    RawAnimation PHASE_3_ATTACK_1 = RawAnimation.begin().thenPlay("attack.melee.1");
    RawAnimation PHASE_3_ATTACK_2 = RawAnimation.begin().thenPlay("attack.melee.2");

    enum Animation {
        NEEDLE_1(FightStage.PHASE_2, RawAnimation.begin().thenPlay("attack.needles.1")),
        NEEDLE_2(FightStage.PHASE_2, RawAnimation.begin().thenPlay("attack.needles.2")),
        SWORD_1(FightStage.PHASE_2, RawAnimation.begin().thenPlay("attack.sword.1")),
        SWORD_2(FightStage.PHASE_2, RawAnimation.begin().thenPlay("attack.sword.2"))
        ;

        public final FightStage stage;
        public final RawAnimation animation;

        Animation(FightStage stage, RawAnimation animation) {
            this.stage = stage;
            this.animation = animation;
        }

        public String id() {
            return this.name().toLowerCase();
        }
    }

}
