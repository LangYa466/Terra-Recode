package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import kotlin.jvm.internal.Intrinsics

@ModuleInfo("ScaHelp", description = "ScaffoldHelper", category = ModuleCategory.WORLD)
class ScaHelp : Module() {
    private val autoScaffold = ListValue("AutoScaffold", arrayOf("Scaffold"), "Scaffold")
    private val autoTimer = BoolValue("AutoTimer", false)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer!!

        if (!thePlayer.sneaking) {
            val thePlayer2 = mc.thePlayer

            if (thePlayer2 == null) {
                Intrinsics.throwNpe()
            }

            if (thePlayer2!!.onGround) {
                scaChange(false)
            } else {
                scaChange(true)
            }

        }

    }

    @EventTarget
    override fun onDisable() {
        scaChange(false)
    }


    private fun scaChange(state: Boolean) {
        when (autoScaffold.get().toLowerCase()) {
            "scaffold" -> Terra.moduleManager.getModule(Scaffold::class.java).state = state
        }

        if (autoTimer.get()) {
            Terra.moduleManager.getModule(Timer::class.java).state = state
        }

    }


}