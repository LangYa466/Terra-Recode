package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.TickEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo

@ModuleInfo(name = "NoAchievements", description = "", category = ModuleCategory.CLIENT)
class NoAchievements : Module() {
    @EventTarget
    fun onTick(event: TickEvent) {
        mc2.toastGui.clear()
    }
    init {
        state = true
        array = false
    }
}
