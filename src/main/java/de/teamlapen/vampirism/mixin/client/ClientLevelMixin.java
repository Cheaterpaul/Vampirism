package de.teamlapen.vampirism.mixin.client;

import de.teamlapen.vampirism.world.VampirismWorld;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {


    @Deprecated
    private ClientLevelMixin(WritableLevelData p_220352_, ResourceKey<Level> p_220353_, Holder<DimensionType> p_220354_, Supplier<ProfilerFiller> p_220355_, boolean p_220356_, boolean p_220357_, long p_220358_, int p_220359_) {
        super(p_220352_, p_220353_, p_220354_, p_220355_, p_220356_, p_220357_, p_220358_, p_220359_);
    }

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void modifyBloodMoonColor(Vec3 p_171661_, float p_171662_, CallbackInfoReturnable<Vec3> cir) {
        VampirismWorld world = VampirismWorld.get(this);
        if (world.isBloodMoon()) {
            Vec3 color = cir.getReturnValue();
            Vec3 red = new Vec3(0.5, 0, 0).scale(world.bloodMoonProgress());
            cir.setReturnValue(color.add(red).scale(0.5));
        }
    }
}
