/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import java.awt.Color

@ModuleInfo(name = "TNTESP", description = "Allows you to see ignited TNT blocks through walls.", category = ModuleCategory.RENDER)
class TNTESP : Module() {

    @EventTarget
    fun onRender3D(event : Render3DEvent) {
        mc.theWorld!!.loadedEntityList.filter(classProvider::isEntityTNTPrimed).forEach { RenderUtils.drawEntityBox(it, Color.RED, false) }
    }
}