package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.api.enums.WEnumHand
import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.utils.ClientUtils.displayChatMessage
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.createUseItemPacket
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.EnumHand

@ModuleInfo(name = "AntiVoid", category = ModuleCategory.MOVEMENT, description = "Prevent you from falling into the void.")
open class AntiVoid : Module() {
    private val modeValue = ListValue("Mode", arrayOf("BlazeRod", "Stuck"), "BlazeRod")
    private val maxFallDistValue = IntegerValue("MaxFallDistance", 2, 1, 20)
    private val voidOnlyValue = BoolValue("OnlyVoid", true)
    private val debugValue = BoolValue("Debug", true).displayable { modeValue.get().equals("BlazeRod",ignoreCase = true) }

    private var blink = false
    private var canBlink = false
    private var canSpoof = false
    private var tried = false
    private var canrod = false
    private var flagged = false
    var a = false

    private var lastRecY = 0.0
    var packets = ArrayList<CPacketPlayer>()

    open fun isInVoid(): Boolean {
        for (i in 0..128) {
            if (MovementUtils.isOnGround(i.toDouble())) {
                return false
            }
        }
        return true
    }

    override fun onEnable() {
        blink = false
        canBlink = false
        canSpoof = false
        lastRecY = mc.thePlayer!!.posY
        tried = false
        canrod = false
        flagged = false
        a = true
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer!!.onGround) {
            tried = false
            flagged = false
        }

        when (modeValue.get().toLowerCase()) {
            "blazerod" -> {
                if (!voidOnlyValue.get() || checkVoid()) {
                    canrod = mc.thePlayer!!.fallDistance > maxFallDistValue.get()
                }
            }

            "stuck" -> {
                if (isInVoid() && mc.thePlayer!!.fallDistance > maxFallDistValue.get()) {
                    mc2.player.motionX = 0.0
                    mc2.player.motionY = 0.0
                    mc2.player.motionZ = 0.0
                }
            }
        }
    }

    private fun checkVoid(): Boolean {
        var i = (-(mc.thePlayer!!.posY - 1.4857625)).toInt()
        var dangerous = true
        while (i <= 0) {
            dangerous = mc.theWorld!!.getCollisionBoxes(
                mc.thePlayer!!.entityBoundingBox.offset(
                    mc.thePlayer!!.motionX * 0.5,
                    i.toDouble(),
                    mc.thePlayer!!.motionZ * 0.5
                )
            ).isEmpty()
            i++
            if (!dangerous) break
        }
        return dangerous
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet.unwrap()

        when (modeValue.get().toLowerCase()) {
            "stuck" -> {
                if (isInVoid() && mc.thePlayer!!.fallDistance > maxFallDistValue.get()) {
                    if (packet is CPacketPlayer && a) event.cancelEvent()
                    if (packet is SPacketPlayerPosLook) this.state = false
                    if (packet is CPacketPlayerTryUseItem && a) {
                        a = false
                        event.cancelEvent()
                        mc2.connection!!.sendPacket(
                            CPacketPlayer.Rotation(
                                mc2.player.rotationYaw,
                                mc2.player.rotationPitch,
                                mc2.player.onGround
                            )
                        )
                        mc2.connection!!.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                    }
                }
            }

            "blazerod" -> {
                val thePlayer = mc.thePlayer!!
                if (canrod && classProvider.isCPacketPlayer(packet)) {
                    if (debugValue.get()) displayChatMessage("try get ITEM")
                    val blazerod = getItemFromHotbar(369)
                    if (blazerod == -1) return
                    if (debugValue.get()) displayChatMessage("try use ITEM")
                    mc.netHandler.addToSendQueue(classProvider.createCPacketHeldItemChange(blazerod))
                    mc.netHandler.addToSendQueue(
                        createUseItemPacket(
                            thePlayer.inventory.getStackInSlot(blazerod),
                            WEnumHand.MAIN_HAND
                        )
                    )
                    mc.netHandler.addToSendQueue(classProvider.createCPacketHeldItemChange(thePlayer.inventory.currentItem))

                }
            }
        }
    }

    private fun getItemFromHotbar(id: Int): Int {
        for (i in 0..8) {
            if (mc.thePlayer!!.inventory.mainInventory[i] != null) {
                val a: IItemStack? = mc.thePlayer!!.inventory.mainInventory[i]
                val item = a!!.item
                if (functions.getIdFromItem(item!!) == id) {
                    return i
                }
            }
        }
        return -1
    }

}
