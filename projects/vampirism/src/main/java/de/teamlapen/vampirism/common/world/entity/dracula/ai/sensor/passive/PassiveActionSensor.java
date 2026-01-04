package de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.passive;

import de.teamlapen.vampirism.common.world.entity.dracula.ai.DraculaState;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.ActionSensor;

public class PassiveActionSensor extends ActionSensor {

    public PassiveActionSensor() {
        super(DraculaState.PASSIVE);
        addAction(new SummonAction());
    }
}
