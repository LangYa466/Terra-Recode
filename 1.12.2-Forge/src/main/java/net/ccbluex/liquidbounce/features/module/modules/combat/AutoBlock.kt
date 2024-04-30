package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.api.enums.BlockType
import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock

@ModuleInfo(name = "AutoBlock", description = "Auto block when you are attacking a target.", category = ModuleCategory.COMBAT)
class AutoBlock : Module() {

    private var blockRange = FloatValue("Range", 5f, 0f, 8f)

    private var blocking = false
    private var hitable = false
    private var safeice = false
    private var aim = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val aura = Terra.moduleManager[KillAura::class.java]
        hitable = RotationUtils.isFaced(aura.target, 0.05)
        val thePlayer = mc.thePlayer ?: return
        if (!classProvider.isItemSword(mc.thePlayer!!.heldItem!!.item)) {
            mc.gameSettings.keyBindUseItem.pressed = mc.gameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)

        }

        if (thePlayer.onGround && !thePlayer.isOnLadder && !thePlayer.sneaking && thePlayer.sprinting && thePlayer.movementInput.moveForward > 0.0) {
            BlockUtils.getMaterial(thePlayer.position.down()).let {
                safeice = it == classProvider.getBlockEnum(BlockType.ICE) || it == classProvider.getBlockEnum(BlockType.ICE_PACKED)
            }
        }

        if (((aura.state) || aura.target != null) && !aura.target!!.isDead && classProvider.isItemSword(mc.thePlayer!!.heldItem!!.item)) {
            if (mc.thePlayer!!.getDistanceToEntity(aura.target!!) <= blockRange.get()) {
                if (!safeice) {
                    mc.gameSettings.keyBindUseItem.pressed = true
                    blocking = true
                }
            }
        } else {
            mc.gameSettings.keyBindUseItem.pressed = mc.gameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)
            blocking = false
        }

        val objectMouseOver = mc.objectMouseOver

        if (objectMouseOver != null
            && EntityUtils.isSelected(objectMouseOver.entityHit, true)
            && blocking && !hitable) {
            aim = true
        } else {
            aim = false
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val e = event.packet.unwrap()
        val aura = Terra.moduleManager[KillAura::class.java]

        if (!aim && (e is CPacketPlayerTryUseItemOnBlock) && classProvider.isItemSword(mc.thePlayer!!.heldItem!!.item) && blocking && !hitable && aura.state) {
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (!MovementUtils.isMoving) {
            return
        }

        if (event.eventState == EventState.PRE && classProvider.isItemSword(mc.thePlayer!!.heldItem!!.item)) {
            mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerBlockPlacement(mc.thePlayer!!.inventory.getCurrentItemInHand() as IItemStack))
        }
    }

    init {
        state = true
        array = true
    }

}