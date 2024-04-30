package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.api.minecraft.util.IAxisAlignedBB
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "Fly", description = "Test", category = ModuleCategory.MOVEMENT)
class Fly : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Vanilla", "SmoothVanilla", "KeepAlive"),"Vanilla")
    private val vanillaSpeedValue = FloatValue("VanillaSpeed", 2f, 0f, 5f).displayable { !modeValue.get().equals("SmoothVanilla",true) }
    private val vanillaKickBypassValue = BoolValue("VanillaKickBypass", false)

    private val groundTimer = MSTimer()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val vanillaSpeed = vanillaSpeedValue.get()
        val thePlayer = mc.thePlayer!!

        run {
            when (modeValue.get().toLowerCase()) {
                "vanilla" -> {
                    thePlayer.capabilities.isFlying = false
                    thePlayer.motionY = 0.0
                    thePlayer.motionX = 0.0
                    thePlayer.motionZ = 0.0
                    if (mc.gameSettings.keyBindJump.isKeyDown) thePlayer.motionY += vanillaSpeed
                    if (mc.gameSettings.keyBindSneak.isKeyDown) thePlayer.motionY -= vanillaSpeed
                    MovementUtils.strafe(vanillaSpeed)
                    handleVanillaKickBypass()
                }

                "smoothvanilla" -> {
                    thePlayer.capabilities.isFlying = true
                    handleVanillaKickBypass()
                }

                "keepalive" -> {
                    mc.netHandler.addToSendQueue(classProvider.createCPacketKeepAlive())
                    thePlayer.capabilities.isFlying = false
                    thePlayer.motionY = 0.0
                    thePlayer.motionX = 0.0
                    thePlayer.motionZ = 0.0
                    if (mc.gameSettings.keyBindJump.isKeyDown) thePlayer.motionY += vanillaSpeed
                    if (mc.gameSettings.keyBindSneak.isKeyDown) thePlayer.motionY -= vanillaSpeed
                    MovementUtils.strafe(vanillaSpeed)
                }
            }
        }

    }

    private fun handleVanillaKickBypass() {
        if (!vanillaKickBypassValue.get() || !groundTimer.hasTimePassed(1000)) return
        val ground = calculateGround()
        run {
            var posY = mc.thePlayer!!.posY
            while (posY > ground) {
                mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(mc.thePlayer!!.posX, posY, mc.thePlayer!!.posZ, true))
                if (posY - 8.0 < ground) break // Prevent next step
                posY -= 8.0
            }
        }

        mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(mc.thePlayer!!.posX, ground, mc.thePlayer!!.posZ, true))
        var posY = ground
        while (posY < mc.thePlayer!!.posY) {
            mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(mc.thePlayer!!.posX, posY, mc.thePlayer!!.posZ, true))
            if (posY + 8.0 > mc.thePlayer!!.posY) break // Prevent next step
            posY += 8.0
        }

        mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerPosition(mc.thePlayer!!.posX, mc.thePlayer!!.posY, mc.thePlayer!!.posZ, true))
        groundTimer.reset()
    }

    // TODO: Make better and faster calculation lol
    private fun calculateGround(): Double {
        val playerBoundingBox: IAxisAlignedBB = mc.thePlayer!!.entityBoundingBox
        var blockHeight = 1.0
        var ground = mc.thePlayer!!.posY
        while (ground > 0.0) {
            val customBox = classProvider.createAxisAlignedBB(playerBoundingBox.maxX, ground + blockHeight, playerBoundingBox.maxZ, playerBoundingBox.minX, ground, playerBoundingBox.minZ)
            if (mc.theWorld!!.checkBlockCollision(customBox)) {
                if (blockHeight <= 0.05) return ground + blockHeight
                ground += blockHeight
                blockHeight = 0.05
            }

            ground -= blockHeight
        }

        return 0.0
    }

}