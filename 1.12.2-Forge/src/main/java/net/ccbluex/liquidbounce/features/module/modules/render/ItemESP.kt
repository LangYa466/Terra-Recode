/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.shader.shaders.OutlineShader
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import java.awt.Color

@ModuleInfo(name = "ItemESP", description = "Allows you to see items through walls.", category = ModuleCategory.RENDER)
class ItemESP : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Box", "OtherBox", "ShaderOutline"), "Box")
    private val colorRedValue = IntegerValue("R", 0, 0, 255)
    private val colorGreenValue = IntegerValue("G", 255, 0, 255)
    private val colorBlueValue = IntegerValue("B", 0, 0, 255)
    private val colorRainbow = BoolValue("Rainbow", true)

    private fun getColor(): Color {
        return if (colorRainbow.get()) rainbow() else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        val color = getColor()
        for (entity in mc.theWorld!!.loadedEntityList) {
            if (!(classProvider.isEntityItem(entity) || classProvider.isEntityArrow(entity))) continue
            when (modeValue.get().toLowerCase()) {
                "box" -> RenderUtils.drawEntityBox(entity, color, true)
                "otherbox" -> RenderUtils.drawEntityBox(entity, color, false)
            }
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (modeValue.get().equals("ShaderOutline", ignoreCase = true)) {
            OutlineShader.OUTLINE_SHADER.startDraw(event.partialTicks)
            try {
                for (entity in mc.theWorld!!.loadedEntityList) {
                    if (!(classProvider.isEntityItem(entity) || classProvider.isEntityArrow(entity)))
                        continue

                    mc.renderManager.renderEntityStatic(entity, event.partialTicks, true)
                }
            } catch (ex: Exception) {
                ClientUtils.getLogger().error("An error occurred while rendering all item entities for shader esp", ex)
            }

            OutlineShader.OUTLINE_SHADER.stopDraw(if (colorRainbow.get()) rainbow() else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 1f, 1f)
        }
    }
}