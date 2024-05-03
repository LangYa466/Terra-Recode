package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.api.minecraft.client.gui.IFontRenderer
import net.ccbluex.liquidbounce.api.minecraft.util.IResourceLocation
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.FadeState.*
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notifications.Companion.shadowValue
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.GlowUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RoundedUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.max
import kotlin.math.pow


/**
 * CustomHUD Notification element
 */
@ElementInfo(name = "Notifications")
class Notifications(x: Double = 0.0, y: Double = 0.0, scale: Float = 1F, side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)) : Element(x, y, scale, side) {
    companion object {
        val shadowValue = BoolValue("Shadow", true)
        val styleValue = ListValue("Mode", arrayOf("Terra", "Tenacity","LangYa"), "LangYa")
    }

    /**
     * Example notification for CustomHUD designer
     */
    private val exampleNotification = Notification("Notification", "This is an example notification.", NotifyType.INFO)


    /**
     * Draw element
     */
    override fun drawElement(): Border? {
        Terra.hud.notifications.map { it }.forEachIndexed { index, notify ->
            GL11.glPushMatrix()

            if (notify.drawNotification(
                    index,
                    Fonts.font35,
                    200,
                    this.renderX.toFloat(),
                    this.renderY.toFloat(),
                    scale,
                    contentShadow = false,
                    titleShadow = false,
                    whiteText = true,
                    modeColored = true,
                    parent = Companion
                )
            ) {
                Terra.hud.notifications.remove(notify)
            }

            GL11.glPopMatrix()
        }

        if (classProvider.isGuiHudDesigner(mc.currentScreen)) {
            if (!Terra.hud.notifications.contains(exampleNotification)) {
                Terra.hud.addNotification(exampleNotification)
            }

            exampleNotification.fadeState = STAY
            exampleNotification.displayTime = System.currentTimeMillis()

            return Border(-exampleNotification.width.toFloat(), -exampleNotification.height.toFloat(), 0F, 0F)
        }

        return null
    }

}


class Notification(
    val title: String,
    val content: String,
    val type: NotifyType,
    val time: Int = 1500,
    private val animeTime: Int = 500
) {
    private var s: String? = null
    var width = 100
    var height = 30
    var x = 0F

    private var textLengthtitle = 0
    private var textLengthcontent = 0
    private var textLength = 0f

    init {
        textLengthtitle = Fonts.font35.getStringWidth(title)
        textLengthcontent = Fonts.font35.getStringWidth(content)
        textLength = textLengthcontent.toFloat() + textLengthtitle.toFloat()
    }

    var fadeState = IN
    private var nowY = -height
    var displayTime = System.currentTimeMillis()
    private var animeXTime = System.currentTimeMillis()
    private var animeYTime = System.currentTimeMillis()


    fun easeInBack(x: Double): Double {
        val c1 = 1.70158
        val c3 = c1 + 1

        return c3 * x * x * x - c1 * x * x
    }

    fun easeOutBack(x: Double): Double {
        val c1 = 1.70158
        val c3 = c1 + 1

        return 1 + c3 * (x - 1).pow(3) + c1 * (x - 1).pow(2)
    }

    /**
     * Draw notification
     */
    fun drawNotification(
        index: Int, font: IFontRenderer, alpha: Int, x: Float, y: Float, scale: Float,
        contentShadow: Boolean,
        titleShadow: Boolean,
        whiteText: Boolean,
        modeColored: Boolean,
        parent: Notifications.Companion

    ): Boolean {
        this.width = 100.coerceAtLeast(
            font.getStringWidth(content)
                .coerceAtLeast(font.getStringWidth(title)) + 15
        )
        val realY = -(index + 1) * height
        val nowTime = System.currentTimeMillis()
        var transY = nowY.toDouble()
        font.getStringWidth("$title: $content")

        val textColor: Int = if (whiteText) {
            Color(255, 255, 255).rgb
        } else {
            Color(10, 10, 10).rgb
        }
        val error = MinecraftInstance.classProvider.createResourceLocation("terra/notifications/tenacity/cross.png")
        val successful =
            MinecraftInstance.classProvider.createResourceLocation("terra/notifications/tenacity/tick.png")
        val warn =
            MinecraftInstance.classProvider.createResourceLocation("terra/notifications/tenacity/warning.png")
        val info = MinecraftInstance.classProvider.createResourceLocation("terra/notifications/tenacity/info.png")
        // Y-Axis Animation
        if (nowY != realY) {
            var pct = (nowTime - animeYTime) / animeTime.toDouble()
            if (pct > 1) {
                nowY = realY
                pct = 1.0
            } else {
                pct = EaseUtils.easeOutExpo(pct)
            }
            transY += (realY - nowY) * pct
        } else {
            animeYTime = nowTime
        }

        // X-Axis Animation
        var pct = (nowTime - animeXTime) / animeTime.toDouble()
        when (fadeState) {
            IN -> {
                if (pct > 1) {
                    fadeState = STAY
                    animeXTime = nowTime
                    pct = 1.0
                }
                pct = easeOutBack(pct)
            }

            STAY -> {
                pct = 1.0
                if ((nowTime - animeXTime) > time) {
                    fadeState = OUT
                    animeXTime = nowTime
                }
            }

            OUT -> {
                if (pct > 1) {
                    fadeState = END
                    animeXTime = nowTime
                    pct = 1.0
                }
                pct = 1 - easeInBack(pct)
            }

            END -> {
                return true
            }
        }
        val transX = width - (width * pct) - width
        GL11.glTranslated(transX, transY, 0.0)
        // draw notify
        val style = parent.styleValue.get()

        if (style.equals("Tenacity")) {
            val fontRenderer = Fonts.font35
            val thisWidth = 100.coerceAtLeast(
                fontRenderer.getStringWidth(this.title).coerceAtLeast(fontRenderer.getStringWidth(this.content)) + 40
            )
            when (type.renderColor) {
                Color(0xFF2F2F) -> {
                    RenderUtils.drawRoundedCornerRect(
                        -18F,
                        1F,
                        thisWidth.toFloat(),
                        height.toFloat() - 2F,
                        5f,
                        Color(180, 0, 0, 190).rgb
                    )
                    RenderUtils.drawImage(error, -13, 5, 18, 18)
                    Fonts.font35.drawString(title, 9F, 16F, Color(255, 255, 255, 255).rgb)
                    Fonts.font40.drawString(content, 9F, 6F, Color(255, 255, 255, 255).rgb)
                }

                Color(0x60E092) -> {
                    RenderUtils.drawRoundedCornerRect(
                        -16F,
                        1F,
                        thisWidth.toFloat(),
                        height.toFloat() - 2F,
                        5f,
                        Color(0, 180, 0, 190).rgb
                    )
                    RenderUtils.drawImage(successful, -13, 5, 18, 18)
                    Fonts.font35.drawString(title, 9F, 16F, Color(255, 255, 255, 255).rgb)
                    Fonts.font40.drawString(content, 9F, 6F, Color(255, 255, 255, 255).rgb)
                }

                Color(0xF5FD00) -> {
                    RenderUtils.drawRoundedCornerRect(
                        -16F,
                        1F,
                        thisWidth.toFloat(),
                        height.toFloat() - 2F,
                        5f,
                        Color(0, 0, 0, 190).rgb
                    )
                    RenderUtils.drawImage(warn, -13, 5, 18, 18)
                    Fonts.font35.drawString(title, 9F, 16F, Color(255, 255, 255, 255).rgb)
                    Fonts.font40.drawString(content, 9F, 6F, Color(255, 255, 255, 255).rgb)
                }

                else -> {
                    RenderUtils.drawRoundedCornerRect(
                        -16F,
                        1F,
                        thisWidth.toFloat(),
                        height.toFloat() - 2F,
                        5f,
                        Color(0, 0, 0, 190).rgb
                    )
                    RenderUtils.drawImage(info, -13, 5, 18, 18)
                    Fonts.font35.drawString(title, 9F, 16F, Color(255, 255, 255, 255).rgb)
                    Fonts.font40.drawString(content, 9F, 6F, Color(255, 255, 255, 255).rgb)
                }
            }
            return false
        }

        if (type == NotifyType.SUCCESS)
            s = "SUCCESS";
        else if (type == NotifyType.ERROR)
            s = "ERROR";
        else if (type == NotifyType.WARNING)
            s = "WARNING";
        else if (type == NotifyType.INFO)
            s = "INFO";
        if (style.equals("Terra")) {
            if (s == "INFO") {
                if (shadowValue.get()) RenderUtils.drawShadow(
                    0.0f,
                    0.0f,
                    Fonts.font35.getStringWidth(content) + 25F,
                    20F
                )
                RenderUtils.drawRect(
                    0F,
                    0F,
                    max(
                        width - width * ((nowTime - displayTime) / (animeTime * 2F + time)) + Fonts.font40.getStringWidth(
                            content
                        ) - 95F, 0F
                    ),
                    20f,
                    Color(33, 150, 252).rgb
                )
                RenderUtils.drawRect(0F, 0F, Fonts.font35.getStringWidth(content) + 25F, 20f, Color(0, 0, 0, 100).rgb)
                Fonts.font40.drawString(content, 3F, 6f, Color.white.rgb)
            }
            if (s == "SUCCESS") {
                if (shadowValue.get()) RenderUtils.drawShadow(
                    0.0f,
                    0.0f,
                    Fonts.font40.getStringWidth(content) + 25F,
                    20F
                )
                RenderUtils.drawRect(
                    0F,
                    0F,
                    max(
                        width - width * ((nowTime - displayTime) / (animeTime * 2F + time)) + Fonts.font40.getStringWidth(
                            content
                        ) - 85F, 0F
                    ),
                    20f,
                    Color(76, 236, 76).rgb
                )
                RenderUtils.drawRect(0F, 0F, Fonts.font40.getStringWidth(content) + 25F, 20f, Color(0, 0, 0, 100).rgb)
                Fonts.font40.drawString(content, 3F, 6f, Color.white.rgb)
            }
            if (s == "ERROR") {
                if (shadowValue.get()) RenderUtils.drawShadow(
                    0.0f,
                    0.0f,
                    Fonts.font40.getStringWidth(content) + 25F,
                    20F
                )
                RenderUtils.drawRect(
                    0F,
                    0F,
                    max(
                        width - width * ((nowTime - displayTime) / (animeTime * 2F + time)) + Fonts.font40.getStringWidth(
                            content
                        ) - 85F, 0F
                    ),
                    20f,
                    Color(244, 50, 50).rgb
                )
                RenderUtils.drawRect(0F, 0F, Fonts.font40.getStringWidth(content) + 25F, 20f, Color(0, 0, 0, 100).rgb)
                Fonts.font40.drawString(content, 3F, 6f, Color.white.rgb)
            }
            if (s == "WARNING") {
                if (shadowValue.get()) RenderUtils.drawShadow(
                    0.0f,
                    0.0f,
                    Fonts.font40.getStringWidth(content) + 25F,
                    20F
                )
                RenderUtils.drawRect(
                    0F,
                    0F,
                    max(
                        width - width * ((nowTime - displayTime) / (animeTime * 2F + time)) + Fonts.font40.getStringWidth(
                            content
                        ) - 85F, 0F
                    ),
                    20f,
                    Color(251, 140, 0).rgb
                )
                RenderUtils.drawRect(0F, 0F, Fonts.font40.getStringWidth(content) + 25F, 20f, Color(0, 0, 0, 100).rgb)
                Fonts.font40.drawString(content, 3F, 6f, Color.white.rgb)
            }

            return false
        }

        if (style == "LangYa") {
            height = 30
            width = Fonts.font35.getStringWidth(content) + Fonts.font40.getStringWidth(title) + 10
            GlowUtils.drawGlow(0F, 0F, width.toFloat(), height.toFloat(), 2, Color(0, 0, 0, 80))
            RoundedUtils.drawRound(
                0F,
                30F,
                max(
                    width - width * ((nowTime - displayTime) / (animeTime * 2F + time)) + width - 100F, 0F
                ),
                2f,
                2f,
                type.renderColor
            )

            Fonts.font40.drawString(title, 30F, 15F, Color(255, 255, 255, 255).rgb)
            Fonts.font35.drawString(content, 30F, 5F, Color(255, 255, 255, 255).rgb)

            var img: IResourceLocation? = null
            when (s) {
                "INFO" -> img = info
                "SUCCESS" -> img = successful
                "ERROR" -> img = error
                "WARNING" -> img = warn
            }
            RenderUtils.drawImage(img, 2, 4, 25, 25)
            return false
        }

        return false
    }

}

enum class NotifyType(var renderColor: Color) {
    SUCCESS(Color(0x60E092)),
    ERROR(Color(0xFF2F2F)),
    WARNING(Color(0xF5FD00)),
    INFO(Color(0x6490A7));
}

enum class FadeState { IN, STAY, OUT, END }

