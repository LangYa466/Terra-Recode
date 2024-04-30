/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.utils.ClientUtils.displayChatMessage
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.network.play.server.SPacketAnimation
import net.minecraft.network.play.server.SPacketPlayerPosLook

@ModuleInfo(name = "Alert", description = "_RyF is Gay", category = ModuleCategory.MISC)
class Alert : Module() {
    private val flagAlert = BoolValue("FlagAlert", true)
    private val critAlert = BoolValue("CritAlert", true)
    private val showCritTarget = BoolValue("ShowCritTarget", true).displayable { critAlert.get() }
    private val flagAutoHub = BoolValue("FlagAutoHub",false)
    private val amountValue = IntegerValue("Amount",5,1,20).displayable { flagAutoHub.get() }

    private var a = 0

    @EventTarget
    fun onWorld(event: WorldEvent) {
        a = 0
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet.unwrap()
        if (packet is SPacketPlayerPosLook && packet.flags.isEmpty() && !mc2.isSingleplayer) {
            if (flagAlert.get()) { displayChatMessage("FLAG") }
            a++
        }
        val target = Terra.combatManager.target ?: return
        if (packet is SPacketAnimation && critAlert.get()) {
            if (packet.animationType == 4 && packet.entityID == target.entityId) {
                displayChatMessage("${if (showCritTarget.get()) "§aApply " else ""}§rCRIT ${if (showCritTarget.get()) "§7-> §6${target.name}" else ""}")
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (a >= amountValue.get() && flagAutoHub.get()) {
            mc.thePlayer!!.sendChatMessage("/hub")
            displayChatMessage("Flag过多已自动HUB")
            a = 0
        }
    }

    init {
        state = true
        array = false
    }
}