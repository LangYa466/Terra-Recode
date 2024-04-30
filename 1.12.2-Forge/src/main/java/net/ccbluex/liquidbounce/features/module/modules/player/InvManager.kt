/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.Terra.combatManager
import net.ccbluex.liquidbounce.api.enums.BlockType
import net.ccbluex.liquidbounce.api.enums.EnchantmentType
import net.ccbluex.liquidbounce.api.enums.WEnumHand
import net.ccbluex.liquidbounce.api.minecraft.item.IItem
import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.item.ArmorComparator
import net.ccbluex.liquidbounce.utils.item.ArmorPiece
import net.ccbluex.liquidbounce.utils.item.ItemUtils
import net.ccbluex.liquidbounce.utils.createUseItemPacket
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.inventory.ClickType
import net.minecraft.network.play.client.CPacketClickWindow
import net.minecraft.network.play.client.CPacketConfirmTransaction
import java.util.stream.Collectors
import java.util.stream.IntStream

@ModuleInfo(name = "InvManager", description = "Automatically throws away useless items.", category = ModuleCategory.PLAYER)
class InvManager : Module() {

    /**
     * OPTIONS
     */

    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 0, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val minDelay = minDelayValue.get()
            if (minDelay > newValue) set(minDelay)
        }
    }

    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 0, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val maxDelay = maxDelayValue.get()
            if (maxDelay < newValue) set(maxDelay)
        }
    }

    private val invOpenValue = BoolValue("InvOpen", false)
    private val simulateInventory = BoolValue("SimulateInventory", true)
    private val noMoveValue = BoolValue("NoMove", false)
    private val swingValue = BoolValue("SwingItem", false)
    private val ignoreChainArmorValue = BoolValue("HYT-NoChainArmor", true)
    private val ignoreVehiclesValue = BoolValue("IgnoreVehicles", true)
    private val randomSlotValue = BoolValue("RandomSlot", false)
    private val sortValue = BoolValue("Sort", true)
    private val noCombatValue = BoolValue("NoCombat", false)
    private val itemDelayValue = IntegerValue("ItemDelay", 0, 0, 5000)
    private val swapArmorModeValue = ListValue("SwapMode", arrayOf("Packet", "WindowClick"), "Packet")
    private val items = arrayOf("None", "Ignore", "Sword", "Bow", "Pickaxe", "Axe", "Block", "Water", "Gapple", "Pearl")
    private val sortSlot1Value = ListValue("SortSlot-1", items, "Sword").displayable { sortValue.get() }
    private val sortSlot2Value = ListValue("SortSlot-2", items, "Block").displayable { sortValue.get() }
    private val sortSlot3Value = ListValue("SortSlot-3", items, "Bow").displayable { sortValue.get() }
    private val sortSlot4Value = ListValue("SortSlot-4", items, "Gapple").displayable { sortValue.get() }
    private val sortSlot5Value = ListValue("SortSlot-5", items, "Pearl").displayable { sortValue.get() }
    private val sortSlot6Value = ListValue("SortSlot-6", items, "None").displayable { sortValue.get() }
    private val sortSlot7Value = ListValue("SortSlot-7", items, "Water").displayable { sortValue.get() }
    private val sortSlot8Value = ListValue("SortSlot-8", items, "Pickaxe").displayable { sortValue.get() }
    private val sortSlot9Value = ListValue("SortSlot-9", items, "Axe").displayable { sortValue.get() }
    private var delay: Long = 0
    private var locked = false


    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (!InventoryUtils.CLICK_TIMER.hasTimePassed(delay) || (combatManager.inCombat && noCombatValue.get())
            || (!classProvider.isGuiInventory(mc.currentScreen) && invOpenValue.get()) || noMoveValue.get() && MovementUtils.isMoving
            || thePlayer.openContainer != null && thePlayer.openContainer!!.windowId != 0) return

        while (InventoryUtils.CLICK_TIMER.hasTimePassed(delay)) {

            // Find best armor
            val armorPieces = IntStream.range(0, 36).filter { i: Int ->
                val itemStack = mc.thePlayer!!.inventory.getStackInSlot(i)
                itemStack != null && classProvider.isItemArmor(itemStack.item) && (i < 9 || System.currentTimeMillis() - itemStack.itemDelay >= itemDelayValue.get())
            }.mapToObj { i: Int ->
                ArmorPiece(
                    mc.thePlayer!!.inventory.getStackInSlot(
                        i
                    ), i
                )
            }.collect(Collectors.groupingBy<ArmorPiece?, Int> { obj: ArmorPiece? -> obj!!.armorType })
            val bestArmor = arrayOfNulls<ArmorPiece>(4)
            for ((key, value) in armorPieces) {
                bestArmor[key] = value.stream().max(ARMOR_COMPARATOR).orElse(null)
            }

            // Swap armor
            for (i in 0..3) {
                val armorPiece = bestArmor[i] ?: continue
                if ((ignoreChainArmorValue.get() && !(armorPiece.itemStack.unlocalizedName == "item.helmetChain" || armorPiece.itemStack.unlocalizedName == "item.leggingsChain")) || (!ignoreChainArmorValue.get())) {
                    val armorSlot = 3 - i
                    val oldArmor = ArmorPiece(mc.thePlayer!!.inventory.armorItemInSlot(armorSlot), -1)
                    if (ItemUtils.isStackEmpty(oldArmor.itemStack) || !classProvider.isItemArmor(oldArmor.itemStack.item) || ARMOR_COMPARATOR.compare(
                            oldArmor,
                            armorPiece
                        ) < 0
                    ) {
                        if (!ItemUtils.isStackEmpty(oldArmor.itemStack) && move(8 - (3 - armorSlot), true)) {
                            locked = true
                            return
                        }
                        if (ItemUtils.isStackEmpty(mc.thePlayer!!.inventory.armorItemInSlot(armorSlot)) && move(
                                armorPiece.slot,
                                false
                            )
                        ) {
                            locked = true
                            return
                        }
                    }
                }
            }
            locked = false

            if (sortValue.get()) sortHotbar()

            val garbageItems =
                items(9, 45).filter { !isUseful(it.value, it.key) }.keys.toMutableList()

            // Shuffle items
            if (randomSlotValue.get()) garbageItems.shuffle()

            val garbageItem = garbageItems.firstOrNull() ?: break

            // Drop all useless items
            val openInventory = !classProvider.isGuiInventory(mc.currentScreen) && simulateInventory.get()

            //if (openInventory) mc.netHandler.addToSendQueue(createOpenInventoryPacket())

            if (swingValue.get()) mc.thePlayer!!.swingItem()
            mc.playerController.windowClick(
                thePlayer.openContainer!!.windowId,
                garbageItem,
                1,
                4,
                thePlayer
            )
            if (openInventory) mc.netHandler.addToSendQueue(classProvider.createCPacketCloseWindow())

            delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
        }
    }

    /**
     * Checks if the item is useful
     *
     * @param slot Slot id of the item. If the item isn't in the inventory -1
     * @return Returns true when the item is useful
     */
    fun isUseful(itemStack: IItemStack, slot: Int): Boolean {
        return try {
            val item = itemStack.item

            if (classProvider.isItemSword(item) || classProvider.isItemTool(item)) {
                val thePlayer = mc.thePlayer ?: return true

                if (slot >= 36 && findBetterItem(
                        slot - 36, thePlayer.inventory.getStackInSlot(slot - 36)
                    ) == slot - 36
                ) return true

                for (i in 0..8) {
                    if (type(i).equals("sword", true) && classProvider.isItemSword(item) || type(i).equals(
                            "pickaxe", true
                        ) && classProvider.isItemPickaxe(item) || type(i).equals(
                            "axe", true
                        ) && classProvider.isItemAxe(item)
                    ) {
                        if (findBetterItem(i, thePlayer.inventory.getStackInSlot(i)) == null) {
                            return true
                        }
                    }
                }

                val damage = (itemStack.getAttributeModifier("generic.attackDamage").firstOrNull()?.amount
                    ?: 0.0) + 1.25 * ItemUtils.getEnchantment(
                    itemStack, classProvider.getEnchantmentEnum(EnchantmentType.SHARPNESS)
                )

                items(0, 45).none { (_, stack) ->
                    stack != itemStack && stack.javaClass == itemStack.javaClass && damage < (stack.getAttributeModifier(
                        "generic.attackDamage"
                    ).firstOrNull()?.amount ?: 0.0) + 1.25 * ItemUtils.getEnchantment(
                        stack, classProvider.getEnchantmentEnum(EnchantmentType.SHARPNESS)
                    )
                }
            } else if (classProvider.isItemArmor(item) && (ignoreChainArmorValue.get() && (itemStack.unlocalizedName == "item.helmetChain" || itemStack.unlocalizedName == "item.leggingsChain")))
                false else if (classProvider.isItemBow(item)) {
                val currPower =
                    ItemUtils.getEnchantment(itemStack, classProvider.getEnchantmentEnum(EnchantmentType.POWER))

                items().none { (_, stack) ->
                    itemStack != stack && classProvider.isItemBow(stack.item) && currPower < ItemUtils.getEnchantment(
                        stack, classProvider.getEnchantmentEnum(EnchantmentType.POWER)
                    )
                }
            } else if (classProvider.isItemArmor(item)) {
                val currArmor = ArmorPiece(itemStack, slot)

                items().none { (slot, stack) ->
                    if (stack != itemStack && classProvider.isItemArmor(stack.item)) {
                        val armor = ArmorPiece(stack, slot)

                        if (armor.armorType != currArmor.armorType) false
                        else ARMOR_COMPARATOR.compare(currArmor, armor) <= 0
                    } else false
                }
            } else if (itemStack.unlocalizedName == "item.compass") {
                items(0, 45).none { (_, stack) -> itemStack != stack && stack.unlocalizedName == "item.compass" }
            } else (classProvider.isItemFood(item) && classProvider.isItemAppleGold(item)) || itemStack.unlocalizedName == "item.arrow" || itemStack.unlocalizedName == "item.slimeball" || classProvider.isItemBlock(
                item
            ) && !classProvider.isBlockBush(item?.asItemBlock()?.block) || classProvider.isItemBed(item) || classProvider.isItemPotion(
                item
            ) || classProvider.isItemEnderPearl(item) || classProvider.isItemBucket(item) || itemStack.unlocalizedName == "item.stick" || ignoreVehiclesValue.get() && (classProvider.isItemBoat(
                item
            ) || classProvider.isItemMinecart(item))
        } catch (ex: Exception) {
            ClientUtils.getLogger().error("(InvManager) Failed to check item: ${itemStack.unlocalizedName}.", ex)

            true
        }
    }

    /**
     * INVENTORY SORTER
     */

    /**
     * Sort hotbar
     */
    private fun sortHotbar() {
        for (index in 0..8) {
            val thePlayer = mc.thePlayer ?: return

            val bestItem = findBetterItem(index, thePlayer.inventory.getStackInSlot(index)) ?: continue

            if (bestItem != index) {
                val openInventory = !classProvider.isGuiInventory(mc.currentScreen) && simulateInventory.get()

                //if (openInventory) mc.netHandler.addToSendQueue(createOpenInventoryPacket())

                mc.playerController.windowClick(
                    0, if (bestItem < 9) bestItem + 36 else bestItem, index, 2, thePlayer
                )

                if (openInventory) mc.netHandler.addToSendQueue(classProvider.createCPacketCloseWindow())

                delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
                break
            }
        }
    }

    private fun findBetterItem(targetSlot: Int, slotStack: IItemStack?): Int? {
        val type = type(targetSlot)

        val thePlayer = mc.thePlayer ?: return null

        when (type.toLowerCase()) {
            "sword", "pickaxe", "axe" -> {
                val currentTypeChecker: ((IItem?) -> Boolean) = when {
                    type.equals("Sword", ignoreCase = true) -> { item: IItem? -> classProvider.isItemSword(item) }
                    type.equals("Pickaxe", ignoreCase = true) -> { obj: IItem? -> classProvider.isItemPickaxe(obj) }
                    type.equals("Axe", ignoreCase = true) -> { obj: IItem? -> classProvider.isItemAxe(obj) }
                    else -> return null
                }

                var bestWeapon = if (currentTypeChecker(slotStack?.item)) targetSlot
                else -1

                thePlayer.inventory.mainInventory.forEachIndexed { index, itemStack ->
                    if (itemStack != null && currentTypeChecker(itemStack.item) && !type(index).equals(
                            type, ignoreCase = true
                        )
                    ) {
                        if (bestWeapon == -1) {
                            bestWeapon = index
                        } else {
                            val currDamage =
                                (itemStack.getAttributeModifier("generic.attackDamage").firstOrNull()?.amount
                                    ?: 0.0) + (1.25 * ItemUtils.getEnchantment(
                                    itemStack, classProvider.getEnchantmentEnum(EnchantmentType.SHARPNESS)
                                ))

                            val bestStack = thePlayer.inventory.getStackInSlot(bestWeapon) ?: return@forEachIndexed
                            val bestDamage =
                                (bestStack.getAttributeModifier("generic.attackDamage").firstOrNull()?.amount
                                    ?: 0.0) + 1.25 * ItemUtils.getEnchantment(
                                    bestStack, classProvider.getEnchantmentEnum(EnchantmentType.SHARPNESS)
                                )

                            if (bestDamage < currDamage) bestWeapon = index
                        }
                    }
                }

                return if (bestWeapon != -1 || bestWeapon == targetSlot) bestWeapon else null
            }

            "bow" -> {
                var bestBow = if (classProvider.isItemBow(slotStack?.item)) targetSlot else -1
                var bestPower = if (bestBow != -1) ItemUtils.getEnchantment(
                    slotStack, classProvider.getEnchantmentEnum(EnchantmentType.POWER)
                )
                else 0

                thePlayer.inventory.mainInventory.forEachIndexed { index, itemStack ->
                    if (classProvider.isItemBow(itemStack?.item) && !type(index).equals(type, ignoreCase = true)) {
                        if (bestBow == -1) {
                            bestBow = index
                        } else {
                            val power = ItemUtils.getEnchantment(
                                itemStack, classProvider.getEnchantmentEnum(EnchantmentType.POWER)
                            )

                            if (ItemUtils.getEnchantment(
                                    itemStack, classProvider.getEnchantmentEnum(EnchantmentType.POWER)
                                ) > bestPower
                            ) {
                                bestBow = index
                                bestPower = power
                            }
                        }
                    }
                }

                return if (bestBow != -1) bestBow else null
            }

            "block" -> {
                thePlayer.inventory.mainInventory.forEachIndexed { index, stack ->
                    if (stack != null) {
                        val item = stack.item!!

                        if (classProvider.isItemBlock(item) && !InventoryUtils.BLOCK_BLACKLIST.contains(item.asItemBlock().block) && !type(
                                index
                            ).equals("Block", ignoreCase = true)
                        ) {
                            val replaceCurr = ItemUtils.isStackEmpty(slotStack) || !classProvider.isItemBlock(item)

                            return if (replaceCurr) index else null
                        }
                    }
                }
            }

            "water" -> {
                thePlayer.inventory.mainInventory.forEachIndexed { index, stack ->
                    if (stack != null) {
                        val item = stack.item!!

                        if (classProvider.isItemBucket(item) && item.asItemBucket().isFull == classProvider.getBlockEnum(
                                BlockType.FLOWING_WATER
                            ) && !type(index).equals("Water", ignoreCase = true)
                        ) {
                            val replaceCurr =
                                ItemUtils.isStackEmpty(slotStack) || !classProvider.isItemBucket(item) || (item.asItemBucket()).isFull != classProvider.getBlockEnum(
                                    BlockType.FLOWING_WATER
                                )

                            return if (replaceCurr) index else null
                        }
                    }
                }
            }

            "gapple" -> {
                thePlayer.inventory.mainInventory.forEachIndexed { index, stack ->
                    if (stack != null) {
                        val item = stack.item!!

                        if (classProvider.isItemAppleGold(item) && !type(index).equals("Gapple", ignoreCase = true)) {
                            val replaceCurr =
                                ItemUtils.isStackEmpty(slotStack) || !classProvider.isItemAppleGold(slotStack?.item)

                            return if (replaceCurr) index else null
                        }
                    }
                }
            }

            "pearl" -> {
                thePlayer.inventory.mainInventory.forEachIndexed { index, stack ->
                    if (stack != null) {
                        val item = stack.item

                        if (classProvider.isItemEnderPearl(item) && !type(index).equals("Pearl", ignoreCase = true)) {
                            val replaceCurr =
                                ItemUtils.isStackEmpty(slotStack) || !classProvider.isItemEnderPearl(slotStack?.item)

                            return if (replaceCurr) index else null
                        }
                    }
                }
            }
        }

        return null
    }

    /**
     * GetKey items in inventory
     */
    private fun items(start: Int = 0, end: Int = 45): Map<Int, IItemStack> {
        val items = mutableMapOf<Int, IItemStack>()

        for (i in end - 1 downTo start) {
            val itemStack = mc.thePlayer?.inventoryContainer?.getSlot(i)?.stack ?: continue

            if (ItemUtils.isStackEmpty(itemStack)) continue

            if (i in 36..44 && type(i).equals("Ignore", ignoreCase = true)) continue

            if (System.currentTimeMillis() - (itemStack).itemDelay >= itemDelayValue.get()) items[i] = itemStack
        }

        return items
    }

    /**
     * GetKey type of [targetSlot]
     */
    private fun type(targetSlot: Int) = when (targetSlot) {
        0 -> sortSlot1Value.get()
        1 -> sortSlot2Value.get()
        2 -> sortSlot3Value.get()
        3 -> sortSlot4Value.get()
        4 -> sortSlot5Value.get()
        5 -> sortSlot6Value.get()
        6 -> sortSlot7Value.get()
        7 -> sortSlot8Value.get()
        8 -> sortSlot9Value.get()
        else -> ""
    }

    /**
     * Shift+Left clicks the specified item
     *
     * @param item        Slot of the item to click
     * @param isArmorSlot
     * @return True if it is unable to move the item
     */
    private fun move(item: Int, isArmorSlot: Boolean): Boolean {
        if (!isArmorSlot && item < 9 && !classProvider.isGuiInventory(mc.currentScreen)) {
            mc.netHandler.addToSendQueue(classProvider.createCPacketHeldItemChange(item))
            mc.netHandler.addToSendQueue(createUseItemPacket(WEnumHand.MAIN_HAND))
            mc.netHandler.addToSendQueue(
                classProvider.createCPacketHeldItemChange(
                    mc.thePlayer!!.inventory.currentItem
                )
            )
            delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
            return true
        } else if (!(noMoveValue.get() && MovementUtils.isMoving) && (!invOpenValue.get() || classProvider.isGuiInventory(
                mc.currentScreen
            )) && item != -1
        ) {
            val openInventory = simulateInventory.get() && !classProvider.isGuiInventory(mc.currentScreen)
            //if (openInventory) mc.netHandler.addToSendQueue(createOpenInventoryPacket())
            var full = isArmorSlot
            if (full) {
                for (iItemStack in mc.thePlayer!!.inventory.mainInventory) {
                    if (ItemUtils.isStackEmpty(iItemStack)) {
                        full = false
                        break
                    }
                }
            }
            if (full) {
                mc.playerController.windowClick(
                    mc.thePlayer!!.inventoryContainer.windowId,
                    item,
                    1,
                    4,
                    mc.thePlayer!!
                )
            } else {
                when (swapArmorModeValue.get().toLowerCase()) {
                    "windowclick" -> mc.playerController.windowClick(
                        mc.thePlayer!!.inventoryContainer.windowId,
                        if (isArmorSlot) item else if (item < 9) item + 36 else item, 0, 1,
                        mc.thePlayer!!
                    )

                    "packet" -> {
                        mc2.connection?.sendPacket(
                            CPacketClickWindow(
                                mc.thePlayer!!.inventoryContainer.windowId,
                                if (isArmorSlot) item else if (item < 9) item + 36 else item,
                                0,
                                ClickType.QUICK_MOVE,
                                mc.thePlayer!!.inventoryContainer.getSlot(if (isArmorSlot) item else if (item < 9) item + 36 else item).stack!!.unwrap(),
                                1.toShort()
                            )
                        )
                        mc.thePlayer!!.unwrap().inventoryContainer.slotClick(
                            (if (isArmorSlot) item else if (item < 9) item + 36 else item),
                            0,
                            ClickType.QUICK_MOVE,
                            mc.thePlayer!!.unwrap()
                        )
                        mc2.connection!!.sendPacket(
                            CPacketConfirmTransaction(
                                mc.thePlayer!!.inventoryContainer.windowId,
                                1.toShort(),
                                true
                            )
                        )
                    }
                }
                delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
                if (openInventory) mc.netHandler.addToSendQueue(classProvider.createCPacketCloseWindow())
                return true
            }
        }
        return false

    }

    companion object {
        val ARMOR_COMPARATOR = ArmorComparator()
    }
}