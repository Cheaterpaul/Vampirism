package de.teamlapen.vampirism.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.world.VampirismWorld;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow @Nullable private ClientLevel level;

    private static final ResourceLocation BLOOD_MOON_LOCATION = new ResourceLocation(REFERENCE.MODID, "textures/environment/blood_moon_phases.png");

    @Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V", ordinal = 1, shift = At.Shift.AFTER))
    private void chooseBloodMoonTexture(PoseStack stack, Matrix4f matrix4f, float p_202426_, Camera camera, boolean p_202428_, Runnable p_202429_, CallbackInfo ci) {
        var world = VampirismWorld.get(this.level);

        if (world.isBloodMoon()) {
            RenderSystem.setShaderTexture(0, BLOOD_MOON_LOCATION);
        }
    }
}
