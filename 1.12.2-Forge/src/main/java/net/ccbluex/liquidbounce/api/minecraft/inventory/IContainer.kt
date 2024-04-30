/*
 * Terra Hacked Client
 */

package net.ccbluex.liquidbounce.api.minecraft.inventory

import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack

interface IContainer {
    val windowId: Int

    fun getSlot(id: Int): ISlot

}
