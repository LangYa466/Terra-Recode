package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.Terra.CLIENT_NAME
import net.ccbluex.liquidbounce.Terra.CLIENT_VERSION
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.features.special.Recorder.hour
import net.ccbluex.liquidbounce.features.special.Recorder.minute
import net.ccbluex.liquidbounce.features.special.Recorder.second
import net.ccbluex.liquidbounce.features.special.Recorder.tick
import net.ccbluex.liquidbounce.utils.misc.Transformer.getPlayTime
import net.ccbluex.liquidbounce.value.TextValue
import org.lwjgl.opengl.Display.setTitle

@ModuleInfo(name = "Title", description = "Change the client's title", category = ModuleCategory.MISC)
class Title : Module() {
    private val mainTitle = TextValue("FirstTitle", HUD.customClientName.get())
    private val midTitle = TextValue("MiddleTitle", "宁世鑫意念后门客户端 你的电脑已被植入Skyrim意念后门")

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        tick += 1
        if (tick == 20) {
            second += 1
            tick = 0
        }
        if (second == 60) {
            minute += 1
            second = 0
        }
        if (minute == 60) {
            hour += 1
            minute = 0
        }

        if (this.state) setTitle("${mainTitle.get()} | ${midTitle.get()} | Time has passed: ${getPlayTime(true)}") else setTitle(
                "$CLIENT_NAME b$CLIENT_VERSION"
        )
    }

    override fun handleEvents() = true

    init {
        state = true
        array = false
    }

}