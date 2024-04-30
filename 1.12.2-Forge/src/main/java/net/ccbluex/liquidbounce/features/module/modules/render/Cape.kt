/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.ListValue

import net.minecraft.util.ResourceLocation

@ModuleInfo(name = "Cape", description = "Inside Custom capes.", category = ModuleCategory.RENDER)
class Cape : Module() {
    val styleValue = ListValue("Style", arrayOf("Astolfo", "Lunar"), "Astolfo")

    fun getCapeLocation(value: String): ResourceLocation {
        return try {
            CapeStyle.valueOf(value.toUpperCase()).location
        } catch (e: IllegalArgumentException) {
            CapeStyle.LUNAR.location
        }
    }

    enum class CapeStyle(val location: ResourceLocation) {
        ASTOLFO(ResourceLocation("terra/capes/astolfo.png")),
        LUNAR(ResourceLocation("terra/capes/lunar.png"))
    }

    override val tag: String
        get() = styleValue.get()

}