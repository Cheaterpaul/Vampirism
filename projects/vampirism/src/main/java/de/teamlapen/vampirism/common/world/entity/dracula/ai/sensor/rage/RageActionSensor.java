package de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.rage;

import de.teamlapen.vampirism.common.world.entity.dracula.ai.DraculaState;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.ActionSensor;

public class RageActionSensor extends ActionSensor {

    public RageActionSensor() {
        super(DraculaState.RAGED);
        addAction(new RegenerationAction());
    }
}
