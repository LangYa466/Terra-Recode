package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.features.special.Recorder
import net.ccbluex.liquidbounce.features.special.Recorder.killCounts
import net.ccbluex.liquidbounce.features.special.Recorder.win
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.render.GlowUtils
import net.ccbluex.liquidbounce.value.FontValue
import net.ccbluex.liquidbounce.value.IntegerValue
import java.awt.Color
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@ElementInfo(name = "GameInfo")
class GameInfo(x: Double = 10.0, y: Double = 10.0, scale: Float = 1F) : Element(x, y, scale) {
    private val alphaValue = IntegerValue("Alpha", 150, 0, 255)
    private val radiusValue = IntegerValue("Radius", 5, 0, 10)
    private var fontValue = FontValue("Font", Fonts.font35)
    private val DATE_FORMAT = SimpleDateFormat("HH:mm:ss")

    override fun drawElement(): Border {
        val borderX1 = 0
        val borderY1 = 0
        var borderX2 = 0
        var borderY2 = 0

        val font = Fonts.font30
        borderX2 += 150
        borderY2 += 3 + Fonts.font40.fontHeight + font.fontHeight * 3 + 30

        var hm = 0
        var s = 0
        var m = 0
        var h = 0
        hm += 1

        if (hm == 120) {
            s += 1
            hm = 0
        }
        if (s == 60) {
            m += 1
            s = 0
        }
        if (m == 60) {
            h += 1
            m = 0
        }

        val color = Color.WHITE.rgb
        val fontHeight = Fonts.font30.fontHeight
        val format = DecimalFormat("#.##")

        //VisualUtils.drawCircleRect(0F, 0F, 150F, 3F + fontHeight + font.fontHeight * 3 + 30F, radiusValue.get().toFloat(), Color(0, 0, 0, alphaValue.get()).rgb)
        GlowUtils.drawGlowy(0F,0F,150F,3F + fontHeight + font.fontHeight * 3 + 30F,radiusValue.get(),Color(0,0,0,alphaValue.get()))
        // title
        fontValue.get().drawString("Game Info", 5F, 3F, color)
        fontValue.get().drawString("Played Time", 7F, 3F + fontHeight + 5F, color)
        fontValue.get().drawString("Speed", 7F, 3F + fontHeight + font.fontHeight + 10F, color)
        fontValue.get().drawString("Wins", 7F, 3F + fontHeight + font.fontHeight * 2 + 15F, color)
        fontValue.get().drawString("Kills", 7F, 3F + fontHeight + font.fontHeight * 3 + 20F, color)


        //play time
        fontValue.get().drawString(DATE_FORMAT.format(Date(System.currentTimeMillis() - Recorder.startTime - 8000L * 3600L)),
            150 - fontValue.get().getStringWidth(DATE_FORMAT.format(Date(System.currentTimeMillis() - Recorder.startTime - 8000L * 3600L))) - 5F, 3F + fontHeight + 5F, color)

        //bps
        fontValue.get().drawString(
            format.format(MovementUtils.getBlockSpeed(mc2.player)),
            150 - fontValue.get().getStringWidth(format.format(MovementUtils.getBlockSpeed(mc2.player))) - 5F, 3F + fontHeight + font.fontHeight + 10F, color)

        //wins
        fontValue.get().drawString("$win", 150 - fontValue.get().getStringWidth("$win") - 5F, 3F + fontHeight + font.fontHeight * 2 + 15F, color)

        //kills
        fontValue.get().drawString("$killCounts", 150 - fontValue.get().getStringWidth("$killCounts") - 5F, 3F + fontHeight + font.fontHeight * 3 + 20F, color)

        return Border(borderX1.toFloat(), borderY1.toFloat(), borderX2.toFloat(), borderY2.toFloat())
    }

}