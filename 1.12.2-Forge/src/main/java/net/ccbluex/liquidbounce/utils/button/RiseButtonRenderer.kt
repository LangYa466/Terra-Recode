package net.ccbluex.liquidbounce.utils.button

import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import org.lwjgl.opengl.GL11

class RiseButtonRenderer(button: GuiButton) : AbstractButtonRenderer(button) {
    override fun render(mouseX: Int, mouseY: Int, mc: Minecraft) {
        val startX = button.x.toFloat()
        val endX = button.x + button.width.toFloat()
        val endY = button.y + button.height.toFloat()
        if (button.enabled) {
            GL11.glEnable(3042)
            GL11.glDisable(3553)
            GL11.glBlendFunc(770, 771)
            GL11.glEnable(2848)
            GL11.glShadeModel(7425)
            for (i in button.x..button.x + button.width step 1) {
                RenderUtils.quickDrawGradientSidewaysH(
                    i.toDouble(), endY - 1.0, i + 1.0, endY.toDouble(),
                    ColorUtils.hslRainbow(i, indexOffset = 10).rgb, ColorUtils.hslRainbow(i + 1, indexOffset = 10).rgb
                )
            }
            GL11.glEnable(3553)
            GL11.glDisable(3042)
            GL11.glDisable(2848)
            GL11.glShadeModel(7424)
            GL11.glColor4f(1f, 1f, 1f, 1f)
        }
    }
}