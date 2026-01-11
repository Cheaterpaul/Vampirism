package de.teamlapen.vampirism.client.models.entities.dracula.needle;

import de.teamlapen.vampirism.api.util.VIdentifier;
import de.teamlapen.vampirism.common.world.entity.dracula.FlyingNeedleEntity;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class FlyingNeedleModel  extends DefaultedEntityGeoModel<FlyingNeedleEntity> {

    public static final Identifier PHASE1 = VIdentifier.mod("flying_needle");

    public FlyingNeedleModel() {
        super(PHASE1);
    }
}
