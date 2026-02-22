package de.teamlapen.vampirism.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.teamlapen.vampirism.api.util.VIdentifier;
import de.teamlapen.vampirism.common.world.entity.dracula.FlyingSwordEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

public class FlyingSwordRenderer extends EntityRenderer<FlyingSwordEntity, FlyingSwordRenderer.FlyingSwordRenderState> {
    private static final Identifier TEXTURE = VIdentifier.mod("textures/entity/dracula/flying_sword.png");

    public FlyingSwordRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public FlyingSwordRenderState createRenderState() {
        return new FlyingSwordRenderState();
    }

    @Override
    public void extractRenderState(FlyingSwordEntity entity, FlyingSwordRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.yRot = entity.getViewYRot(partialTick);
        state.xRot = entity.getViewXRot(partialTick);
    }

    @Override
    public void submit(FlyingSwordRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));

        nodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TEXTURE), (pose, vertexBuilder) -> {
            float size = 0.5f;
            Matrix4f matrix4f = pose.pose();
            int light = 15728880;
            vertex0(vertexBuilder, matrix4f, pose, light, -size, -size, 0, 1);
            vertex0(vertexBuilder, matrix4f, pose, light, size, -size, 1, 1);
            vertex0(vertexBuilder, matrix4f, pose, light, size, size, 1, 0);
            vertex0(vertexBuilder, matrix4f, pose, light, -size, size, 0, 0);
        });

        poseStack.popPose();
        super.submit(state, poseStack, nodeCollector, cameraRenderState);
    }

    private static void vertex0(VertexConsumer pConsumer, Matrix4f pMatrix, PoseStack.Pose pPose, int pLight, float pX, float pY, int pU, int pV) {
        pConsumer.addVertex(pMatrix, pX, pY, 0.0f)
                .setColor(255, 255, 255, 255)
                .setUv((float)pU, (float)pV)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(pLight)
                .setNormal(pPose, 0.0F, 0.0F, 1.0F);
    }

    public static class FlyingSwordRenderState extends EntityRenderState {
        public float yRot;
        public float xRot;
    }
}
