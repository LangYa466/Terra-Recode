/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.api.enums.BlockType
import net.ccbluex.liquidbounce.api.minecraft.client.block.IBlock
import net.ccbluex.liquidbounce.api.minecraft.item.IItem
import net.ccbluex.liquidbounce.event.ClickWindowEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.item.Item
import java.util.*

class InventoryUtils : MinecraftInstance(), Listenable {
    @EventTarget
    fun onClick(event: ClickWindowEvent?) {
        CLICK_TIMER.reset()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (classProvider.isCPacketPlayerBlockPlacement(packet)) CLICK_TIMER.reset()
    }

    override fun handleEvents(): Boolean {
        return true
    }

    companion object {
        @JvmField
        val CLICK_TIMER: MSTimer = MSTimer()

        @JvmField
        val BLOCK_BLACKLIST: List<IBlock> = Arrays.asList(
            classProvider.getBlockEnum(BlockType.CHEST),
            classProvider.getBlockEnum(BlockType.ENDER_CHEST),
            classProvider.getBlockEnum(BlockType.TRAPPED_CHEST),
            classProvider.getBlockEnum(BlockType.ANVIL),
            classProvider.getBlockEnum(BlockType.SAND),
            classProvider.getBlockEnum(BlockType.WEB),
            classProvider.getBlockEnum(BlockType.TORCH),
            classProvider.getBlockEnum(BlockType.CRAFTING_TABLE),
            classProvider.getBlockEnum(BlockType.FURNACE),
            classProvider.getBlockEnum(BlockType.WATERLILY),
            classProvider.getBlockEnum(BlockType.DISPENSER),
            classProvider.getBlockEnum(BlockType.STONE_PRESSURE_PLATE),
            classProvider.getBlockEnum(BlockType.WODDEN_PRESSURE_PLATE),
            classProvider.getBlockEnum(BlockType.NOTEBLOCK),
            classProvider.getBlockEnum(BlockType.DROPPER),
            classProvider.getBlockEnum(BlockType.TNT),
            classProvider.getBlockEnum(BlockType.STANDING_BANNER),
            classProvider.getBlockEnum(BlockType.WALL_BANNER),
            classProvider.getBlockEnum(BlockType.REDSTONE_TORCH),
            classProvider.getBlockEnum(BlockType.DIAMOND_ORE),
            classProvider.getBlockEnum(BlockType.ENCHANTING_TABLE)
        )

        @JvmStatic
        fun findItem(startSlot: Int, endSlot: Int, item: IItem): Int {
            for (i in startSlot until endSlot) {
                val stack = mc.thePlayer!!.inventoryContainer.getSlot(i).stack
                if (stack != null && stack.item == item) return i
            }
            return -1
        }

        fun findItem2(startSlot: Int, endSlot: Int, item: Item): Int {
            for (i in startSlot until endSlot) {
                val stack = mc.thePlayer!!.inventoryContainer.getSlot(i).stack
                if (stack != null && stack.item === item) return i
            }
            return -1
        }

        fun hasSpaceHotbar(): Boolean {
            for (i in 36..44) {
                val stack = mc.thePlayer!!.inventory.getStackInSlot(i) ?: return true
            }

            return false
        }

        @JvmStatic
        fun findAutoBlockBlock(): Int {
            for (i in 36..44) {
                val itemStack = mc.thePlayer!!.inventoryContainer.getSlot(i).stack

                if (itemStack != null && classProvider.isItemBlock(itemStack.item) && itemStack.stackSize > 0) {
                    val itemBlock = itemStack.item!!.asItemBlock()
                    val block = itemBlock.block

                    if (block.isFullCube(block.defaultState!!) && !BLOCK_BLACKLIST.contains(block)
                        && !classProvider.isBlockBush(block)
                    ) return i
                }
            }

            for (i in 36..44) {
                val itemStack = mc.thePlayer!!.inventoryContainer.getSlot(i).stack

                if (itemStack != null && classProvider.isItemBlock(itemStack.item) && itemStack.stackSize > 0) {
                    val itemBlock = itemStack.item!!.asItemBlock()
                    val block = itemBlock.block

                    if (!BLOCK_BLACKLIST.contains(block) && !classProvider.isBlockBush(block)) return i
                }
            }

            return -1
        }
    }
}
