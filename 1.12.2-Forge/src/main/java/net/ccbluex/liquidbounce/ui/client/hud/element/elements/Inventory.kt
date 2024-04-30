/*
 * Terra Hacked Client
 */

package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.GlowUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RoundedUtils
import net.ccbluex.liquidbounce.utils.render.ShadowRenderUtils
import net.ccbluex.liquidbounce.value.IntegerValue
import java.awt.Color

/**
 * CustomHUD Model element
 *
 * Draw mini figure of your character to the HUD
 */
@ElementInfo(name = "Inventory")
class Inventory(x: Double = 300.0, y: Double = 50.0) : Element(x, y) {
    val r = IntegerValue("Red", 0, 0, 255)
    val g = IntegerValue("Green", 0, 0, 255)
    val b = IntegerValue("Blue", 0, 0, 255)
    val alpha = IntegerValue("BG-Alpha", 150, 0, 255)
    private val radiusValue = IntegerValue("Radius", 5, 0, 10)

    /**
     * Draw element
     */
    fun shader() {
        val startY = -12.0
        RenderUtils.drawRoundedRect2(0.4F, startY.toFloat(), 174F, 76.5F, 4f, Color(0, 0, 0).rgb)
    }

    override fun drawElement(): Border {
        val startY = -12.0
        GlowUtils.drawGlowy(0.4F, startY.toFloat(), 174F, 76.5F, radiusValue.get(), Color(r.get(), g.get(), b.get(), alpha.get()))

        //RoundedUtils.drawRound(0F, startY.toFloat() - 0.5f, 174F, 13F, 4f, Color(250, 250, 250, 250))
        Fonts.font35.drawString("Inventory", 5F, (startY + 7.6F).toFloat() - 4, Color(255, 255, 255).rgb)

        // render item
        RenderHelper.enableGUIStandardItemLighting()
        renderInv(9, 17, 6, 6)
        renderInv(18, 26, 6, 24)
        renderInv(27, 35, 6, 42)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.enableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.disableLighting()
        return Border(0F, startY.toFloat(), 174F, 66F)
    }

    private fun renderInv(slot: Int, endSlot: Int, x: Int, y: Int) {
        var xOffset = x
        for (i in slot..endSlot) {
            xOffset += 18
            val stack = mc.thePlayer!!.inventoryContainer.getSlot(i).stack ?: continue

            mc.renderItem.renderItemAndEffectIntoGUI(stack, xOffset - 18, y)
            mc.renderItem.renderItemOverlays(
                Fonts.posterama30,
                stack,
                xOffset - 18,
                y
            )
        }
    }
}