/*
 * Terra Hacked Client
 */

package net.ccbluex.liquidbounce.api.minecraft.client.gui

interface IGuiSlot : IGui {
    val width: Int
    val slotHeight: Int

    fun scrollBy(value: Int)
    fun registerScrollButtons(down: Int, up: Int)
    fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float)
    fun elementClicked(index: Int, doubleClick: Boolean, var3: Int, var4: Int)
    fun handleMouseInput()
    fun setListWidth(width: Int)
    fun setEnableScissor(flag: Boolean)
}