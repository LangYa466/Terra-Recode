/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.ccbluex.liquidbounce.Terra;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.Animations;
import net.ccbluex.liquidbounce.features.module.modules.client.SilentDisconnect;
import net.ccbluex.liquidbounce.features.module.modules.exploit.Disabler;
import net.ccbluex.liquidbounce.injection.backend.PacketImplKt;
import net.minecraft.client.Minecraft;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.server.SPacketCustomPayload;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {
    @Shadow
    private Channel channel;
    @Shadow
    private INetHandler packetListener;

    /**
     * @author 1mC0w#1337
     * @reason Fix the GrimAC Post VL
     */
    @Overwrite
    protected void channelRead0(ChannelHandlerContext p_channelRead0_1_, Packet<?> p_channelRead0_2_) {
        if (this.channel.isOpen()) {
            try {
                Packet<INetHandler> packet = (Packet<INetHandler>) p_channelRead0_2_;
                Disabler disabler2 = Terra.moduleManager.getModule(Disabler.class);
                if (p_channelRead0_2_ instanceof SPacketCustomPayload) {
                    final PacketEvent event = new PacketEvent(PacketImplKt.wrap(p_channelRead0_2_));
                    Terra.eventManager.callEvent(event);
                    if (event.isCancelled()) {
                        return;
                    }
                    packet.processPacket(this.packetListener);

                } else if (disabler2.getGrimPost() && disabler2.grimPostDelay(p_channelRead0_2_)) {
                    Minecraft.getMinecraft().addScheduledTask(() -> Disabler.getStoredPackets().add(packet));
                } else {
                    final PacketEvent event = new PacketEvent(PacketImplKt.wrap(p_channelRead0_2_));
                    Terra.eventManager.callEvent(event);
                    if (event.isCancelled()) {
                        return;
                    } packet.processPacket(this.packetListener);
                }
            } catch (ThreadQuickExitException ex) {
            }
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void send(Packet<?> packet, CallbackInfo callback) {
        final PacketEvent event = new PacketEvent(PacketImplKt.wrap(packet));
        Terra.eventManager.callEvent(event);

        if (event.isCancelled())
            callback.cancel();
    }

    /**
     * show player head in tab bar
     */
    @Inject(method = "isEncrypted", at = @At("HEAD"), cancellable = true)
    private void getIsencrypted(CallbackInfoReturnable<Boolean> cir) {
        if (Animations.INSTANCE.getFlagRenderTabOverlay()) {
            cir.setReturnValue(true);
        }
    }

    @Redirect(method = "checkDisconnected", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;)V"))
    public void checkDisconnectedLoggerWarn(Logger instance, String s) {
        if (!Terra.moduleManager.getModule(SilentDisconnect.class).getState()) {
            instance.warn(s); // it will spam "handleDisconnection() called twice" in console if SilentDisconnect is enabled
        }
    }

}