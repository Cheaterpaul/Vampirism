package de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.ranged;

import de.teamlapen.vampirism.common.world.entity.dracula.ai.DraculaState;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor.ActionSensor;

public class RangedActionSensor extends ActionSensor {

    public RangedActionSensor() {
        super(DraculaState.RANGED);
        addAction(new SummonVampiricBatsAction());
        addAction(new FlyingSwordAction());
    }
}
