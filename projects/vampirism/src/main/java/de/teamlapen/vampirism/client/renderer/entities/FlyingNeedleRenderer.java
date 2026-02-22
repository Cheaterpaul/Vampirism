package de.teamlapen.vampirism.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import de.teamlapen.vampirism.api.util.VIdentifier;
import de.teamlapen.vampirism.client.core.ModEntitiesRender;
import de.teamlapen.vampirism.client.models.entities.flying_needle.FlyingNeedleModel;
import de.teamlapen.vampirism.common.world.entity.dracula.FlyingNeedleEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;

public class FlyingNeedleRenderer extends EntityRenderer<FlyingNeedleEntity, FlyingNeedleRenderer.FlyingNeedleRenderState> {

    private static final Identifier TEXTURE = VIdentifier.mod("textures/entity/flying_needle/flying_needle.png");
    private final FlyingNeedleModel model;

    public FlyingNeedleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FlyingNeedleModel(context.bakeLayer(ModEntitiesRender.FLYING_NEEDLE));
    }

    @Override
    public void submit(FlyingNeedleRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0, -1.25, 0);
        this.model.setupAnim(renderState);
        nodeCollector.submitModel(this.model, renderState, poseStack, RenderTypes.entityCutout(TEXTURE), renderState.lightCoords, 0, 0,null);
        poseStack.popPose();
    }

    @Override
    public FlyingNeedleRenderState createRenderState() {
        return new FlyingNeedleRenderState();
    }

    public static class FlyingNeedleRenderState extends EntityRenderState {
    }
}
