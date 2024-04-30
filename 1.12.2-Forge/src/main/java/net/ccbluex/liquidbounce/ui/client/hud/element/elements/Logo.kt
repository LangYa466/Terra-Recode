package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.Colors
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.render.GlowUtils
import net.ccbluex.liquidbounce.value.IntegerValue
import java.awt.Color

@ElementInfo(name = "Logo")
class Logo(x: Double = 6.0, y: Double = 10.0, scale: Float = 1F, side: Side = Side.default()) : Element(x, y, scale, side) {

    private val infoAlphaValue = IntegerValue("LogoAlpha",150,0,255)
    private val radiusValue = IntegerValue("Radius", 5, 0, 7)

    override fun drawElement(): Border {
        val themeR = Terra.moduleManager.getModule(HUD::class.java).getThemeColorR()
        val themeG = Terra.moduleManager.getModule(HUD::class.java).getThemeColorG()
        val themeB = Terra.moduleManager.getModule(HUD::class.java).getThemeColorB()
        val terraColor = Colors.getTerraRainBow(Color(themeR,themeG,themeB), Color(255,255,255),0.1).rgb

        val clientName = HUD.customClientName.get()
        val text = " | ${mc.session.username} | Ping: ${EntityUtils.getPing(mc.thePlayer)} | FPS: ${mc.debugFPS} | ${Text.HOUR_FORMAT.format(System.currentTimeMillis())}"

        GlowUtils.drawGlowy(0f, 0f, 15f + Fonts.rise35.getStringWidth(clientName) + Fonts.rise35.getStringWidth(text), Fonts.rise35.fontHeight.toFloat() * 1.2F , radiusValue.get(),Color(0,0,0,infoAlphaValue.get()))

        Fonts.rise35.drawString(clientName, 8f, 2F, terraColor)
        Fonts.rise35.drawString(text, 8f + Fonts.rise35.getStringWidth(clientName), 2F, Color.WHITE.rgb)

        return Border(0F, 0F, 15f + Fonts.rise35.getStringWidth(clientName) + Fonts.rise35.getStringWidth(text), Fonts.rise35.fontHeight.toFloat() * 1.2F)
    }
}