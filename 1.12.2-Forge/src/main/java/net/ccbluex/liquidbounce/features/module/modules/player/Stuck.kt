package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.EnumHand

@ModuleInfo(name = "Stuck", description = "OMG.", category = ModuleCategory.PLAYER)
class Stuck : Module() {
    var a = false

    override fun onEnable() {
        a = true
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc2.player.motionX = 0.0
        mc2.player.motionY = 0.0
        mc2.player.motionZ = 0.0
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet.unwrap()

        if (packet is CPacketPlayer && a) event.cancelEvent()
        if (packet is SPacketPlayerPosLook) this.state = false
        if (packet is CPacketPlayerTryUseItem && a) {
            a = false
            event.cancelEvent()
            mc2.connection!!.sendPacket(
                CPacketPlayer.Rotation(
                    mc2.player.rotationYaw,
                    mc2.player.rotationPitch,
                    mc2.player.onGround
                )
            )
            mc2.connection!!.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
        }

    }
}