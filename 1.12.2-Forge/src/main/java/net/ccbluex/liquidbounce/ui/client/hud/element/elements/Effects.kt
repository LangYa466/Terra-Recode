package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.api.minecraft.potion.IPotion
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.effects.HanaBiColors
import net.ccbluex.liquidbounce.utils.effects.PotionData
import net.ccbluex.liquidbounce.utils.effects.Translate
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import kotlin.math.abs
import kotlin.math.max

@ElementInfo(name = "Effects")
class Effects(x: Double = 1.0, y: Double = 239.0, scale: Float = 1F, side: Side = Side(Side.Horizontal.LEFT, Side.Vertical.UP)) : Element(x, y, scale, side) {
    private val potionMap: MutableMap<IPotion, PotionData?> = HashMap()
    private val potionMaxDurations: MutableMap<Int, Int> = mutableMapOf()
    /**
     * Options
     */
    companion object {
        private val shadow = BoolValue("Shadow", false)
    }

    /**
     * Draw the entity.
     */
    override fun drawElement(): Border {
        GlStateManager.pushMatrix()
        var y = 0
        for (potionEffect in Objects.requireNonNull(mc.thePlayer)!!.activePotionEffects) {
            val potion = functions.getPotionById(potionEffect.potionID)
            val name = functions.formatI18n(potion.name)
            val potionData: PotionData?
            if (potionMap.containsKey(potion) && potionMap[potion]!!.level == potionEffect.amplifier) potionData =
                potionMap[potion]
            else potionMap[potion] =
                PotionData(potion, Translate(0f, 40f + y), potionEffect.amplifier).also { potionData = it }
            var flag = true
            for (checkEffect in mc.thePlayer!!.activePotionEffects) if (checkEffect.amplifier == potionData!!.level) {
                flag = false
                break
            }
            val potion2 = potionEffect.potionID
            val maxDuration = potionMaxDurations[potion2]
            if (maxDuration == null || maxDuration < potionEffect.duration) {
                potionMaxDurations[potion2] = potionEffect.duration
            }
            if (flag) potionMap.remove(potion)
            var potionTime: Int
            var potionMaxTime: Int
            try {
                potionTime = potionEffect.getDurationString().split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0].toInt()
                potionMaxTime = potionEffect.getDurationString().split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1].toInt()
            } catch (ignored: Exception) {
                potionTime = 100
                potionMaxTime = 1000
            }
            val lifeTime = (potionTime * 60 + potionMaxTime)
            if (potionData!!.getMaxTimer() == 0 || lifeTime > potionData.getMaxTimer().toDouble()) potionData.maxTimer =
                lifeTime
            var state = 0.0f
            if (lifeTime >= 0.0) state = (lifeTime / potionData.getMaxTimer().toFloat().toDouble() * 100.0).toFloat()
            val position = Math.round(potionData.translate.y + 5)
            state = max(state.toDouble(), 2.0).toFloat()

            //进度条
            potionData.translate.interpolate(0f, y.toFloat(), 0.1)
            potionData.animationX = RenderUtils.getAnimationState2(potionData.getAnimationX().toDouble(), (1.2f * state).toDouble(), (max(10.0, (abs((potionData.animationX - 1.2f * state).toDouble()) * 15.0f)) * 0.3f)).toFloat()
            RenderUtils.drawRectPotion(0f, potionData.translate.y, 120f, potionData.translate.y + 30f, ClientUtils.reAlpha(Color(0, 0, 0).brighter().rgb, 0.2f))

            //进度条倒计时
            val potionDurationRatio = potionEffect.duration.toFloat() / (potionMaxDurations[potionEffect.potionID]?.toFloat() ?: 1f)
            RenderUtils.drawRectPotion(0f, potionData.translate.y,potionDurationRatio *120f, potionData.translate.y + 30f, ClientUtils.reAlpha(Color(0, 0, 0).brighter().rgb, 0.2f))

            if (shadow.get()) {
                RenderUtils.drawShadowWithCustomAlpha(0f, Math.round(potionData.translate.y).toFloat(), 120f, 30f, 200f)
            }

            val posY = potionData.translate.y + 13f
            Fonts.font35.drawString(name + " " + intToRomanByGreedy(potionEffect.amplifier + 1), 29f, posY - mc.fontRendererObj.fontHeight, ClientUtils.reAlpha(HanaBiColors.WHITE.c, 0.8f))
            Fonts.font35.drawString(potionEffect.getDurationString(), 29f, posY + 4.0f, ClientUtils.reAlpha(Color(200, 200, 200).rgb, 0.5f))

            if (potion.hasStatusIcon) {
                GlStateManager.pushMatrix()
                GL11.glDisable(2929)
                GL11.glEnable(3042)
                GL11.glDepthMask(false)
                OpenGlHelper.glBlendFunc(770, 771, 1, 0)
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
                val statusIconIndex = potion.statusIconIndex
                mc.textureManager.bindTexture(classProvider.createResourceLocation("textures/gui/container/inventory.png"))
                mc2.ingameGUI.drawTexturedModalRect(
                    6f,
                    (position - 40).toFloat(),
                    statusIconIndex % 8 * 18,
                    198 + statusIconIndex / 8 * 18,
                    18,
                    18
                )
                GL11.glDepthMask(true)
                GL11.glDisable(3042)
                GL11.glEnable(2929)
                GlStateManager.popMatrix()
            }
            y -= 35
        }
        GlStateManager.popMatrix()
        return Border(0f, 0f, 120f, 30f)
    }

    private fun intToRomanByGreedy(nu: Int): String {
        var num = nu
        val values = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
        val symbols = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
        val stringBuilder = StringBuilder()
        var i = 0
        while (i < values.size && num >= 0) {
            while (values[i] <= num) {
                num -= values[i]
                stringBuilder.append(symbols[i])
            }
            i++
        }

        return stringBuilder.toString()
    }
}
