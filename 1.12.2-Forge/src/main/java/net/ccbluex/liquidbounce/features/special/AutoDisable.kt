package net.ccbluex.liquidbounce.features.special

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.features.module.EnumTriggerType
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.MinecraftInstance.classProvider

object AutoDisable : Listenable {
    private const val name = "AutoDisable"

    @EventTarget
    fun onWorld(event: WorldEvent) {
        Terra.moduleManager.modules
                .filter { it.state && it.autoDisable == EnumAutoDisableType.RESPAWN && it.triggerType == EnumTriggerType.TOGGLE }
                .forEach { module ->
                    module.state = false
                    Terra.hud.addNotification(Notification(this.name, "Disabled ${module.name} due world changed.", NotifyType.WARNING, 2000))
                }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (classProvider.isSPacketPlayerPosLook(packet)) {
            Terra.moduleManager.modules
                    .filter { it.state && it.autoDisable == EnumAutoDisableType.FLAG && it.triggerType == EnumTriggerType.TOGGLE }
                    .forEach { module ->
                        module.state = false
                        Terra.hud.addNotification(Notification(this.name, "Disabled ${module.name} due flags", NotifyType.WARNING, 2000))
                    }
        }
    }

    fun handleGameEnd() {
        Terra.moduleManager.modules
                .filter { it.state && it.autoDisable == EnumAutoDisableType.GAME_END }
                .forEach { module ->
                    module.state = false
                    Terra.hud.addNotification(Notification(this.name, "Disabled ${module.name} due to game end", NotifyType.WARNING, 2000))
                }
    }

    override fun handleEvents() = true
}
