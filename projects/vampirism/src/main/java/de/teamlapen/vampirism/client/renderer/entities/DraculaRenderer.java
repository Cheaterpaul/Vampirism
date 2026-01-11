package de.teamlapen.vampirism.client.renderer.entities;

import de.teamlapen.vampirism.client.core.ModEntityRenderStates;
import de.teamlapen.vampirism.client.models.entities.dracula.DraculaPhase1Model;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.FightStage;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
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
        super(context, new DraculaPhase1Model());
        withRenderLayer(new FormRenderLayer(FightStage.PHASE_2, new ItemInHandGeoLayer<>(this, "right_arm_inner", "left_arm_inner")));
    }


    @Override
    public void addRenderData(Dracula animatable, @Nullable Void relatedObject, RenderState renderState, float partialTick) {
        var state = animatable.getState();

        renderState.addGeckolibData(ModEntityRenderStates.DRACULA_STAGE, state.stage);

    }

    public Map<FightStage, List<GeoRenderLayer<Dracula, Void, RenderState>>> getLayers() {
        return layers;
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

        @Override
        protected Identifier getTextureResource(RenderState renderState) {
            return this.renderer.getTextureLocation(renderState);
        }
    }
}
