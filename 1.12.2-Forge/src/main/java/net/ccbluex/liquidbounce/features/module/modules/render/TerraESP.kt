package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.ListValue
import org.lwjgl.opengl.GL11

@ModuleInfo(name = "TerraESP", description = "Calamity", category = ModuleCategory.RENDER)
class TerraESP : Module() {
    private val modeValue = ListValue("Mode", arrayOf("CAJ","CXL","KK","LD","YE"), "KK")

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        for (entity in mc.theWorld!!.loadedEntityList) {
            if (entity != mc.thePlayer && EntityUtils.isSelected(entity, false)) {
                val entityLiving = entity.asEntityLivingBase()

                var var10000 =
                    entityLiving.lastTickPosX + (entityLiving.posX - entityLiving.lastTickPosX) * mc.timer.renderPartialTicks
                mc.renderManager
                val pX: Double = var10000 - mc.renderManager.renderPosX
                var10000 =
                    entityLiving.lastTickPosY + (entityLiving.posY - entityLiving.lastTickPosY) * mc.timer.renderPartialTicks
                mc.renderManager
                val pY: Double = var10000 - mc.renderManager.renderPosY
                var10000 =
                    entityLiving.lastTickPosZ + (entityLiving.posZ - entityLiving.lastTickPosZ) * mc.timer.renderPartialTicks
                mc.renderManager
                val pZ: Double = var10000 - mc.renderManager.renderPosZ
                GL11.glPushMatrix()
                GL11.glTranslatef(
                    pX.toFloat(),
                    pY.toFloat() + if (entityLiving.sneaking) 0.8f else 1.3f,
                    pZ.toFloat()
                )
                GL11.glNormal3f(1.0f, 1.0f, 1.0f)
                GL11.glRotatef(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
                GL11.glRotatef(mc.renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
                val scale = 0.06f
                GL11.glScalef(-scale, -scale, scale)
                GL11.glDisable(2896)
                GL11.glDisable(2929)
                GL11.glEnable(3042)
                GL11.glBlendFunc(770, 771)
                GL11.glPushMatrix()
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)

                val datouImgSet =
                    when (modeValue.get().toLowerCase()) {
                        "caj" -> "caj"
                        "cxl" -> "cxl"
                        "kk" -> "kk"
                        "ld" -> "ld"
                        "ye" -> "ye"
                        else -> ""
                    }

                RenderUtils.drawImage(
                    classProvider.createResourceLocation("terra/datou/$datouImgSet.png"),
                    (-8.0).toInt(),
                    (-14.0).toInt(),
                    16,
                    16
                )
                GL11.glPopMatrix()
                GL11.glPopMatrix()


            }
        }
    }
}