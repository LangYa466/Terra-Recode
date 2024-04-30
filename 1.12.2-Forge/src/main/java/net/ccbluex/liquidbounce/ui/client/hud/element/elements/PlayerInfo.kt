package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.api.minecraft.util.IResourceLocation
import net.ccbluex.liquidbounce.features.special.Recorder.killCounts
import net.ccbluex.liquidbounce.features.special.Recorder.win
import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.features.special.Recorder
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.Colors
import net.ccbluex.liquidbounce.utils.render.GlowUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.IntegerValue
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*

@ElementInfo("PlayerInfo")
class PlayerInfo(x: Double = 10.0, y: Double = 10.0, scale: Float = 1F) : Element(x, y, scale) {
    private val radiusValue = IntegerValue("Radius",5,0,10)
    private val alphaValue = IntegerValue("Alpha",150,0,255)
    private val dateFormat = SimpleDateFormat("HH:mm:ss")

    override fun drawElement(): Border {
        //绘制背景
        GlowUtils.drawGlowy(0f,0f,120f,40f,radiusValue.get(),Color(0,0,0,alphaValue.get()))

        //绘制信息
        val themeR = Terra.moduleManager.getModule(HUD::class.java).getThemeColorR()
        val themeG = Terra.moduleManager.getModule(HUD::class.java).getThemeColorG()
        val themeB = Terra.moduleManager.getModule(HUD::class.java).getThemeColorB()
        val terraColor = Colors.getTerraRainBow(Color(themeR,themeG,themeB), Color(255,255,255),0.1).rgb
        Fonts.font40.drawString("PlayerInfo",41,2, terraColor)
        Fonts.font35.drawString("Time",41,4 + Fonts.font40.fontHeight, Color.WHITE.rgb)
        Fonts.font35.drawString("Kills",41,4 + Fonts.font35.fontHeight + Fonts.font40.fontHeight, Color.WHITE.rgb)
        Fonts.font35.drawString("Wins",41,4 + Fonts.font35.fontHeight * 2 + Fonts.font40.fontHeight, Color.WHITE.rgb)

        Fonts.font35.drawString(dateFormat.format(Date(System.currentTimeMillis() - Recorder.startTime - 8000L * 3600L)),
            120 - Fonts.font35.getStringWidth(dateFormat.format(Date(System.currentTimeMillis() - Recorder.startTime - 8000L * 3600L))) - 2,
            4 + Fonts.font40.fontHeight,Color.WHITE.rgb)

        Fonts.font35.drawString("$killCounts",120 - Fonts.font35.getStringWidth("$killCounts") - 2,4 + Fonts.font35.fontHeight + Fonts.font40.fontHeight, Color.WHITE.rgb)
        Fonts.font35.drawString("$win",120 - Fonts.font35.getStringWidth("$win") - 2,4 + Fonts.font35.fontHeight * 2 + Fonts.font40.fontHeight, Color.WHITE.rgb)

        //绘制玩家头像
        val playerInfo = mc.netHandler.getPlayerInfo(mc.thePlayer!!.uniqueID)
        if (playerInfo != null) {
            val locationSkin = playerInfo.locationSkin
            drawHead(locationSkin, 36, 36)
        }
        return Border(0f,0f,120f,40f)
    }

    private fun drawHead(skin: IResourceLocation, width: Int, height: Int) {
        mc.textureManager.bindTexture(skin)
        RenderUtils.drawScaledCustomSizeModalRect(2, 2, 8F, 8F, 8, 8, width, height, 64F, 64F)
    }

}