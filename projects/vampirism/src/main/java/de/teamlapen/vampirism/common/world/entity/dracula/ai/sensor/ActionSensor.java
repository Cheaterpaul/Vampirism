package de.teamlapen.vampirism.common.world.entity.dracula.ai.sensor;

import de.teamlapen.vampirism.common.world.entity.ai.system.AiAction;
import de.teamlapen.vampirism.common.world.entity.ai.system.AiActionSensor;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.ai.DraculaState;

public abstract class ActionSensor extends AiActionSensor<Dracula> {

    public ActionSensor(DraculaState state) {
        super(dracula -> dracula.getState() == state);
    }

    protected void addAction(IDraculaAction action) {
        super.addAction(action);
    }
}
