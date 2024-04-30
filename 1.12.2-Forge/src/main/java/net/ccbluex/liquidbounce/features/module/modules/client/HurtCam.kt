/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.server.SPacketEntityStatus
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import java.awt.Color

@ModuleInfo(name = "HurtCam", description = "HurtCam Settings", category = ModuleCategory.CLIENT, canEnable = false)
class HurtCam : Module() {
    companion object {
        @JvmStatic
        val modeValue = ListValue("Mode", arrayOf("Vanilla", "Cancel", "FPS", "LB+"), "Vanilla")
        private val colorRedValue = IntegerValue("R", 255, 0, 255).displayable { modeValue.get().equals("FPS",true) }
        private val colorGreenValue = IntegerValue("G", 0, 0, 255).displayable { modeValue.get().equals("FPS",true) }
        private val colorBlueValue = IntegerValue("B", 0, 0, 255).displayable { modeValue.get().equals("FPS",true) }
        private val colorRainbow = BoolValue("Rainbow", false).displayable { modeValue.get().equals("FPS",true) }
        private val timeValue = IntegerValue("FPSTime", 1000, 0, 1500).displayable { modeValue.get().equals("FPS",true) }
        private val fpsHeightValue = IntegerValue("FPSHeight", 25, 10, 50).displayable { modeValue.get().equals("FPS",true) }

        private var hurt = 0L
        private var alpha = 0
    }

    @EventTarget(ignoreCondition = true)
    fun onRender2D(event: Render2DEvent) {
        val sr = ScaledResolution(mc2)
        val width = sr.scaledWidth_double
        val height = sr.scaledHeight_double
        if (modeValue.get() == "FPS") {
            if (hurt == 0L) return

            val passedTime = System.currentTimeMillis() - hurt
            if (passedTime > timeValue.get()) {
                hurt = 0L
                return
            }

            val color = getColor((((timeValue.get() - passedTime) / timeValue.get().toFloat()) * 255).toInt())
            val color1 = getColor(0)

            RenderUtils.drawGradientSidewaysV(0.0, 0.0, width, fpsHeightValue.get().toDouble(), color.rgb, color1.rgb)
            RenderUtils.drawGradientSidewaysV(0.0, height - fpsHeightValue.get(), width, height, color1.rgb, color.rgb)
        }
        if (modeValue.get() == "LB+") {
            if (mc.thePlayer!!.hurtTime > 0) {
                if (alpha < 100) {
                    alpha += 5
                }
            } else {
                if (alpha > 0) {
                    alpha -= 5
                }
            }
            RenderUtils.drawGradientSidewaysV2(
                0.0, 0.0, width, 25.0, Color(255, 0, 0, 0).rgb,
                Color(255, 0, 0, alpha).rgb
            )
            RenderUtils.drawGradientSidewaysV2(
                0.0, height - 25, width, height,
                Color(255, 0, 0, alpha).rgb, Color(255, 0, 0, 0).rgb
            )
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet.unwrap()

        when (modeValue.get().toLowerCase()) {
            "fps" -> {
                if (packet is SPacketEntityStatus) {
                    if (packet.opCode.toInt() == 2 && mc2.player.equals(packet.getEntity(mc2.world))) {
                        hurt = System.currentTimeMillis()
                    }
                }
            }
        }
    }


    private fun getColor(alpha: Int): Color {
        return if (colorRainbow.get()) ColorUtils.reAlpha(ColorUtils.rainbow(), alpha) else Color(
            colorRedValue.get(),
            colorGreenValue.get(),
            colorBlueValue.get(),
            alpha
        )
    }

    // always handle event
    override fun handleEvents() = true
}
