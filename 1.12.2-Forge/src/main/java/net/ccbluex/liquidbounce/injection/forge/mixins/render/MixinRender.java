/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.Terra;
import net.ccbluex.liquidbounce.event.RenderEntityEvent;
import net.ccbluex.liquidbounce.injection.backend.EntityImplKt;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Render.class)
public abstract class MixinRender {
    @Shadow
    protected abstract <T extends Entity> boolean bindEntityTexture(T entity);

    @Inject(method = "doRender", at = @At("HEAD"))
    private void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo callbackInfo) {
        Terra.eventManager.callEvent(new RenderEntityEvent(EntityImplKt.wrap(entity), x, y, z, entityYaw, partialTicks));
    }
}
