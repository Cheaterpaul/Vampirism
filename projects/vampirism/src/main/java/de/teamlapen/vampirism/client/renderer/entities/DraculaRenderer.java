package de.teamlapen.vampirism.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.teamlapen.vampirism.client.core.ModEntityRenderStates;
import de.teamlapen.vampirism.client.models.entities.dracula.DraculaModel;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.FightStage;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.cache.model.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.PerBoneRender;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class DraculaRenderer<RenderState extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<Dracula, RenderState> {

    private final Map<FightStage, List<GeoRenderLayer<Dracula, Void, RenderState>>> layers = Util.makeEnumMap(FightStage.class, stage -> new ArrayList<>());

    public DraculaRenderer(EntityRendererProvider.Context context) {
        super(context, new DraculaModel());
        withRenderLayer(new FormRenderLayer(FightStage.PHASE_2, new Hand()));
        withRenderLayer(new TransitionLayer());
    }


    @Override
    public void addRenderData(Dracula animatable, @Nullable Void relatedObject, RenderState renderState, float partialTick) {
        var state = animatable.getState();

        renderState.addGeckolibData(ModEntityRenderStates.DRACULA_STAGE, state.stage);
        renderState.addGeckolibData(ModEntityRenderStates.DRACULA_TRANSFORMING, state.isTransforming);
        if (state.isTransforming) {
            float progress = (animatable.level().getGameTime() + partialTick - animatable.getTransformationStart()) / (float) state.transformTime;
            renderState.addGeckolibData(ModEntityRenderStates.DRACULA_TRANSFORMATION_PROGRESS, Math.min(1, Math.max(0, progress)));
        } else {
            renderState.addGeckolibData(ModEntityRenderStates.DRACULA_TRANSFORMATION_PROGRESS, 0f);
        }
    }

    public Map<FightStage, List<GeoRenderLayer<Dracula, Void, RenderState>>> getLayers() {
        return layers;
    }

    private class Hand extends ItemInHandGeoLayer<Dracula, Void,RenderState> {

        public Hand() {
            super(DraculaRenderer.this);
        }

        @Override
        protected void submitItemStackRender(PoseStack poseStack, GeoBone bone, ItemStack stack, ItemDisplayContext displayContext, RenderState renderState, SubmitNodeCollector renderTasks, CameraRenderState cameraState, int packedLight, int packedOverlay, int renderColor) {
            poseStack.pushPose();
            if (displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
                poseStack.translate(0.4f, 0f, 0.82f);
                if (stack.getItem() instanceof ShieldItem) {
                    poseStack.translate(0.0, 0.125, -0.25);
                }
                poseStack.mulPose(Axis.XN.rotationDegrees(-90.0F));
            } else if (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                poseStack.translate(-0.4f, 0f, 0.82f);
                if (stack.getItem() instanceof ShieldItem) {
                    poseStack.translate(0.0, 0.125, -0.25);
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                }
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            }

            super.submitItemStackRender(poseStack, bone, stack, displayContext, renderState, renderTasks, cameraState, packedLight, packedOverlay, renderColor);
            poseStack.popPose();
        }
    }

    public class FormRenderLayer extends GeoRenderLayer<Dracula, Void, RenderState> {

        private final FightStage stage;
        private final GeoRenderLayer<Dracula, Void, RenderState> parent;

        public FormRenderLayer(FightStage stage, GeoRenderLayer<Dracula, Void, RenderState> parent) {
            super(DraculaRenderer.this);
            this.stage = stage;
            this.parent = parent;
        }

        @Override
        public void addPerBoneRender(RenderPassInfo<RenderState> renderPassInfo, BiConsumer<GeoBone, PerBoneRender<RenderState>> consumer) {
            if (renderPassInfo.renderState().getGeckolibData(ModEntityRenderStates.DRACULA_STAGE) == this.stage) {
                this.parent.addPerBoneRender(renderPassInfo, consumer);
            }
        }

        @Override
        public void submitRenderTask(RenderPassInfo<RenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (renderPassInfo.renderState().getGeckolibData(ModEntityRenderStates.DRACULA_STAGE) == this.stage) {
                    this.parent.submitRenderTask(renderPassInfo, renderTasks);
                }
        }

        @Override
        public void preRender(RenderPassInfo<RenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (renderPassInfo.renderState().getGeckolibData(ModEntityRenderStates.DRACULA_STAGE) == this.stage) {
                this.parent.preRender(renderPassInfo, renderTasks);
            }
        }

        @Override
        public void addRenderData(Dracula animatable, @Nullable Void relatedObject, RenderState renderState, float partialTick) {
            if (renderState.getGeckolibData(ModEntityRenderStates.DRACULA_STAGE) == this.stage) {
                //noinspection OverrideOnly
                this.parent.addRenderData(animatable, relatedObject, renderState, partialTick);
            }
        }
    }

    private class TransitionLayer extends GeoRenderLayer<Dracula, Void, RenderState> {

        public TransitionLayer() {
            super(DraculaRenderer.this);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<RenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            RenderState state = renderPassInfo.renderState();
            if (Boolean.TRUE.equals(state.getGeckolibData(ModEntityRenderStates.DRACULA_TRANSFORMING))) {
                FightStage currentStage = state.getGeckolibData(ModEntityRenderStates.DRACULA_STAGE);
                FightStage previousStage = switch (currentStage) {
                    case PHASE_2 -> FightStage.PHASE_1;
                    case PHASE_3 -> FightStage.PHASE_2;
                    default -> FightStage.NONE;
                };

                if (previousStage != FightStage.NONE && DraculaModel.RENDER_STAGE.get() == null) {
                    try {
                        DraculaModel.RENDER_STAGE.set(previousStage);
                        //noinspection unchecked
                        ((LivingEntityRenderer<Dracula, RenderState, ?>)getRenderer()).submit(renderPassInfo.renderState(), renderPassInfo.poseStack(), renderTasks, renderPassInfo.cameraState());
                    } finally {
                        DraculaModel.RENDER_STAGE.remove();
                    }
                }
            }
        }
    }
}
