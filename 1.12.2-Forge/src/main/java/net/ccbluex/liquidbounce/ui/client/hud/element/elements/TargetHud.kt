package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntity
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.GlowUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.abs
import kotlin.math.pow

@ElementInfo(name = "TargetHud")
class TargetHud(x: Double = 10.0, y: Double = 10.0, scale: Float = 1F) : Element(x, y, scale) {
    private val radiusValue = IntegerValue("Radius",5,0,8)
    private val fadeSpeed = FloatValue("FadeSpeed", 2F, 1F, 9F)

    private var easingHealth: Float = 0F
    private var lastTarget: IEntity? = null

    override fun drawElement(): Border {
        val target = Terra.combatManager.target ?:
        if (classProvider.isGuiHudDesigner(mc.currentScreen)) mc.thePlayer!! else null

        if (target != null) {
            if (target != lastTarget || easingHealth < 0 || easingHealth > target.maxHealth ||
                abs(easingHealth - target.health) < 0.01) {
                easingHealth = target.health
            }

            GlowUtils.drawGlowy(0f,0f,90F + Fonts.font45.getStringWidth(target.name!!),40f,radiusValue.get(),Color(0,0,0,150))

            if (easingHealth > target.health) GlowUtils.drawGlowy(0F, 0F, (easingHealth / target.maxHealth) * 120f, 40F, radiusValue.get(), Color(150,150,150,200))
            easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime


            Fonts.font45.drawString("${target.name}",41,3,Color.WHITE.rgb)

            var playerInfo = mc.netHandler.getPlayerInfo(mc.thePlayer!!.uniqueID)
            if (classProvider.isEntityPlayer(target)) {
                playerInfo = mc.netHandler.getPlayerInfo(target.uniqueID)
            }
            if (playerInfo != null) {
                Fonts.font45.drawString("${target.health} HP", 41,3 + Fonts.rise45.fontHeight + 10, Color.WHITE.rgb)

                val locationSkin = playerInfo.locationSkin
                val renderHurtTime = target.hurtTime - if (target.hurtTime != 0) {
                    Minecraft.getMinecraft().timer.renderPartialTicks
                } else {
                    0f
                }
                // 受伤的红色效果
                val hurtPercent = renderHurtTime / 10.0F
                GL11.glColor4f(1f, 1 - hurtPercent, 1 - hurtPercent, 1f)
                val scale = if (hurtPercent == 0f) {
                    1f
                } else if (hurtPercent < 0.5f) {
                    1 - (0.2f * hurtPercent * 2)
                } else {
                    0.8f + (0.2f * (hurtPercent - 0.5f) * 2)
                }
                val size = 36
                GL11.glPushMatrix()
                // 受伤的缩放效果
                GL11.glScalef(scale, scale, scale)
                GL11.glTranslatef(((size * 0.5f * (1 - scale)) / scale), ((size * 0.5f * (1 - scale)) / scale), 0f)
                mc.textureManager.bindTexture(locationSkin)
                RenderUtils.drawScaledCustomSizeModalRect(2, 2, 8F, 8F, 8, 8, size, size, 64F, 64F)
                GL11.glPopMatrix()
            }
        }

        lastTarget = target
        return Border(0f,0f,120f,40f)
    }
}