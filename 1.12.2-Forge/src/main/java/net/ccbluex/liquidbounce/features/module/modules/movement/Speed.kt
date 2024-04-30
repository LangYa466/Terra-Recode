/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase

@ModuleInfo(name = "Speed", description = "Allows you to move faster.", category = ModuleCategory.MOVEMENT)
class Speed : Module() {
    val modeValue = ListValue("Mode", arrayOf("Watchdog", "GrimAC", "Legit"),"Watchdog")
    val grimMode = ListValue("GrimACMode", arrayOf("StrafeHop", "EntitySpeed", "All"),"Watchdog")
    private val alwayJump = BoolValue("AlwayJump",false)
    private val speed = IntegerValue("EntityBoxSpeed", 0, 0, 20)
    private val strafe = FloatValue("StrafeSpeed",0.25F,0F,0.5F)
    private val distanceToEntityBox = FloatValue("EntityBoxDistance",0.25F,0.00F,3.00f)
    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
    }
    @EventTarget
    fun onUpdate() {
        if ((grimMode.get()=="StrafeHop" && modeValue.get()=="GrimAC") || (modeValue.get()=="Legit") || (modeValue.get()=="Watchdog")) {
            //防止没脑子的乱点空格
            mc.gameSettings.keyBindJump.pressed = false
        }
    }
    @EventTarget
    fun onMotion(event: MotionEvent) {
        when (modeValue.get()) {
            "GrimAC" -> {
                if (grimMode.get()=="StrafeHop" && alwayJump.get()) {
                    if (mc2.player.isInWater || mc2.player.isInLava) return
                    if (event.eventState == EventState.PRE) {
                        if (MovementUtils.isMoving) {
                            if (mc.thePlayer!!.onGround) mc.thePlayer!!.jump()
                        }
                    }
                }
                for (entity in mc.theWorld!!.loadedEntityList) {
                    if (entity.unwrap() is EntityLivingBase && entity.entityId != mc2.player!!.entityId && mc.thePlayer!!.getDistanceToEntityBox(entity) <= distanceToEntityBox.get()) {
                        when (grimMode.get()) {
                            "StrafeHop" -> {
                                if (event.eventState == EventState.PRE) {
                                    if (MovementUtils.isMoving && !alwayJump.get()) {
                                        if (mc.thePlayer!!.onGround) mc.thePlayer!!.jump()
                                    }
                                }
                                MovementUtils.strafe(strafe.get())
                                return
                            }
                            "EntitySpeed" -> {
                                mc.thePlayer!!.motionX *= (1 + (speed.get() * 0.01))
                                mc.thePlayer!!.motionZ *= (1 + (speed.get() * 0.01))
                                return
                            }
                            "All" -> {
                                mc.thePlayer!!.motionX *= (1 + (speed.get() * 0.01))
                                mc.thePlayer!!.motionZ *= (1 + (speed.get() * 0.01))
                                MovementUtils.strafe(0.45F)
                                return
                            }
                        }
                    }
                }
            }
            "Watchdog" -> {
                //Strafe & Speeder
                when (mc.thePlayer!!.hurtTime) {
                    0 , 4 , 6 -> {
                        MovementUtils.strafe(0.3f)
                        mc.timer.timerSpeed = 1.03f
                    }
                    10 -> {
                        MovementUtils.strafe(0.45f)
                        mc.timer.timerSpeed = 1.07f
                    }
                    8 -> {
                        MovementUtils.strafe(0.5f)
                        mc.timer.timerSpeed = 1.1f
                    }
                    2 -> MovementUtils.strafe(0.42f)
                    else -> {
                        MovementUtils.strafe(0.34f)
                        mc.timer.timerSpeed = 1.02f
                    }
                }
                if (event.eventState == EventState.PRE) {
                    if (mc.thePlayer!!.isInWater) return
                    if (MovementUtils.isMoving) {
                        if (mc.thePlayer!!.onGround) mc.thePlayer!!.jump()
                    }
                }
            }
            "Legit" -> {
                if (event.eventState == EventState.PRE) {
                    if (mc.thePlayer!!.isInWater) return
                    if (MovementUtils.isMoving) {
                        if (mc.thePlayer!!.onGround) mc.thePlayer!!.jump()
                    }
                }
            }
        }
    }
    override val tag: String
        get() = modeValue.get()
}