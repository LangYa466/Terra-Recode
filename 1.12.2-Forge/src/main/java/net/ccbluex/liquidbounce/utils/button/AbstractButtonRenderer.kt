package net.ccbluex.liquidbounce.utils.button

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton

abstract class AbstractButtonRenderer(protected val button: GuiButton) {
    abstract fun render(mouseX: Int, mouseY: Int, mc: Minecraft)
}