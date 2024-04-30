package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntity
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketEntityAction
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketUseEntity
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.utils.RaycastUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.CPacketConfirmTransaction
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketExplosion

@ModuleInfo(name = "Velocity", description = "Allows you to modify the amount of KnockBack you take.", category = ModuleCategory.COMBAT)
class Velocity : Module() {

    /**
     * OPTIONS
     */
    val modeValue = ListValue("Mode", arrayOf("Simple", "Jump", "NoXZ"), "NoXZ")

    // Simple
    private val horizontalValue = FloatValue("Horizontal", 0F, 0F, 1F).displayable { modeValue.get().equals("Simple", ignoreCase = true) }
    private val verticalValue = FloatValue("Vertical", 0F, 0F, 1F).displayable { modeValue.get().equals("Simple", ignoreCase = true) }
    private val reachValue = FloatValue("Reach", 3F, 1F, 8F).displayable { modeValue.get().equals("NoXZ", ignoreCase = true) }

    private val c02 = IntegerValue("C02", 5, 0, 10).displayable { modeValue.get().equals("NoXZ", ignoreCase = true) }
    private val c0f = IntegerValue("C0F", 5, 0, 10).displayable { modeValue.get().equals("NoXZ", ignoreCase = true) }
    private val c0b = BoolValue("C0B", true).displayable { modeValue.get().equals("NoXZ", ignoreCase = true) }

    private var grimNoAntiKB = 0
    private var cancel = false
    private var lag = false

    /**
     * VALUES
     */
    private var velocityTimer = MSTimer()
    private var velocityInput = false
    private var press = false
    private var grimReduce = 0

    var x = 0.0
    var y = -0.1
    var z = 0.0

    override val tag: String
        get() = modeValue.get()

    override fun onDisable() {
        cancel = false
        grimNoAntiKB = 0
        mc.thePlayer?.speedInAir = 0.02F
        grimReduce = 0
        velocityInput = false
        mc.timer.timerSpeed = 1f
        press = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer!!.isInWater || mc.thePlayer!!.isInLava || mc.thePlayer!!.isInWeb)
            return

        if (mc.thePlayer == null)
            return

        when (modeValue.get().toLowerCase()) {
            "noxz" -> {
                if (velocityInput) {
                    if (mc.thePlayer!!.hurtTime == 0)
                        velocityInput = false
                }
            }
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        when (modeValue.get().toLowerCase()) {
            "noxz" -> {
                if (velocityInput) {
                    if (c0b.get() && !mc2.player.isSprinting) {
                        val player = mc.thePlayer ?: return
                        if (player.sprinting) mc.netHandler.addToSendQueue(classProvider.createCPacketEntityAction(player, ICPacketEntityAction.WAction.STOP_SPRINTING))
                        mc.netHandler.addToSendQueue(classProvider.createCPacketEntityAction(player, ICPacketEntityAction.WAction.START_SPRINTING))

                        player.sprinting = true
                        player.serverSprintState = true
                    }

                    mc2.player.motionX *= 0.6
                    mc2.player.motionZ *= 0.6
                }
            }
        }
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        when (modeValue.get().toLowerCase()) {
            "noxz" -> {
                if (velocityInput) {
                    val reach = reachValue.get().toDouble()
                    val currentRotation = RotationUtils.serverRotation!!

                    val raycastedEntity = RaycastUtils.raycastEntity(reach, currentRotation.yaw, currentRotation.pitch,
                        object : RaycastUtils.EntityFilter {
                            override fun canRaycast(entity: IEntity?): Boolean {
                                return true
                            }
                        }
                    )

                    if (raycastedEntity != null && raycastedEntity != mc.thePlayer) {
                        repeat(c02.get()) {
                            Terra.eventManager.callEvent(AttackEvent(raycastedEntity))
                            mc.netHandler.addToSendQueue(classProvider.createCPacketUseEntity(raycastedEntity, ICPacketUseEntity.WAction.ATTACK))
                            repeat(c0f.get()) {
                                mc2.connection!!.sendPacket(CPacketConfirmTransaction(0, 19198.toShort(), true))
                            }

                            mc.thePlayer!!.swingItem()
                        }

                        velocityInput = false
                    }
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet.unwrap()

        if (packet is SPacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer)
                return

            velocityTimer.reset()

            x = mc.thePlayer!!.posX
            y = mc.thePlayer!!.posY
            z = mc.thePlayer!!.posZ

            when (modeValue.get().toLowerCase()) {
                "simple" -> {
                    val horizontal = horizontalValue.get()
                    val vertical = verticalValue.get()

                    if (horizontal == 0F && vertical == 0F)
                        event.cancelEvent()

                    packet.motionX = (packet.getMotionX() * horizontal).toInt()
                    packet.motionY = (packet.getMotionY() * vertical).toInt()
                    packet.motionZ = (packet.getMotionZ() * horizontal).toInt()
                }

                "jump" -> {
                    var start = 0
                    while (mc.thePlayer!!.hurtTime >= 8) {
                        mc.gameSettings.keyBindJump.pressed = true
                        break
                    }

                    while (mc.thePlayer!!.hurtTime >=7 && !mc.gameSettings.keyBindForward.pressed) {
                        mc.gameSettings.keyBindForward.pressed = true
                        start = 1
                        break
                    }

                    if (mc.thePlayer!!.hurtTime in 1..6) {
                        mc.gameSettings.keyBindJump.pressed = false
                        if (start == 1) {
                            mc.gameSettings.keyBindForward.pressed = false
                            start = 0
                        }
                    }
                }

                "noxz" -> velocityInput = true
            }
        }

        if (packet is SPacketExplosion) {
            // TODO: Support velocity for explosions
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        lag = event.eventState == EventState.PRE
    }
}
