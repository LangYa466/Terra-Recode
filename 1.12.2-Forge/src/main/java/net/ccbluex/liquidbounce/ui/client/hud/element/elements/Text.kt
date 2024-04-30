/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.CPSCounter
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.ServerUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowFontShader
import net.ccbluex.liquidbounce.value.*
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import kotlin.math.sqrt

/**
 * CustomHUD text element
 *
 * Allows to draw custom text
 */
@ElementInfo(name = "Text")
class Text(x: Double = 6.0, y: Double = 10.0, scale: Float = 1F,
           side: Side = Side.default()) : Element(x, y, scale, side) {

    companion object {

        val DATE_FORMAT = SimpleDateFormat("MMddyy")
        val HOUR_FORMAT = SimpleDateFormat("HH:mm")
        val Y_FORMAT = DecimalFormat("0.000000000")
        val DECIMAL_FORMAT = DecimalFormat("0.00")

    }

    private val displayString = TextValue("DisplayText", "Text Element")
    private val redValue = IntegerValue("Red", 255, 0, 255)
    private val greenValue = IntegerValue("Green", 255, 0, 255)
    private val blueValue = IntegerValue("Blue", 255, 0, 255)
    private val rainbow = BoolValue("Rainbow", false)
    private val rainbowX = FloatValue("Rainbow-X", -1000F, -2000F, 2000F)
    private val rainbowY = FloatValue("Rainbow-Y", -1000F, -2000F, 2000F)
    private val shadow = BoolValue("Shadow", true)
    private val rect = BoolValue("Rect", false)

    private val op = BoolValue("OneTapRect", false)
    private val opRedValue1 = IntegerValue("OP-R1",0,0,255)
    private val opGreenValue1 = IntegerValue("OP-G1",255,0,255)
    private val opBlueValue1 = IntegerValue("OP-B1",255,0,255)
    private val opRedValue2 = IntegerValue("OP-R2",0,0,255)
    private val opGreenValue2 = IntegerValue("OP-G2",160,0,255)
    private val opBlueValue2 = IntegerValue("OP-B2",255,0,255)

    private val only = BoolValue("OnlyWhtie", false)
    private var fontValue = FontValue("Font", Fonts.font35)

    private var editMode = false
    private var editTicks = 0
    private var prevClick = 0L

    private var displayText = display

    private val display: String
        get() {
            val textContent = if (displayString.get().isEmpty() && !editMode)
                "Text Element"
            else
                displayString.get()


            return multiReplace(textContent)
        }

    private fun getReplacement(str: String): String? {
        val thePlayer = mc.thePlayer

        if (thePlayer != null) {
            when (str) {
                "x" -> return DECIMAL_FORMAT.format(thePlayer.posX)
                "y" -> return DECIMAL_FORMAT.format(thePlayer.posY)
                "z" -> return DECIMAL_FORMAT.format(thePlayer.posZ)
                "xdp" -> return thePlayer.posX.toString()
                "ydp" -> return thePlayer.posY.toString()
                "zdp" -> return thePlayer.posZ.toString()
                "velocity" -> return DECIMAL_FORMAT.format(sqrt(thePlayer.motionX * thePlayer.motionX + thePlayer.motionZ * thePlayer.motionZ))
                "ping" -> return EntityUtils.getPing(thePlayer).toString()
                "0" -> return "§0"
                "1" -> return "§1"
                "2" -> return "§2"
                "3" -> return "§3"
                "4" -> return "§4"
                "5" -> return "§5"
                "6" -> return "§6"
                "7" -> return "§7"
                "8" -> return "§8"
                "9" -> return "§9"
                "a" -> return "§a"
                "b" -> return "§b"
                "c" -> return "§c"
                "d" -> return "§d"
                "e" -> return "§e"
                "f" -> return "§f"
                "n" -> return "§n"
                "m" -> return "§m"
                "l" -> return "§l"
                "k" -> return "§k"
                "o" -> return "§o"
                "r" -> return "§r"
            }
        }

        return when (str) {
            "username" -> mc.session.username
            "clientname" -> Terra.CLIENT_NAME
            "clientversion" -> "b${Terra.CLIENT_VERSION}"
            "clientcreator" -> Terra.CLIENT_CREATOR
            "fps" -> mc.debugFPS.toString()
            "date" -> DATE_FORMAT.format(System.currentTimeMillis())
            "time" -> HOUR_FORMAT.format(System.currentTimeMillis())
            "serverip" -> ServerUtils.getRemoteIp()
            "cps", "lcps" -> return CPSCounter.getCPS(CPSCounter.MouseButton.LEFT).toString()
            "mcps" -> return CPSCounter.getCPS(CPSCounter.MouseButton.MIDDLE).toString()
            "rcps" -> return CPSCounter.getCPS(CPSCounter.MouseButton.RIGHT).toString()

            else -> null // Null = don't replace
        }
    }

    private fun multiReplace(str: String): String {
        var lastPercent = -1
        val result = StringBuilder()
        for (i in str.indices) {
            if (str[i] == '%') {
                if (lastPercent != -1) {
                    if (lastPercent + 1 != i) {
                        val replacement = getReplacement(str.substring(lastPercent + 1, i))

                        if (replacement != null) {
                            result.append(replacement)
                            lastPercent = -1
                            continue
                        }
                    }
                    result.append(str, lastPercent, i)
                }
                lastPercent = i
            } else if (lastPercent == -1) {
                result.append(str[i])
            }
        }

        if (lastPercent != -1) {
            result.append(str, lastPercent, str.length)
        }

        return result.toString()
    }

    /**
     * Draw element
     */
    override fun drawElement(): Border? {
        val color = Color(redValue.get(), greenValue.get(), blueValue.get()).rgb
        val opColor = Color(opRedValue1.get(),opGreenValue1.get(),opBlueValue1.get(),255).rgb+Color(opRedValue2.get(),opGreenValue2.get(),opBlueValue2.get(),255).rgb
        val fontRenderer = fontValue.get()
        if(this.rect.get())
            RenderUtils.drawRect(-2f,-2f,(fontRenderer.getStringWidth(displayText)+1).toFloat(),fontRenderer.fontHeight.toFloat(),Color(0,0,0,150).rgb)
        fontRenderer.drawString(displayText, 0F, 0F, if (rainbow.get())
            ColorUtils.rainbow(400000000L).rgb else if (only.get()) -1 else color, shadow.get())
        if(this.op.get()){
            RenderUtils.drawRect(-4.0f, -8.0f, (fontRenderer.getStringWidth(displayText) + 3).toFloat(), fontRenderer.fontHeight.toFloat(), Color(0,0,0,150).rgb)
            RenderUtils.drawGradientSideways(-3.0, -7.0, (fontRenderer.getStringWidth(displayText) +2.0).toDouble(), -3.0, if (rainbow.get()) ColorUtils.rainbow(400000000L).rgb+Color(0,0,0,150).rgb
            else opColor, if (rainbow.get()) ColorUtils.rainbow(400000000L).rgb else color)
        }
        val rainbow = rainbow.get()
        RainbowFontShader.begin(rainbow, if (rainbowX.get() == 0.0F) 0.0F else 1.0F / rainbowX.get(), if (rainbowY.get() == 0.0F) 0.0F else 1.0F / rainbowY.get(), System.currentTimeMillis() % 10000 / 10000F).use {
            fontRenderer.drawString(displayText, 0F, 0F, if (rainbow)
                0 else color, shadow.get())

            if (editMode && classProvider.isGuiHudDesigner(mc.currentScreen) && editTicks <= 40)
                fontRenderer.drawString("_", fontRenderer.getStringWidth(displayText) + 2F,
                        0F, if (rainbow) ColorUtils.rainbow(400000000L).rgb else color, shadow.get())
        }

        if (editMode && !classProvider.isGuiHudDesigner(mc.currentScreen)) {
            editMode = false
            updateElement()
        }

        return Border(
                -2F,
                -2F,
                fontRenderer.getStringWidth(displayText) + 2F,
                fontRenderer.fontHeight.toFloat()
        )
    }

    override fun updateElement() {
        editTicks += 5
        if (editTicks > 80) editTicks = 0

        displayText = if (editMode) displayString.get() else display
    }

    override fun handleMouseClick(x: Double, y: Double, mouseButton: Int) {
        if (isInBorder(x, y) && mouseButton == 0) {
            if (System.currentTimeMillis() - prevClick <= 250L)
                editMode = true

            prevClick = System.currentTimeMillis()
        } else {
            editMode = false
        }
    }

    override fun handleKey(c: Char, keyCode: Int) {
        if (editMode && classProvider.isGuiHudDesigner(mc.currentScreen)) {
            if (keyCode == Keyboard.KEY_BACK) {
                if (displayString.get().isNotEmpty())
                    displayString.set(displayString.get().substring(0, displayString.get().length - 1))

                updateElement()
                return
            }

            if (ColorUtils.isAllowedCharacter(c) || c == '§')
                displayString.set(displayString.get() + c)

            updateElement()
        }
    }

    fun setColor(c: Color): Text {
        redValue.set(c.red)
        greenValue.set(c.green)
        blueValue.set(c.blue)
        return this
    }

}