package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import org.lwjgl.input.Keyboard

@ModuleInfo(name = "HudDesigner", description = "Open the Hud editor.", category = ModuleCategory.CLIENT, canEnable = false, keyBind = Keyboard.KEY_RCONTROL)
class HudDesigner : Module() {
    override fun onEnable() {
        mc.displayGuiScreen(classProvider.wrapGuiScreen(GuiHudDesigner()))
    }
}