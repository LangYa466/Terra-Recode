/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue

@ModuleInfo(name = "Chams", description = "Allows you to see targets through blocks.", category = ModuleCategory.RENDER)
class Chams : Module() {
    val targetsValue = BoolValue("Targets", true)
    val chestsValue = BoolValue("Chests", true)
    val itemsValue = BoolValue("Items", true)
}
