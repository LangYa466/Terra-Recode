/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import me.qingyou.terra.sound.SoundPlayer
import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.features.special.Recorder
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.misc.Transformer.transform
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.network.play.server.SPacketChat
import net.minecraft.network.play.server.SPacketTitle

@ModuleInfo(name = "AutoGG", description = "Automatically send GG after your victory.", category = ModuleCategory.MISC, canEnable = false)
class AutoGG : Module() {
    private val startMsgValue = BoolValue("StartChat", false)
    private val startMsg = TextValue("StartMsg", "@我正在使用${HUD.customClientName.get()}").displayable { startMsgValue.get() }
    private val sendGGValue = BoolValue("GameEndChat", false)
    private val ggMessageValue = TextValue("GGMessage", "[${HUD.customClientName.get()}] GG").displayable { sendGGValue.get() }
    private val sendnoti = BoolValue("SendNoti", true)
    private val ggsound = BoolValue("GGSound", true)
    private val ggsoundmode = ListValue("SoundMode", arrayOf("JiGou", "WoCao"), "JiGou").displayable { ggsound.get() }

    private var winning = false
    private var winverify1 = false
    private var winverify2 = false
    private var gamestarted = false
    private var started1 = false
    private var started2 = false
    override fun onEnable() {
        stateReset()
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        stateReset()
    }

    private fun stateReset() {
        winning = false
        winverify1 = false
        winverify2 = false
        gamestarted = false
        started1 = false
        started2 = false
    }


    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet.unwrap()
        if (packet is SPacketChat) {
            val message = packet.chatComponent.unformattedText

            if (message.contains("恭喜") && !message.contains(":") && message.startsWith("起床战争")) {
                winverify1 = true
            }
            if (message.contains("[起床战争]") && message.contains("赢得了游戏") && message.contains(":")) {
                winverify2 = true
            }
            if (message.contains("游戏开始 ...") && message.startsWith("起床战争")) {
                gamestarted = true
            }
            if (message.contains("你现在是观察者状态. 按E打开菜单.")) {
                winverify2 = true
            }
            if (message.contains("开始倒计时: 1 秒")) {
                started2 = true
            }
        }
        if (packet is SPacketTitle) {
            val title = (packet.message ?: return).unformattedText
            if (title.contains("恭喜")) {
                winverify2 = true
            }
            if (title.contains("你的队伍获胜了")) {
                winverify2 = true
            }
            if (title.contains("VICTORY")) {
                winning = true
            }
            if (title.contains("战斗开始")) {
                started1 = true
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        // if (winverify2) lose()
        if ((winverify1 && winverify2 || winning)) {
            gg2()
        }
        if (gamestarted) {
            total()
        }

    }

    private fun gg2() {
        if (sendnoti.get()) {
            Terra.hud.addNotification(
                Notification("AutoGG", "You won the game! GG!", NotifyType.SUCCESS, 4000, 700)
            )
        }
        if (sendGGValue.get()) {
            mc.thePlayer!!.sendChatMessage(transform(ggMessageValue.get()))
        }
        stateReset()
        if (ggsound.get()) {
            when (ggsoundmode.get().toLowerCase()) {
                "jigou" -> SoundPlayer().playSound(SoundPlayer.SoundType.JIGOU, Terra.moduleManager.toggleVolume)
                "wocao" -> SoundPlayer().playSound(SoundPlayer.SoundType.WOCAO, Terra.moduleManager.toggleVolume)
            }
        }
        Recorder.win++
    }

    private fun total() {
        if (startMsgValue.get()) {
            mc.thePlayer!!.sendChatMessage(transform(startMsg.get()))
        }
        Recorder.totalPlayed++
        stateReset()

    }


    override fun handleEvents() = true
}