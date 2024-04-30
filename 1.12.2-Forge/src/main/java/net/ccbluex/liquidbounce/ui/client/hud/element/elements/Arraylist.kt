/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.client.hud.element.Side.Horizontal
import net.ccbluex.liquidbounce.ui.client.hud.element.Side.Vertical
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.Colors
import net.ccbluex.liquidbounce.utils.render.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowFontShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowShader
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

/**
 * CustomHUD Arraylist element
 *
 * Shows a list of enabled modules
 */
@ElementInfo(name = "Arraylist", single = true)
class Arraylist(x: Double = 1.0, y: Double = 2.0, scale: Float = 1F, side: Side = Side(Horizontal.RIGHT, Vertical.UP)) : Element(x, y, scale, side) {
    //Color
    private val colorModeValue = ListValue("Text-Color", arrayOf("Custom", "Random", "Rainbow", "OtherRainbow", "Two"), "Custom")
    private val colorRedValue = IntegerValue("Text-R", 255, 0, 255)
    private val colorGreenValue = IntegerValue("Text-G", 255, 0, 255)
    private val colorBlueValue = IntegerValue("Text-B", 255, 0, 255)

    private val fadeOffset = FloatValue("Two-Offset", 0.1f, 0.1f, 1f)
    private val twoRedValue1 = IntegerValue("Two-R1",255,0,255)
    private val twoGreenValue1 = IntegerValue("Two-G1",255,0,255)
    private val twoBlueValue1 = IntegerValue("Two-B1",255,0,255)
    private val twoRedValue2 = IntegerValue("Two-R2",255,0,255)
    private val twoGreenValue2 = IntegerValue("Two-G2",255,0,255)
    private val twoBlueValue2 = IntegerValue("Two-B2",255,0,255)

    private val rainbowX = FloatValue("Rainbow-X", -1000F, -2000F, 2000F)
    private val rainbowY = FloatValue("Rainbow-Y", -1000F, -2000F, 2000F)

    private val shadow = BoolValue("ShadowText", true)

    //Rect
    private val rectValue = ListValue("Rect", arrayOf("None", "Left", "Right", "Outline", "Terra"), "None")
    private val rectColorModeValue = ListValue("Rect-Color", arrayOf("Custom", "Random", "Rainbow", "OtherRainbow", "Two"), "Two")
    private val rectColorRedValue = IntegerValue("Rect-R", 255, 0, 255)
    private val rectColorGreenValue = IntegerValue("Rect-G", 255, 0, 255)
    private val rectColorBlueValue = IntegerValue("Rect-B", 255, 0, 255)
    private val rectColorBlueAlpha = IntegerValue("Rect-Alpha", 255, 0, 255)

    private val saturationValue = FloatValue("Random-Saturation", 0.9f, 0f, 1f)
    private val brightnessValue = FloatValue("Random-Brightness", 1f, 0f, 1f)

    private val tags = BoolValue("Tags", true)
    private val tagsArrayColor = BoolValue("TagsArrayColor", false)

    //Background
    private val backgroundColorModeValue = ListValue("Background-Color", arrayOf("Custom", "Random", "Rainbow", "OtherRainbow", "Two"), "Custom")
    private val backgroundColorRedValue = IntegerValue("Background-R", 0, 0, 255)
    private val backgroundColorGreenValue = IntegerValue("Background-G", 0, 0, 255)
    private val backgroundColorBlueValue = IntegerValue("Background-B", 0, 0, 255)
    private val backgroundColorAlphaValue = IntegerValue("Background-Alpha", 80, 0, 255)
    private val backGroundWidthValue = IntegerValue("BackGroundWidth", 2, 0, 14)

    //Options
    private val upperCaseValue = BoolValue("UpperCase", false)
    private val spaceValue = FloatValue("Space", 0F, 0F, 5F)
    private val textHeightValue = FloatValue("TextHeight", 11F, 1F, 20F)
    private val textYValue = FloatValue("TextY", 1F, 0F, 20F)
    private val breakChange = BoolValue("NameBreak", false)
    private val fontValue = FontValue("Font", Fonts.rise40)

    private var x2 = 0
    private var y2 = 0F

    private var modules = emptyList<Module>()

    override fun drawElement(): Border? {
        val fontRenderer = fontValue.get()
        AWTFontRenderer.assumeNonVolatile = true

        // Slide animation - update every render
        val delta = RenderUtils.deltaTime

        for (module in Terra.moduleManager.modules) {
            if (!module.array || (!module.state && module.slide == 0F)) continue

            var displayString = if (!tags.get())
                module.name
            else if (tagsArrayColor.get())
                module.colorlessTagName
            else module.tagName

            if (upperCaseValue.get())
                displayString = displayString.toUpperCase()

            val width = fontRenderer.getStringWidth(displayString)

            if (module.state) {
                if (module.slide < width) {
                    module.slide = AnimationUtils.easeOut(module.slideStep, width.toFloat()) * width
                    module.slideStep += delta / 4F
                }
            } else if (module.slide > 0) {
                module.slide = AnimationUtils.easeOut(module.slideStep, width.toFloat()) * width
                module.slideStep -= delta / 4F
            }

            module.slide = module.slide.coerceIn(0F, width.toFloat())
            module.slideStep = module.slideStep.coerceIn(0F, width.toFloat())
        }

        // Draw arraylist
        val colorMode = colorModeValue.get()
        val rectColorMode = rectColorModeValue.get()
        val backgroundColorMode = backgroundColorModeValue.get()
        val customColor = Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), 1).rgb
        val rectCustomColor = Color(rectColorRedValue.get(), rectColorGreenValue.get(), rectColorBlueValue.get(), rectColorBlueAlpha.get()).rgb
        val backgroundCustomColor = Color(backgroundColorRedValue.get(), backgroundColorGreenValue.get(), backgroundColorBlueValue.get(), backgroundColorAlphaValue.get()).rgb
        val space = spaceValue.get()
        val textHeight = textHeightValue.get()
        val textY = textYValue.get()
        val rectMode = rectValue.get()
        val textShadow = shadow.get()
        val textSpacer = textHeight + space
        val saturation = saturationValue.get()
        val brightness = brightnessValue.get()

        when (side.horizontal) {
            Horizontal.RIGHT, Horizontal.MIDDLE -> {
                modules.forEachIndexed { index, module ->
                    var displayString = if (!tags.get())
                        module.name
                    else if (tagsArrayColor.get())
                        module.colorlessTagName
                    else module.tagName

                    module.BreakName = breakChange.get()


                    if (upperCaseValue.get())
                        displayString = displayString.toUpperCase()
                    val xPos = -module.slide - 2
                    val yPos = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) * if (side.vertical == Vertical.DOWN) index + 1 else index
                    val moduleColor = Color.getHSBColor(module.hue, saturation, brightness).rgb
                    //Terra Color
                    val terraColor = Colors.getTerraRainBow(Color(twoRedValue1.get(),twoGreenValue1.get(),twoBlueValue1.get()), Color(twoRedValue2.get(),twoGreenValue2.get(),twoBlueValue2.get()),index * fadeOffset.get().toDouble()).rgb

                    val backgroundRectRainbow = backgroundColorMode.equals("Rainbow", ignoreCase = true)

                    val size = modules.size * 2.0E-2f
                    if (module.state) {
                        if (module.higt < yPos) {
                            module.higt += (size - min(module.higt * 0.002f, size - (module.higt * 0.0001f))) * delta
                            module.higt = min(yPos, module.higt)
                        } else {
                            module.higt -= (size - min(module.higt * 0.002f, size - (module.higt * 0.0001f))) * delta
                            module.higt = max(module.higt, yPos)
                        }
                    }

                    RainbowShader.begin(
                        backgroundRectRainbow,
                        if (rainbowX.get() == 0.0F) 0.0F else 1.0F / rainbowX.get(),
                        if (rainbowY.get() == 0.0F) 0.0F else 1.0F / rainbowY.get(),
                        System.currentTimeMillis() % 10000 / 10000F
                    ).use {
                        RenderUtils.drawRect(xPos - if (rectMode.equals("right", true)) 5 else 2, module.higt,
                            if (rectMode.equals("right", true)) -3F else 0F,
                            module.higt + textHeight, when {
                                backgroundRectRainbow -> 0xFF shl 24
                                backgroundColorMode.equals("Random", ignoreCase = true) -> moduleColor
                                backgroundColorMode.equals("OtherRainbow", ignoreCase = true) -> Colors.getRainbow(-2000, (yPos * 8.toFloat()).toInt())
                                backgroundColorMode.equals("Two",ignoreCase = true) -> terraColor
                                else -> backgroundCustomColor
                            }
                        )
                    }

                    val rainbow = colorMode.equals("Rainbow", ignoreCase = true)

                    RainbowFontShader.begin(
                        rainbow,
                        if (rainbowX.get() == 0.0F) 0.0F else 1.0F / rainbowX.get(),
                        if (rainbowY.get() == 0.0F) 0.0F else 1.0F / rainbowY.get(),
                        System.currentTimeMillis() % 10000 / 10000F
                    ).use {
                        fontRenderer.drawString(displayString, xPos - if (rectMode.equals("right", true)) 3 else 0, module.higt + textY,
                            when {
                                rainbow -> 0
                                colorMode.equals("Random", ignoreCase = true) -> moduleColor
                                colorMode.equals("OtherRainbow", ignoreCase = true) -> Colors.getRainbow(-2000, (yPos * 8.toFloat()).toInt())
                                colorMode.equals("Two",ignoreCase = true) -> terraColor
                                else -> customColor
                            },
                            textShadow
                        )
                    }

                    if (!rectMode.equals("none", true)) {
                        val rectRainbow = rectColorMode.equals("Rainbow", ignoreCase = true)

                        RainbowShader.begin(
                            rectRainbow,
                            if (rainbowX.get() == 0.0F) 0.0F else 1.0F / rainbowX.get(),
                            if (rainbowY.get() == 0.0F) 0.0F else 1.0F / rainbowY.get(),
                            System.currentTimeMillis() % 10000 / 10000F
                        ).use {
                            val rectColor = when {
                                rectRainbow -> 0
                                rectColorMode.equals("Random", ignoreCase = true) -> moduleColor
                                rectColorMode.equals("OtherRainbow", ignoreCase = true) -> Colors.getRainbow(-2000, (yPos * 8.toFloat()).toInt())
                                rectColorMode.equals("Two",ignoreCase = true) -> terraColor

                                else -> rectCustomColor
                            }

                            when {
                                rectMode.equals("left", true) -> RenderUtils.drawRect(xPos - 3, module.higt, xPos - 2, module.higt + textHeight, rectColor)
                                rectMode.equals("right", true) -> RenderUtils.drawRect(-3F, module.higt, 1F, module.higt + textHeight, rectColor)
                                rectMode.equals("terra", true) -> RenderUtils.drawRect(-1.2F, module.higt + 1F, 0F, module.higt + textHeight - 1F, rectColor)
                            }
                            if (rectMode.equals("outline", true)) {
                                //左
                                RenderUtils.drawRect(xPos - 1 - backGroundWidthValue.get(), module.higt, xPos - 0 - backGroundWidthValue.get(), module.higt + textHeight + 0.5F, rectColor)
                                if (module != modules[0]) {
                                    var displayStrings = if (!tags.get())
                                        modules[index - 1].name
                                    else if (tagsArrayColor.get())
                                        modules[index - 1].colorlessTagName
                                    else modules[index - 1].tagName

                                    if (upperCaseValue.get())
                                        displayStrings = displayStrings.toUpperCase()

                                    //功能左条和下条间隔
                                    RenderUtils.drawRect(xPos - 1 - backGroundWidthValue.get() - (fontRenderer.getStringWidth(displayStrings) - fontRenderer.getStringWidth(displayString)), module.higt, xPos - 1 - backGroundWidthValue.get(), module.higt + 1, rectColor)
                                }
                                //最底边
                                if (module == modules[modules.size - 1]) { RenderUtils.drawRect(xPos - 1 - backGroundWidthValue.get(), module.higt + textHeight, 0F, module.higt + textHeight + 1, rectColor)

                                }
                            }
                        }
                    }

                }
            }

            Horizontal.LEFT -> {
                modules.forEachIndexed { index, module ->
                    var displayString = if (!tags.get())
                        module.name
                    else if (tagsArrayColor.get())
                        module.colorlessTagName
                    else module.tagName

                    if (upperCaseValue.get())
                        displayString = displayString.toUpperCase()

                    val width = fontRenderer.getStringWidth(displayString)
                    val xPos = -(width - module.slide) + if (rectMode.equals("left", true)) 5 else 2
                    val yPos = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) *
                            if (side.vertical == Vertical.DOWN) index + 1 else index
                    val moduleColor = Color.getHSBColor(module.hue, saturation, brightness).rgb

                    val backgroundRectRainbow = backgroundColorMode.equals("Rainbow", ignoreCase = true)

                    val size = modules.size * 2.0E-2f
                    if (module.state) {
                        if (module.higt < yPos) {
                            module.higt += (size -
                                    min(
                                        module.higt * 0.002f, size - (module.higt * 0.0001f)
                                    )) * delta
                            module.higt = min(yPos, module.higt)
                        } else {
                            module.higt -= (size -
                                    min(
                                        module.higt * 0.002f, size - (module.higt * 0.0001f)
                                    )) * delta
                            module.higt = max(module.higt, yPos)
                        }
                    }
                    RainbowShader.begin(
                        backgroundRectRainbow,
                        if (rainbowX.get() == 0.0F) 0.0F else 1.0F / rainbowX.get(),
                        if (rainbowY.get() == 0.0F) 0.0F else 1.0F / rainbowY.get(),
                        System.currentTimeMillis() % 10000 / 10000F
                    ).use {
                        RenderUtils.drawRect(
                            0F,
                            module.higt,
                            xPos + width + if (rectMode.equals("right", true)) 5 else 2,
                            module.higt + textHeight, when {
                                backgroundRectRainbow -> 0
                                backgroundColorMode.equals("Random", ignoreCase = true) -> moduleColor
                                else -> backgroundCustomColor
                            }
                        )
                    }

                    val rainbow = colorMode.equals("Rainbow", ignoreCase = true)

                    RainbowFontShader.begin(
                        rainbow,
                        if (rainbowX.get() == 0.0F) 0.0F else 1.0F / rainbowX.get(),
                        if (rainbowY.get() == 0.0F) 0.0F else 1.0F / rainbowY.get(),
                        System.currentTimeMillis() % 10000 / 10000F
                    ).use {
                        fontRenderer.drawString(
                            displayString, xPos, module.higt + textY, when {
                                rainbow -> 0
                                colorMode.equals("Random", ignoreCase = true) -> moduleColor
                                else -> customColor
                            }, textShadow
                        )
                    }

                    val rectColorRainbow = rectColorMode.equals("Rainbow", ignoreCase = true)

                    RainbowShader.begin(
                        rectColorRainbow,
                        if (rainbowX.get() == 0.0F) 0.0F else 1.0F / rainbowX.get(),
                        if (rainbowY.get() == 0.0F) 0.0F else 1.0F / rainbowY.get(),
                        System.currentTimeMillis() % 10000 / 10000F
                    ).use {
                        if (!rectMode.equals("none", true)) {
                            val rectColor = when {
                                rectColorRainbow -> 0
                                rectColorMode.equals("Random", ignoreCase = true) -> moduleColor
                                else -> rectCustomColor
                            }

                            when {
                                rectMode.equals("left", true) -> RenderUtils.drawRect(
                                    0F,
                                    module.higt - 1, 3F, module.higt + textHeight, rectColor
                                )

                                rectMode.equals("right", true) ->
                                    RenderUtils.drawRect(
                                        xPos + width + 2, module.higt, xPos + width + 2 + 3,
                                        module.higt + textHeight, rectColor
                                    )
                            }
                        }
                    }
                }
            }
        }

        // Draw border
        if (classProvider.isGuiHudDesigner(mc.currentScreen)) {
            x2 = Int.MIN_VALUE

            if (modules.isEmpty()) {
                return if (side.horizontal == Horizontal.LEFT)
                    Border(0F, -1F, 20F, 20F)
                else
                    Border(0F, -1F, -20F, 20F)
            }

            for (module in modules) {
                when (side.horizontal) {
                    Horizontal.RIGHT, Horizontal.MIDDLE -> {
                        val xPos = -module.slide.toInt() - 2
                        if (x2 == Int.MIN_VALUE || xPos < x2) x2 = xPos
                    }

                    Horizontal.LEFT -> {
                        val xPos = module.slide.toInt() + 14
                        if (x2 == Int.MIN_VALUE || xPos > x2) x2 = xPos
                    }
                }
            }
            y2 = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) * modules.size

            return Border(0F, 0F, x2 - 7F, y2 - if (side.vertical == Vertical.DOWN) 1F else 0F)
        }

        AWTFontRenderer.assumeNonVolatile = false
        GlStateManager.resetColor()
        return null
    }

    override fun updateElement() {
        modules = Terra.moduleManager.modules
            .filter { it.array && it.slide > 0 }
            .sortedBy {
                -fontValue.get()
                    .getStringWidth(if (upperCaseValue.get()) (if (!tags.get()) it.name else if (tagsArrayColor.get()) it.colorlessTagName else it.tagName).toUpperCase() else if (!tags.get()) it.name else if (tagsArrayColor.get()) it.colorlessTagName else it.tagName)
            }
    }

}