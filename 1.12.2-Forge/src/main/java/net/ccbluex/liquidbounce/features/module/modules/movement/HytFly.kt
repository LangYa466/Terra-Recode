/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.exploit.Disabler
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.server.SPacketConfirmTransaction
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketPlayerAbilities
import java.util.concurrent.LinkedBlockingQueue

@ModuleInfo("HytFly", "Auto fly when you respawn in BedWars modes.", ModuleCategory.MOVEMENT)
class HytFly : Module() {
    val autoFlyValue = BoolValue("AutoFly", true)
    var canFly = false
    private val s32s = LinkedBlockingQueue<SPacketConfirmTransaction>()
    private var playerS14 : SPacketEntityVelocity? = null
    private var disInt = 0

    override fun onEnable() {
        if (autoFlyValue.get()) Terra.moduleManager.getModule(Disabler::class.java).state = false
        canFly = false
        disInt = 0
    }

    override fun onDisable() {
        if (autoFlyValue.get()) {
            Terra.moduleManager.getModule(Disabler::class.java).state = true
            Terra.moduleManager.getModule(Fly::class.java).state = false
        }
        while (s32s.isNotEmpty()) mc2.connection!!.handleConfirmTransaction(s32s.take())
        if (playerS14 != null) mc2.connection!!.handleEntityVelocity(playerS14)
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        val packet = event.packet.unwrap()
        if (packet is SPacketPlayerAbilities) canFly = true
        if (canFly) {
            if (packet is SPacketConfirmTransaction) {
                event.cancelEvent()
                s32s.add(packet)
            }

            if (packet is SPacketEntityVelocity && packet.entityID == mc2.player.entityId) {
                event.cancelEvent()
                playerS14 = packet
            }
        }
    }
}