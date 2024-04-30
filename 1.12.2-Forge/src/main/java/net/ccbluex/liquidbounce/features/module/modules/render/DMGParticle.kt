package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.Terra.combatManager
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.render.DMGPUtil.Location
import net.ccbluex.liquidbounce.features.module.modules.render.DMGPUtil.Particles
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@ModuleInfo(name = "DMGParticle", description = "Display health volume change value.", category = ModuleCategory.RENDER)
class DMGParticle : Module() {
    private val healthMap = HashMap<EntityLivingBase, Float?>()
    private val particles: MutableList<Particles> = ArrayList()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        var i1 = 0
        while (i1 < particles.size) {
            val update = particles[i1]
            val i = ++update.ticks
            if (i < 10) {
                update.location.setY(update.location.y + update.ticks * 0.002)
            }
            if (i > 20) {
                particles.remove(update)
            }
            i1++
        }
        val entity = if (combatManager.target == null) null else combatManager.target!!.unwrap()
        if (entity == null || entity === mc.thePlayer) {
            return
        }
        if (!healthMap.containsKey(entity)) {
            healthMap[entity] = entity.health
        }
        val floatValue = healthMap[entity]!!
        val health = entity.health
        if (health == floatValue) return
        val text: String = if (floatValue - health < 0.0f) {
            "§a+ " + roundToPlace(((floatValue - health) * -1.0f).toDouble(), 1) + "§c❤"
        } else {
            "§c- " + roundToPlace((floatValue - health).toDouble(), 1) + "❤"
        }
        val location = Location(entity)
        location.setY(entity.entityBoundingBox.minY + (entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY) / 2.0)
        location.setX(location.x - 0.5 + Random(System.currentTimeMillis()).nextInt(5) * 0.15)
        location.setZ(
                location.z - 0.5 + Random(System.currentTimeMillis() + (0x203FF36645D9EA2EL xor 0x203FF36645D9EA2FL)).nextInt(
                        5
                ) * 0.15
        )
        particles.add(Particles(location, text))
        healthMap.remove(entity)
        healthMap[entity] = entity.health

    }

    @EventTarget
    fun onRender(event: Render3DEvent?) {
        for (p in particles) {
            val x = p.location.x
            mc.renderManager
            val n = x - mc.renderManager.renderPosX
            val y = p.location.y
            mc.renderManager
            val n2 = y - mc.renderManager.renderPosY
            val z = p.location.z
            mc.renderManager
            val n3 = z - mc.renderManager.renderPosZ
            GlStateManager.pushMatrix()
            GlStateManager.enablePolygonOffset()
            GlStateManager.doPolygonOffset(1.0f, -1500000.0f)
            GlStateManager.translate(n.toFloat(), n2.toFloat(), n3.toFloat())
            GlStateManager.rotate(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
            val textY = 1.0f
            GlStateManager.rotate(mc.renderManager.playerViewX, textY, 0.0f, 0.0f)
            val size = 0.025
            GlStateManager.scale(-size, -size, size)
            enableGL2D()
            disableGL2D()
            GL11.glDepthMask(false)
            mc.fontRendererObj.drawString(
                    p.text,
                    (-(mc.fontRendererObj.getStringWidth(p.text) / 2)).toFloat(),
                    (-(mc.fontRendererObj.fontHeight - 1)).toFloat(),
                    0,
                    true
            )
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
            GL11.glDepthMask(true)
            GlStateManager.doPolygonOffset(1.0f, 1500000.0f)
            GlStateManager.disablePolygonOffset()
            GlStateManager.popMatrix()
        }
    }

    companion object {
        fun enableGL2D() {
            GL11.glDisable(2929)
            GL11.glEnable(3042)
            GL11.glDisable(3553)
            GL11.glBlendFunc(770, 771)
            GL11.glDepthMask(true)
            GL11.glEnable(2848)
            GL11.glHint(3154, 4354)
            GL11.glHint(3155, 4354)
        }

        fun disableGL2D() {
            GL11.glEnable(3553)
            GL11.glDisable(3042)
            GL11.glEnable(2929)
            GL11.glDisable(2848)
            GL11.glHint(3154, 4352)
            GL11.glHint(3155, 4352)
        }

        fun roundToPlace(p_roundToPlace_0_: Double, p_roundToPlace_2_: Int): Double {
            require(p_roundToPlace_2_ >= 0)
            return BigDecimal(p_roundToPlace_0_).setScale(p_roundToPlace_2_, RoundingMode.HALF_UP).toDouble()
        }
    }
}
