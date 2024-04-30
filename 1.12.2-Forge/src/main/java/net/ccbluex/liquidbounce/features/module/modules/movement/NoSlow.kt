package net.ccbluex.liquidbounce.features.module.modules.movement

import io.netty.buffer.Unpooled
import net.ccbluex.liquidbounce.api.enums.EnumFacingType
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketPlayerDigging
import net.ccbluex.liquidbounce.api.minecraft.util.WBlockPos
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.utils.misc.PacketUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.Minecraft
import net.minecraft.init.Items
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.*
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.SPacketSetSlot
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos

@ModuleInfo(name = "NoSlow", description = "Cancel slow effect while you try to using hinder items.", category = ModuleCategory.MOVEMENT)
class NoSlow : Module() {
    private val modeValue = ListValue("Mode", arrayOf("HytPit", "SwitchItem", "GrimAC", "Vanilla"), "SwitchItem")

    private val blockForwardMultiplier = FloatValue("BlockForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val blockStrafeMultiplier = FloatValue("BlockStrafeMultiplier", 1.0F, 0.2F, 1.0F)

    private val consumeForwardMultiplier = FloatValue("ConsumeForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val consumeStrafeMultiplier = FloatValue("ConsumeStrafeMultiplier", 1.0F, 0.2F, 1.0F)

    private val bowForwardMultiplier = FloatValue("BowForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val bowStrafeMultiplier = FloatValue("BowStrafeMultiplier", 1.0F, 0.2F, 1.0F)

    private val foodPacket = BoolValue("FoodPacket",true).displayable { modeValue.get().equals("SwitchItem",true) }
    private val delayValue = IntegerValue("Delay",3,1,20).displayable { modeValue.get().equals("SwitchItem",true) }
    private val fakeShieldValue = BoolValue("FakeShield",true)
    val soulsandValue = BoolValue("Soulsand", false)

    //跑吃的东西
    private var canEat = false
    private var a = false
    private var s = 0
    private var smartSpeed = false

    override fun onDisable() {
        canEat = false
        a = false
        s = 0
        smartSpeed = false
    }


    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (!mc.gameSettings.keyBindUseItem.isKeyDown) {
            a = false
            s = 0
            smartSpeed = false
            return
        }
        when (modeValue.get().toLowerCase()) {
            "Vanilla" -> {
                mc.thePlayer!!.motionX = mc.thePlayer!!.motionX
                mc.thePlayer!!.motionY = mc.thePlayer!!.motionY
                mc.thePlayer!!.motionZ = mc.thePlayer!!.motionZ
            }

            "grimac" -> {
                if ((event.eventState == EventState.PRE && mc.thePlayer!!.itemInUse != null && mc.thePlayer!!.itemInUse!!.item != null) && !mc.thePlayer!!.isBlocking && classProvider.isItemFood(
                        mc.thePlayer!!.heldItem!!.item
                    ) || classProvider.isItemPotion(mc.thePlayer!!.heldItem!!.item)
                ) {
                    if (mc.thePlayer!!.isUsingItem && mc.thePlayer!!.itemInUseCount >= 1) {
                        mc2.connection!!.sendPacket(CPacketHeldItemChange((mc2.player.inventory.currentItem + 1) % 9))
                        mc2.connection!!.sendPacket(CPacketHeldItemChange(mc2.player.inventory.currentItem))
                    }
                }
                if (event.eventState == EventState.PRE && classProvider.isItemSword(mc.thePlayer!!.heldItem!!.item)) {
                    mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerDigging(ICPacketPlayerDigging.WAction.RELEASE_USE_ITEM, WBlockPos.ORIGIN, classProvider.getEnumFacing(EnumFacingType.DOWN)))
                }
            }

            "switchitem" -> {
                if (mc2.player.heldItemMainhand.item is ItemSword && mc.gameSettings.keyBindUseItem.isKeyDown) {
                    when(event.eventState) {
                        EventState.PRE -> {
                            mc2.connection!!.sendPacket(CPacketHeldItemChange(mc.thePlayer!!.inventory.currentItem + 1))
                            mc2.connection!!.sendPacket(CPacketCustomPayload("L", PacketBuffer(Unpooled.buffer())))
                            mc2.connection!!.sendPacket(CPacketHeldItemChange(mc.thePlayer!!.inventory.currentItem))
                        }

                        EventState.POST -> {
                            PacketUtils.sendPacketC0F()
                            mc2.connection!!.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                        }
                    }
                }

                //跑弓
                if (mc2.player.heldItemMainhand.item is ItemBow && mc.gameSettings.keyBindUseItem.isKeyDown) {
                    val curSlot = mc.thePlayer!!.inventory.currentItem
                    val spoof = if (curSlot == 0) 1 else -1
                    if (event.eventState == EventState.PRE) {
                        mc.netHandler.addToSendQueue(classProvider.createCPacketHeldItemChange(curSlot + spoof))
                        mc2.player.connection.sendPacket(CPacketPlayerTryUseItem(EnumHand.OFF_HAND))
                        mc.netHandler.addToSendQueue(classProvider.createCPacketHeldItemChange(curSlot))
                    }
                    if (event.eventState == EventState.POST) {
                        mc2.player.connection.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                    }
                }

                //跑吃
                if (mc2.player.heldItemMainhand.item is ItemFood && canEat && mc.gameSettings.keyBindUseItem.isKeyDown && foodPacket.get()) {
                    if (mc2.player.heldItemMainhand.count != 1) {
                        mc2.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.DROP_ITEM, BlockPos(0, 0, 0), EnumFacing.DOWN))
                        a = true
                        canEat = false
                    }
                }

            }

            "hytpit" -> {
                //Sword
                if (mc2.player.heldItemMainhand.item is ItemSword && mc.gameSettings.keyBindUseItem.isKeyDown) {
                    when(event.eventState) {
                        EventState.PRE -> {
                            mc2.connection!!.sendPacket(CPacketHeldItemChange(mc.thePlayer!!.inventory.currentItem + 1))
                            mc2.connection!!.sendPacket(CPacketCustomPayload("L", PacketBuffer(Unpooled.buffer())))
                            mc2.connection!!.sendPacket(CPacketHeldItemChange(mc.thePlayer!!.inventory.currentItem))
                        }

                        EventState.POST -> {
                            PacketUtils.sendPacketC0F()
                            mc2.connection!!.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                        }
                    }
                }

                //Food
                if ((mc2.player.heldItemMainhand.item is ItemFood || mc2.player.heldItemMainhand.item is ItemPotion)
                    && (event.eventState == EventState.PRE && mc.thePlayer!!.itemInUse != null && mc.thePlayer!!.itemInUse!!.item != null)
                    && !mc.thePlayer!!.isBlocking) {
                    if (mc.thePlayer!!.isUsingItem && mc.thePlayer!!.itemInUseCount >= 1) {
                        mc2.connection!!.sendPacket(CPacketHeldItemChange((mc2.player.inventory.currentItem + 1) % 9))
                        mc2.connection!!.sendPacket(CPacketHeldItemChange(mc2.player.inventory.currentItem))
                    }
                }

                //Bow
                if (mc2.player.heldItemMainhand.item is ItemBow && mc.gameSettings.keyBindUseItem.isKeyDown) {
                    val curSlot = mc.thePlayer!!.inventory.currentItem
                    val spoof = if (curSlot == 0) 1 else -1
                    if (event.eventState == EventState.PRE) {
                        mc.netHandler.addToSendQueue(classProvider.createCPacketHeldItemChange(curSlot + spoof))
                        mc2.player.connection.sendPacket(CPacketPlayerTryUseItem(EnumHand.OFF_HAND))
                        mc.netHandler.addToSendQueue(classProvider.createCPacketHeldItemChange(curSlot))
                    }
                    if (event.eventState == EventState.POST) {
                        mc2.player.connection.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                    }
                }

            }

        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        //假盾 判断手里拿剑然后生成盾牌
        if (fakeShieldValue.get()) {
            val player = Minecraft.getMinecraft().player
            val offHandStack = player?.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND)

            if (offHandStack!!.isEmpty && classProvider.isItemSword(mc.thePlayer!!.heldItem!!.item)) player.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack(Items.SHIELD))
            if (!classProvider.isItemSword(mc.thePlayer!!.heldItem!!.item)) player.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY)
        }

        if (a) {
            s++
        }
        if (s == delayValue.get()) {
            smartSpeed = true
        }

    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet.unwrap()
        val heldItem = mc2.player.heldItemMainhand.item
        if (heldItem is ItemFood && !a) {
            canEat = true
        }
        if (packet is SPacketSetSlot && a) {
            event.cancelEvent()
        }

    }

    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        val heldItemMainhand = mc2.player!!.heldItemMainhand.item

        event.forward = getMultiplier(heldItemMainhand, true)
        event.strafe = getMultiplier(heldItemMainhand, false)
    }

    private fun getMultiplier(item: Item?, isForward: Boolean): Float {
        return when (item) {
            is ItemFood, is ItemPotion, is ItemBucketMilk -> {
                if (smartSpeed) {
                    if (isForward) this.consumeForwardMultiplier.get() else this.consumeStrafeMultiplier.get()
                } else 0.2F
            }

            is ItemSword -> {
                if (isForward) this.blockForwardMultiplier.get() else this.blockStrafeMultiplier.get()
            }

            is ItemBow -> {
                if (isForward) this.bowForwardMultiplier.get() else this.bowStrafeMultiplier.get()
            }

            else -> 0.2F
        }
    }

    override val tag: String
        get() = modeValue.get()
}