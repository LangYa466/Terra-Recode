package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.RenderWings
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "Wings", category = ModuleCategory.RENDER, description = "Dragon Wings")
object Wings : Module() {
    private val onlyThirdPerson = BoolValue("OnlyThirdPerson", true)
    val ColourType = ListValue("Color Type", arrayOf("Custom", "Chroma", "None"), "Custom")
    val CR = IntegerValue("R", 255, 0, 255).displayable { ColourType.get().equals("Custom",ignoreCase = true) }
    val CG = IntegerValue("G", 255, 0, 255).displayable { ColourType.get().equals("Custom",ignoreCase = true) }
    val CB = IntegerValue("B", 255, 0, 255).displayable { ColourType.get().equals("Custom",ignoreCase = true) }
    var wingStyle = ListValue("WingStyle", arrayOf("Dragon", "Simple"), "Dragon")


    @EventTarget
    fun onRenderPlayer(event: Render3DEvent) {
        if (onlyThirdPerson.get() && mc2.gameSettings.thirdPersonView == 0) return
        val renderWings = RenderWings()
        renderWings.renderWings(event.partialTicks)
    }

}

