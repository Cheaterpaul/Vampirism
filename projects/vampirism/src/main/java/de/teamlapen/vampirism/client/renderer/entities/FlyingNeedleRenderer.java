package de.teamlapen.vampirism.client.renderer.entities;

import de.teamlapen.vampirism.client.models.entities.dracula.needle.FlyingNeedleModel;
import de.teamlapen.vampirism.common.world.entity.dracula.FlyingNeedleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class FlyingNeedleRenderer<RenderState extends EntityRenderState & GeoRenderState> extends GeoEntityRenderer<FlyingNeedleEntity, RenderState> {

    public FlyingNeedleRenderer(EntityRendererProvider.Context context) {
        super(context, new FlyingNeedleModel());
    }
}
