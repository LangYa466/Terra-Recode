/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.world;

import net.ccbluex.liquidbounce.injection.backend.ChunkImplKt;
import net.ccbluex.liquidbounce.utils.render.MiniMapRegister;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chunk.class)
public class MixinChunk {

    @Shadow
    @Final
    public int x;

    @Shadow
    @Final
    public int z;

    @Inject(method = "onUnload", at = @At("HEAD"))
    private void injectFillChunk(CallbackInfo ci) {
        MiniMapRegister.INSTANCE.unloadChunk(this.x, this.z);
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void injectFillChunk(PacketBuffer buf, int availableSections, boolean groundUpContinuous, CallbackInfo ci) {
        //noinspection ConstantConditions
        MiniMapRegister.INSTANCE.updateChunk(ChunkImplKt.wrap((Chunk) ((Object) this)));
    }
}
