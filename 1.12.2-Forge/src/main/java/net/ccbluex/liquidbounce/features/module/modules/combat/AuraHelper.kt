package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "AuraHelper", description = "L", category = ModuleCategory.COMBAT)
class AuraHelper : Module() {
    private val rangeFixValue = BoolValue("RangeFix",true)
    private val groundRangeValue = FloatValue("GroundRange",3.1F,3F,6F).displayable { rangeFixValue.get() }
    private val airRangeValue = FloatValue("AirRange",3.1F,3F,6F).displayable { rangeFixValue.get() }

    private val rotFixValue = BoolValue("RotFix",true)
    private val groundRotValue = ListValue("GroundRot", arrayOf("Vanilla", "Other","BackTrack","Terra"), "Terra").displayable { rotFixValue.get() }
    private val airRotValue = ListValue("AirRot", arrayOf("Vanilla", "Other","BackTrack","Terra"), "Terra").displayable { rotFixValue.get() }

    private val fovFixValue = BoolValue("FovFix",true)
    private val groundFovValue = FloatValue("GroundFov",180F,0F,180F).displayable { fovFixValue.get() }
    private val airFovValue = FloatValue("AirFov",180F,0F,180F).displayable { fovFixValue.get() }

    private val noMoveValue = BoolValue("SafeRange",true)
    private val safeRangeValue = FloatValue("Range",3F,3F,6F).displayable { noMoveValue.get() }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val aura = Terra.moduleManager.getModule(KillAura::class.java)
        if (!aura.state) return

        if (mc2.player.isSprinting) {
            if (mc2.player.onGround) {
                if (rangeFixValue.get()) aura.rangeValue.set(groundRangeValue.get())
                if (rotFixValue.get()) aura.rotations.set(groundRotValue.get())
                if (fovFixValue.get()) aura.fovValue.set(groundFovValue.get())
            } else {
                if (rangeFixValue.get()) aura.rangeValue.set(airRangeValue.get())
                if (rotFixValue.get()) aura.rotations.set(airRotValue.get())
                if (fovFixValue.get()) aura.fovValue.set(airFovValue.get())
            }
        } else {
            if (noMoveValue.get()) aura.rangeValue.set(safeRangeValue.get())
        }

    }

}