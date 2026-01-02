package de.teamlapen.vampirism.client.models.entities.dracula;

import de.teamlapen.vampirism.api.util.VIdentifier;
import de.teamlapen.vampirism.client.core.ModEntityRenderStates;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class DraculaPhase1Model extends GeoModel<Dracula> {

    public static final Identifier PHASE1 = VIdentifier.mod("entity/dracula.phase1");
    public static final Identifier PHASE2 = VIdentifier.mod("entity/dracula.phase2");
    public static final Identifier PHASE3 = VIdentifier.mod("entity/dracula.phase3");
    public static final Identifier TEXTURE_PHASE1 = VIdentifier.mod("textures/entity/dracula/phase1.png");
    public static final Identifier TEXTURE_PHASE2 = VIdentifier.mod("textures/entity/dracula/phase2.png");
    public static final Identifier TEXTURE_PHASE3 = VIdentifier.mod("textures/entity/dracula/phase3.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return switch (renderState.getGeckolibData(ModEntityRenderStates.DRACULA_STAGE)) {
            case PHASE_2 -> PHASE2;
            case PHASE_3 -> PHASE3;
            case null, default -> PHASE1;
        };
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return switch (renderState.getGeckolibData(ModEntityRenderStates.DRACULA_STAGE)) {
            case PHASE_2 -> TEXTURE_PHASE2;
            case PHASE_3 -> TEXTURE_PHASE3;
            case null, default -> TEXTURE_PHASE1;
        };
    }

    @Override
    public Identifier getAnimationResource(Dracula animatable) {
        return switch (animatable.getFightStage()) {
            case PHASE_2 -> PHASE2;
            case PHASE_3 -> PHASE3;
            default -> PHASE1;
        };
    }
}
