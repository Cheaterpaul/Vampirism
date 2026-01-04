package de.teamlapen.vampirism.client.renderer.entities;

import de.teamlapen.vampirism.client.core.ModEntityRenderStates;
import de.teamlapen.vampirism.client.models.entities.dracula.DraculaPhase1Model;
import de.teamlapen.vampirism.common.world.entity.dracula.Dracula;
import de.teamlapen.vampirism.common.world.entity.dracula.FightStage;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class DraculaRenderer<RenderState extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<Dracula, RenderState> {

    public DraculaRenderer(EntityRendererProvider.Context context) {
        super(context, new DraculaPhase1Model());
    }

    @Override
    public void addRenderData(Dracula animatable, @Nullable Void relatedObject, RenderState renderState, float partialTick) {
        FightStage fightStage = animatable.getStage();

        renderState.addGeckolibData(ModEntityRenderStates.DRACULA_STAGE, fightStage);

    }
}
