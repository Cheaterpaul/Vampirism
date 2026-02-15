package de.teamlapen.vampirism.client.renderer.entities;

import de.teamlapen.vampirism.common.world.entity.dracula.FlyingNeedleEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class FlyingNeedleRenderer extends EntityRenderer<FlyingNeedleEntity, FlyingNeedleRenderer.FlyingNeedleRenderState> {

    public FlyingNeedleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public FlyingNeedleRenderState createRenderState() {
        return new FlyingNeedleRenderState();
    }

    public static class FlyingNeedleRenderState extends EntityRenderState {
    }
}
