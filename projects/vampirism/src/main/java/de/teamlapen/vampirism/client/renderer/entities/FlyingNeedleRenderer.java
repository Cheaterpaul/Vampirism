package de.teamlapen.vampirism.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.teamlapen.vampirism.api.util.VIdentifier;
import de.teamlapen.vampirism.common.world.entity.dracula.FlyingNeedleEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

public class FlyingNeedleRenderer extends EntityRenderer<FlyingNeedleEntity, FlyingNeedleRenderer.FlyingNeedleRenderState> {
    private static final Identifier TEXTURE = VIdentifier.mod("textures/entity/dracula/flying_needle.png");

    public FlyingNeedleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public FlyingNeedleRenderState createRenderState() {
        return new FlyingNeedleRenderState();
    }

    @Override
    public void extractRenderState(FlyingNeedleEntity entity, FlyingNeedleRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.yRot = entity.getViewYRot(partialTick);
        state.xRot = entity.getViewXRot(partialTick);
    }

    @Override
    public void submit(FlyingNeedleRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));

        nodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TEXTURE), (pose, vertexBuilder) -> {
            float size = 0.3f;
            Matrix4f matrix4f = pose.pose();
            int light = 15728880;
            float thickness = 0.0625f; // 1/16th for a "thickness of 1" in pixel terms

            // Front
            vertex0(vertexBuilder, matrix4f, pose, light, -size, -size, thickness / 2f, 0, 1, 0, 0, 1);
            vertex0(vertexBuilder, matrix4f, pose, light, size, -size, thickness / 2f, 1, 1, 0, 0, 1);
            vertex0(vertexBuilder, matrix4f, pose, light, size, size, thickness / 2f, 1, 0, 0, 0, 1);
            vertex0(vertexBuilder, matrix4f, pose, light, -size, size, thickness / 2f, 0, 0, 0, 0, 1);

            // Back
            vertex0(vertexBuilder, matrix4f, pose, light, size, size, -thickness / 2f, 1, 0, 0, 0, -1);
            vertex0(vertexBuilder, matrix4f, pose, light, -size, size, -thickness / 2f, 0, 0, 0, 0, -1);
            vertex0(vertexBuilder, matrix4f, pose, light, -size, -size, -thickness / 2f, 0, 1, 0, 0, -1);
            vertex0(vertexBuilder, matrix4f, pose, light, size, -size, -thickness / 2f, 1, 1, 0, 0, -1);

            // Sides (simplified "thickness" as requested)
            // Top
            vertex0(vertexBuilder, matrix4f, pose, light, -size, size, -thickness / 2f, 0, 0, 0, 1, 0);
            vertex0(vertexBuilder, matrix4f, pose, light, -size, size, thickness / 2f, 0, 0, 0, 1, 0);
            vertex0(vertexBuilder, matrix4f, pose, light, size, size, thickness / 2f, 1, 0, 0, 1, 0);
            vertex0(vertexBuilder, matrix4f, pose, light, size, size, -thickness / 2f, 1, 0, 0, 1, 0);

            // Bottom
            vertex0(vertexBuilder, matrix4f, pose, light, -size, -size, thickness / 2f, 0, 1, 0, -1, 0);
            vertex0(vertexBuilder, matrix4f, pose, light, -size, -size, -thickness / 2f, 0, 1, 0, -1, 0);
            vertex0(vertexBuilder, matrix4f, pose, light, size, -size, -thickness / 2f, 1, 1, 0, -1, 0);
            vertex0(vertexBuilder, matrix4f, pose, light, size, -size, thickness / 2f, 1, 1, 0, -1, 0);

            // Left
            vertex0(vertexBuilder, matrix4f, pose, light, -size, size, thickness / 2f, 0, 0, -1, 0, 0);
            vertex0(vertexBuilder, matrix4f, pose, light, -size, size, -thickness / 2f, 0, 0, -1, 0, 0);
            vertex0(vertexBuilder, matrix4f, pose, light, -size, -size, -thickness / 2f, 0, 1, -1, 0, 0);
            vertex0(vertexBuilder, matrix4f, pose, light, -size, -size, thickness / 2f, 0, 1, -1, 0, 0);

            // Right
            vertex0(vertexBuilder, matrix4f, pose, light, size, size, -thickness / 2f, 1, 0, 1, 0, 0);
            vertex0(vertexBuilder, matrix4f, pose, light, size, size, thickness / 2f, 1, 0, 1, 0, 0);
            vertex0(vertexBuilder, matrix4f, pose, light, size, -size, thickness / 2f, 1, 1, 1, 0, 0);
            vertex0(vertexBuilder, matrix4f, pose, light, size, -size, -thickness / 2f, 1, 1, 1, 0, 0);

        });

        poseStack.popPose();
        super.submit(state, poseStack, nodeCollector, cameraRenderState);
    }

    private static void vertex0(VertexConsumer pConsumer, Matrix4f pMatrix, PoseStack.Pose pPose, int pLight, float pX, float pY, float pZ, float pU, float pV, float nX, float nY, float nZ) {
        pConsumer.addVertex(pMatrix, pX, pY, pZ).setColor(255, 255, 255, 255).setUv(pU, pV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(pLight).setNormal(pPose, nX, nY, nZ);
    }

    public static class FlyingNeedleRenderState extends EntityRenderState {
        public float yRot;
        public float xRot;
    }
}
