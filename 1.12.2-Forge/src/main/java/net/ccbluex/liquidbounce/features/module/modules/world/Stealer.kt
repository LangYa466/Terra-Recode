package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.api.minecraft.client.gui.inventory.IGuiChest
import net.ccbluex.liquidbounce.api.minecraft.inventory.ISlot
import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.player.InvManager
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.item.ItemUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.inventory.ClickType
import net.minecraft.network.play.client.CPacketClickWindow
import net.minecraft.network.play.client.CPacketConfirmTransaction
import kotlin.random.Random

@ModuleInfo(name = "Stealer", description = "Automatically steals all items from a chest.", category = ModuleCategory.WORLD)
class Stealer : Module() {


    /**
     * OPTIONS
     */
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 0, 0, 400) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minDelayValue.get()
            if (i > newValue)
                set(i)

            nextDelay = TimeUtils.randomDelay(minDelayValue.get(), get())
        }
    }

    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 0, 0, 400) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxDelayValue.get()

            if (i < newValue)
                set(i)

            nextDelay = TimeUtils.randomDelay(get(), maxDelayValue.get())
        }
    }
    private val delayOnFirstValue = BoolValue("DelayOnFirst", false).displayable { !instantValue.get() }

    private val takeRandomizedValue = BoolValue("TakeRandomized", false).displayable { !instantValue.get() }
    private val onlyItemsValue = BoolValue("OnlyItems", false).displayable { !instantValue.get() }
    private val noCompassValue = BoolValue("NoCompass", false).displayable { !instantValue.get() }
    private val noMoveValue = BoolValue("NoMove", false).displayable { !instantValue.get() }
    private val noAirValue = BoolValue("NoAir", false).displayable { !instantValue.get() }
    private val combatCloseValue = BoolValue("DamageClose", false)
    private val instantValue = BoolValue("Instant", true)
    private val normalMoveMode = ListValue("NormalStealMode", arrayOf("Packet", "Normal"), "Packet").displayable { !instantValue.get() }
    private val autoCloseValue = BoolValue("AutoClose", false).displayable { !instantValue.get() }
    private val autoCloseMaxDelayValue: IntegerValue = object : IntegerValue("AutoCloseMaxDelay", 0, 0, 400) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = autoCloseMinDelayValue.get()
            if (i > newValue) set(i)
            nextCloseDelay = TimeUtils.randomDelay(autoCloseMinDelayValue.get(), this.get())
        }
    }.displayable { autoCloseValue.get() && !instantValue.get() } as IntegerValue

    private val autoCloseMinDelayValue: IntegerValue = object : IntegerValue("AutoCloseMinDelay", 0, 0, 400) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = autoCloseMaxDelayValue.get()
            if (i < newValue) set(i)
            nextCloseDelay = TimeUtils.randomDelay(this.get(), autoCloseMaxDelayValue.get())
        }
    }.displayable { autoCloseValue.get() && !instantValue.get() } as IntegerValue

    private val closeOnFullValue = BoolValue("CloseOnFull", true).displayable { !instantValue.get() }


    /**
     * VALUES
     */

    private var stealing = false
    private val delayTimer = MSTimer()
    private var nextDelay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())

    private val autoCloseTimer = MSTimer()
    private var nextCloseDelay = TimeUtils.randomDelay(autoCloseMinDelayValue.get(), autoCloseMaxDelayValue.get())

    private var contentReceived = 0

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        val thePlayer = mc.thePlayer!!

        if (!classProvider.isGuiChest(mc.currentScreen) || mc.currentScreen == null) {
            if (delayOnFirstValue.get())
                delayTimer.reset()
            autoCloseTimer.reset()
            stealing = false
            return
        }

        if (combatCloseValue.get() && mc2.player.hurtTime != 0) {
            thePlayer.closeScreen()
            stealing = false
            return
        }


        if (!delayTimer.hasTimePassed(nextDelay)) {
            autoCloseTimer.reset()
            return
        }

        val screen = mc.currentScreen!!.asGuiChest()

        // No Compass
        if (noCompassValue.get() && thePlayer.inventory.getCurrentItemInHand()?.item?.unlocalizedName == "item.compass")
            return

        // inventory cleaner
        val invManager = Terra.moduleManager.getModule(InvManager::class.java)

        // Is empty?
        if (!isEmpty(screen) && (!closeOnFullValue.get() || !fullInventory)) {

            stealing = true

            autoCloseTimer.reset()

            if ((noMoveValue.get() && isMoving) || (noAirValue.get() && !mc2.player.onGround)) return

            // Randomized
            if (takeRandomizedValue.get()) {
                do {
                    val items = mutableListOf<ISlot>()

                    for (slotIndex in 0 until screen.inventoryRows * 9) {
                        val slot = screen.inventorySlots!!.getSlot(slotIndex)

                        val stack = slot.stack

                        if (stack != null && (!onlyItemsValue.get() || !classProvider.isItemBlock(stack.item)) && (invManager.isUseful(
                                stack,
                                -1
                            ))
                        )
                            items.add(slot)
                    }

                    val randomSlot = Random.nextInt(items.size)
                    val slot = items[randomSlot]

                    move(screen, slot)
                } while (delayTimer.hasTimePassed(nextDelay) && items.isNotEmpty())
                return
            }

            // Non randomized
            for (slotIndex in 0 until screen.inventoryRows * 9) {
                val slot = screen.inventorySlots!!.getSlot(slotIndex)

                val stack = slot.stack

                if (delayTimer.hasTimePassed(nextDelay) && shouldTake(stack, invManager)) {
                    move(screen, slot)
                }
            }

            //AutoClose
        } else if (screen.inventorySlots!!.windowId == contentReceived && autoCloseTimer.hasTimePassed(nextCloseDelay) && autoCloseValue.get()) {
            thePlayer.closeScreen()
            stealing = false
            nextCloseDelay = TimeUtils.randomDelay(autoCloseMinDelayValue.get(), autoCloseMaxDelayValue.get())
        }
    }

    override fun onEnable() {
        stealing = false
    }

    override fun onDisable() {
        stealing = false
    }

    @EventTarget
    private fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (classProvider.isSPacketWindowItems(packet))
            contentReceived = packet.asSPacketWindowItems().windowId

    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (instantValue.get()) {
            if (classProvider.isGuiChest(mc.currentScreen)) {
                val chest = mc.currentScreen?.asGuiChest()
                val rows = chest?.inventoryRows!! * 9
                for (i in 0 until rows) {
                    val slot = chest.inventorySlots?.getSlot(i)
                    if (slot!!.hasStack) {
                        mc2.connection?.sendPacket(
                            CPacketClickWindow(
                                chest.inventorySlots?.windowId!!,
                                i,
                                0,
                                ClickType.QUICK_MOVE,
                                slot.stack!!.unwrap(),
                                1.toShort()
                            )
                        )
                        mc2.connection!!.sendPacket(
                            CPacketConfirmTransaction(
                                chest.inventorySlots!!.windowId,
                                1.toShort(),
                                true
                            )
                        )
                    }
                }
                mc.thePlayer?.closeScreen()
            }
        }
    }

    private fun shouldTake(stack: IItemStack?, invManager: InvManager): Boolean {
        return stack != null && !ItemUtils.isStackEmpty(stack) && (!onlyItemsValue.get() || !classProvider.isItemBlock(
            stack.item
        )) && (invManager.isUseful(stack, -1))
    }


    private fun move(screen: IGuiChest, slot: ISlot) {
        when (normalMoveMode.get().toLowerCase()) {
            "packet" -> {
                mc2.connection?.sendPacket(
                    CPacketClickWindow(
                        screen.inventorySlots?.windowId!!,
                        slot.slotNumber,
                        0,
                        ClickType.QUICK_MOVE,
                        slot.stack!!.unwrap(),
                        1.toShort()
                    )
                )
                mc2.connection!!.sendPacket(
                    CPacketConfirmTransaction(
                        screen.inventorySlots!!.windowId,
                        1.toShort(),
                        true
                    )
                )
            }

            "normal" -> {
                screen.handleMouseClick(slot, slot.slotNumber, 0, 1)
            }
        }
        delayTimer.reset()
        nextDelay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
    }

    private fun isEmpty(chest: IGuiChest): Boolean {
        val invManager = Terra.moduleManager.getModule(InvManager::class.java)

        for (i in 0 until chest.inventoryRows * 9) {
            val slot = chest.inventorySlots!!.getSlot(i)

            val stack = slot.stack

            if (shouldTake(stack, invManager))
                return false
        }

        return true
    }

    private val fullInventory: Boolean
        get() = mc.thePlayer?.inventory?.mainInventory?.none(ItemUtils::isStackEmpty) ?: false

    override val tag: String
        get() = if (instantValue.get()) "Instant" else "${minDelayValue.get()} - ${maxDelayValue.get()}"
}