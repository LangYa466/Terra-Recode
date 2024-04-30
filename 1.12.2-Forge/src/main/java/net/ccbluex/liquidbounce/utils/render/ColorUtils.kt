/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.utils.Colors
import net.ccbluex.liquidbounce.utils.MathUtils.interpolateInt
import net.minecraft.client.Minecraft
import java.awt.Color
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object ColorUtils {
    /** Array of the special characters that are allowed in any text drawing of Minecraft.  */
    val allowedCharactersArray = charArrayOf('/', '\n', '\r', '\t', '\u0000', '', '`', '?', '*', '\\', '<', '>', '|', '\"', ':')

    fun isAllowedCharacter(character: Char): Boolean {
        return character.toInt() != 167 && character.toInt() >= 32 && character.toInt() != 127
    }

    fun rainbowWithAlpha(alpha: Int) = reAlpha(hslRainbow(1), alpha)

    private val COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]")

    /**
     * CNFont
     */
    val RED: Int = getRGB(255, 0, 0)
    val GREED: Int = getRGB(0, 255, 0)
    val BLUE: Int = getRGB(0, 0, 255)
    val GREEN: Int = getRGB(0,255,0)
    val WHITE: Int = getRGB(255, 255, 255)
    val BLACK: Int = getRGB(0, 0, 0)
    val GREY: Int = getRGB(50,50,50,50)
    val NO_COLOR: Int = getRGB(0, 0, 0, 0)

    @JvmStatic
    fun getRGB(r: Int, g: Int, b: Int): Int {
        return net.ccbluex.liquidbounce.utils.render.ColorUtils.getRGB(r, g, b, 255)
    }

    @JvmStatic
    fun getRGB(r: Int, g: Int, b: Int, a: Int): Int {
        return (a and 0xFF) shl 24 or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF)
    }

    @JvmStatic
    fun getRGB(rgb: Int): Int {
        return -0x1000000 or rgb
    }

    @JvmStatic
    fun splitRGB(rgb: Int): IntArray {
        return intArrayOf(rgb shr 16 and 0xFF, rgb shr 8 and 0xFF, rgb and 0xFF)
    }

    @JvmStatic
    fun reAlpha(rgb: Int, alpha: Int): Int {
        return getRGB(getRed(rgb), getGreen(rgb), getBlue(rgb), alpha)
    }

    //CrossHair
    fun reAlpha(color: Color, alpha: Int): Color {
        return Color(color.red, color.green, color.blue, alpha)
    }
    //

    @JvmStatic
    fun getRed(rgb: Int): Int {
        return rgb shr 16 and 0xFF
    }

    @JvmStatic
    fun getGreen(rgb: Int): Int {
        return rgb shr 8 and 0xFF
    }

    @JvmStatic
    fun getBlue(rgb: Int): Int {
        return rgb and 0xFF
    }

    @JvmStatic
    fun getAlpha(rgb: Int): Int {
        return rgb shr 24 and 0xFF
    }

    fun getColor(hueoffset: Float, saturation: Float, brightness: Float): Int {
        val speed = 4500f
        val hue = System.currentTimeMillis() % speed.toInt() / speed
        return Color.HSBtoRGB(hue - hueoffset / 54, saturation, brightness)
    }
    /**
     * ↑
     */

    //ESP CSGO
    @JvmStatic
    fun getHealthColor(health: Float, maxHealth: Float): Int {
        var health = health
        if (health > 20) {
            health = 20f
        }
        val fractions = floatArrayOf(0f, 0.5f, 1f)
        val colors = arrayOf(Color.RED, Color.YELLOW, Color.GREEN)
        val progress = health * 5 * 0.01f
        val customColor = Colors.blendColors(fractions, colors, progress)!!.brighter()
        return customColor.rgb
    }
    //ESP CSGO

    @JvmField
    val hexColors = IntArray(16)

    init {
        repeat(16) { i ->
            val baseColor = (i shr 3 and 1) * 85

            val red = (i shr 2 and 1) * 170 + baseColor + if (i == 6) 85 else 0
            val green = (i shr 1 and 1) * 170 + baseColor
            val blue = (i and 1) * 170 + baseColor

            hexColors[i] = red and 255 shl 16 or (green and 255 shl 8) or (blue and 255)
        }
    }

    @JvmStatic
    fun stripColor(input: String?): String? {
        return COLOR_PATTERN.matcher(input ?: return null).replaceAll("")
    }

    @JvmStatic
    fun translateAlternateColorCodes(textToTranslate: String): String {
        val chars = textToTranslate.toCharArray()

        for (i in 0 until chars.size - 1) {
            if (chars[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".contains(chars[i + 1], true)) {
                chars[i] = '§'
                chars[i + 1] = Character.toLowerCase(chars[i + 1])
            }
        }

        return String(chars)
    }

    fun randomMagicText(text: String): String {
        val stringBuilder = StringBuilder()
        val allowedCharacters = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000"

        for (c in text.toCharArray()) {
            if (isAllowedCharacter(c)) {
                val index = Random().nextInt(allowedCharacters.length)
                stringBuilder.append(allowedCharacters.toCharArray()[index])
            }
        }

        return stringBuilder.toString()
    }

    @JvmStatic
    fun rainbow(): Color {
        val currentColor = Color(Color.HSBtoRGB((System.nanoTime() + 400000L) / 10000000000F % 1, 1F, 1F))
        return Color(currentColor.red / 255F * 1F, currentColor.green / 255f * 1F, currentColor.blue / 255F * 1F, currentColor.alpha / 255F)
    }

    // TODO: Use kotlin optional argument feature

    @JvmStatic
    fun rainbow(offset: Long): Color {
        val currentColor = Color(Color.HSBtoRGB((System.nanoTime() + offset) / 10000000000F % 1, 1F, 1F))
        return Color(currentColor.red / 255F * 1F, currentColor.green / 255F * 1F, currentColor.blue / 255F * 1F,
                currentColor.alpha / 255F)
    }

    @JvmStatic
    fun rainbow(alpha: Float) = rainbow(400000L, alpha)

    @JvmStatic
    fun rainbow(alpha: Int) = rainbow(400000L, alpha / 255)

    @JvmStatic
    fun rainbow(offset: Long, alpha: Int) = rainbow(offset, alpha.toFloat() / 255)
    @JvmStatic
    fun ALLColor(offset: Long): Color {
        val currentColor = Color(Color.HSBtoRGB((Minecraft.getMinecraft().player.ticksExisted / 50.0 + Math.sin(50 / 50.0 * 1.6) % 1).toFloat(), 0.4F, 0.9F))

        return Color(currentColor.rgb)
    }

    @JvmStatic
    fun originalrainbow(offset: Long): Color {
        val currentColor = Color(Color.HSBtoRGB((System.nanoTime() + offset) / 10000000000F % 1, 1.0F, 1.0F))
        return Color(currentColor.red / 255F * 1F, currentColor.green / 255F * 1F, currentColor.blue / 255F * 1F,
                currentColor.alpha / 255F)
    }

    @JvmStatic
    fun LiquidSlowly(time: Long, count: Int, qd: Float, sq: Float): Color? {
        val color = Color(Color.HSBtoRGB((time.toFloat() + count * -3000000f) / 2 / 1.0E9f, qd, sq))
        return Color(color.red / 255.0f * 1, color.green / 255.0f * 1, color.blue / 255.0f * 1, color.alpha / 255.0f)
    }
    @JvmStatic
    fun rainbow(offset: Long, alpha: Float): Color {
        val currentColor = Color(Color.HSBtoRGB((System.nanoTime() + offset) / 10000000000F % 1, 1F, 1F))
        return Color(currentColor.red / 255F * 1F, currentColor.green / 255f * 1F, currentColor.blue / 255F * 1F, alpha)
    }
    @JvmStatic
    fun TwoRainbow(offset: Long,alpha: Float): Color {
        var currentColor = Color(Color.HSBtoRGB((System.nanoTime() + offset) / 8.9999999E10F % 1, 0.75F, 0.8F));
        return Color(currentColor.getRed() / 255.0F * 1.0F, currentColor.getGreen() / 255.0F * 1.0F, currentColor.getBlue() / 255.0F * 1.0F, alpha);
    }

    //Crosshair
    @JvmStatic
    fun slowlyRainbow(time: Long, count: Int, qd: Float, sq: Float): Color {
        val color = Color(Color.HSBtoRGB((time.toFloat() + count * -3000000f) / 2 / 1.0E9f, qd, sq))
        return Color(color.red / 255.0f * 1, color.green / 255.0f * 1, color.blue / 255.0f * 1, color.alpha / 255.0f)
    }

    //Opacity value ranges from 0-1
    fun applyOpacity(color: Color, opacity: Float): Color {
        var opacity = opacity
        opacity = min(1.0, max(0.0, opacity.toDouble())).toFloat()
        return Color(color.red, color.green, color.blue, (color.alpha * opacity).toInt())
    }

    //RiseButton
    private val startTime = System.currentTimeMillis()
    fun hslRainbow(index: Int, lowest: Float = 0.41f, bigest: Float = 0.58f, indexOffset: Int = 300, timeSplit: Int = 1500): Color {
        return Color.getHSBColor((abs(((((System.currentTimeMillis() - startTime).toInt() + index * indexOffset) / timeSplit.toFloat()) % 2) - 1) * (bigest - lowest)) + lowest, 0.7f, 1f)
    }

    fun interpolateColorC(color1: Color, color2: Color, amount: Float): Color {
        var amount = amount
        amount = min(1.0, max(0.0, amount.toDouble())).toFloat()
        return Color(
            interpolateInt(color1.red, color2.red, amount),
            interpolateInt(color1.green, color2.green, amount),
            interpolateInt(color1.blue, color2.blue, amount),
            interpolateInt(color1.alpha, color2.alpha, amount)
        )
    }

}
