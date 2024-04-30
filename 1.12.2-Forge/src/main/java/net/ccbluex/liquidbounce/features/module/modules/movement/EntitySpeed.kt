package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.entity.EntityLivingBase

@ModuleInfo(name = "EntitySpeed", description = "Solid collision acceleration.", category = ModuleCategory.MOVEMENT)
class EntitySpeed : Module() {
    private val onlyAir = BoolValue("OnlyAir",false)
    private val okstrafe = BoolValue("Strafe",false)
    private val keepSprint = BoolValue("KeepSprint",false)
    private val speedUp = BoolValue("SpeedUp",false)
    private val speed = IntegerValue("Speed", 0, 0, 20)
    private val distance = FloatValue("Range", 0f, 0f, 3f)
    private var speeded = false
    private var sprint = false

    override fun onEnable() {
        speeded = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val strafe = Terra.moduleManager.getModule(Strafe::class.java) as Strafe
        for (entity in mc.theWorld!!.loadedEntityList) {
            if (entity.unwrap() is EntityLivingBase && entity.entityId != mc.thePlayer!!.entityId && mc.thePlayer!!.getDistanceToEntityBox(
                    entity
                ) <= distance.get() && ( !onlyAir.get() || !mc.thePlayer!!.onGround)
            ) {
                if(speedUp.get()) {
                    mc.thePlayer!!.motionX *= (1 + (speed.get() * 0.01))
                    mc.thePlayer!!.motionZ *= (1 + (speed.get() * 0.01))
                }
                if(keepSprint.get()){
                    sprint = true
                }
                if(okstrafe.get()){
                    strafe.state = true
                }
                return
            }
            sprint = false
            strafe.state = false
        }
    }


}