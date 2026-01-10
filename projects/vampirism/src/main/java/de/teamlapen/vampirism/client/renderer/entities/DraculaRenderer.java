package de.teamlapen.vampirism.client.renderer.entities;

import de.teamlapen.vampirism.client.core.ModEntityRenderStates;
import de.teamlapen.vampirism.client.models.entities.dracula.DraculaPhase1Model;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;

public class DraculaRenderer<RenderState extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<Dracula, RenderState> {

    public DraculaRenderer(EntityRendererProvider.Context context) {
        super(context, new DraculaPhase1Model());
//        withRenderLayer(new ItemInHandGeoLayer<>(this, "Right_Arm", "Left_Arm"));
    }


    @Override
    public void addRenderData(Dracula animatable, @Nullable Void relatedObject, RenderState renderState, float partialTick) {
        var state = animatable.getState();

        renderState.addGeckolibData(ModEntityRenderStates.DRACULA_STAGE, state.stage);

    }
}
