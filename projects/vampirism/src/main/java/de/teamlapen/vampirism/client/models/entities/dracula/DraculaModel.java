package de.teamlapen.vampirism.client.models.entities.dracula;

import de.teamlapen.vampirism.api.util.VIdentifier;
import de.teamlapen.vampirism.client.core.ModEntityRenderStates;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.FightStage;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import java.util.Map;

public class DraculaModel extends GeoModel<Dracula> {

    public static final ThreadLocal<FightStage> RENDER_STAGE = new ThreadLocal<>();
    private static final Identifier PHASE1 = VIdentifier.mod("entity/dracula.phase1");
    private static final Identifier PHASE2 = VIdentifier.mod("entity/dracula.phase2");
    private static final Identifier PHASE3 = VIdentifier.mod("entity/dracula.phase3");
    private static final Identifier TEXTURE_PHASE1 = VIdentifier.mod("textures/entity/dracula/phase1.png");
    private static final Identifier TEXTURE_PHASE2 = VIdentifier.mod("textures/entity/dracula/phase2.png");
    private static final Identifier TEXTURE_PHASE3 = VIdentifier.mod("textures/entity/dracula/phase3.png");

    private final Map<FightStage, PhaseModel> models;

    public DraculaModel() {
        PhaseModel phase1 = new PhaseModel(PHASE1, TEXTURE_PHASE1);

        this.models = Map.of(
            FightStage.NONE, phase1,
            FightStage.PHASE_1, phase1,
            FightStage.PHASE_2, new PhaseModel(PHASE2, TEXTURE_PHASE2),
            FightStage.PHASE_3, new PhaseModel(PHASE3, TEXTURE_PHASE3)
        );
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        FightStage stage = RENDER_STAGE.get();
        if (stage == null) {
            stage = renderState.getGeckolibData(ModEntityRenderStates.DRACULA_STAGE);
        }
        return this.models.get(stage).getModelResource(renderState);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        FightStage stage = RENDER_STAGE.get();
        if (stage == null) {
            stage = renderState.getGeckolibData(ModEntityRenderStates.DRACULA_STAGE);
        }
        return this.models.get(stage).getTextureResource(renderState);
    }

    @Override
    public Identifier getAnimationResource(Dracula animatable) {
        FightStage stage = RENDER_STAGE.get();
        if (stage == null) {
            stage = animatable.getStage();
        }
        return this.models.get(stage).getAnimationResource(animatable);
    }

    private static class PhaseModel extends GeoModel<Dracula> {

        private final Identifier resource;
        private final Identifier texture;

        public PhaseModel(Identifier resource, Identifier texture) {
            this.resource = resource;
            this.texture = texture;
        }

        @Override
        public Identifier getModelResource(GeoRenderState renderState) {
            return this.resource;
        }

        @Override
        public Identifier getTextureResource(GeoRenderState renderState) {
            return this.texture;
        }

        @Override
        public Identifier getAnimationResource(Dracula animatable) {
            return this.resource;
        }
    }
}
