/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.button.AbstractButtonRenderer
import net.ccbluex.liquidbounce.utils.button.RiseButtonRenderer
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.GuiButton
import net.minecraft.util.ResourceLocation

@ModuleInfo(name = "HUD", description = "Toggles visibility of the HUD.", category = ModuleCategory.CLIENT, array = false)
class HUD : Module() {
    private val toggleSoundValue = ListValue("ToggleSound", arrayOf("None", "Terra", "Rise"), "Rise")

    val blackHotbarValue = BoolValue("BlackHotbar", true)
    val inventoryParticle = BoolValue("InventoryParticle", false)
    private val blurValue = BoolValue("Blur", true)
    val fontChatValue = BoolValue("FontChat", false)
    val chatAnimValue = BoolValue("ChatAnimation", false).displayable { fontChatValue.get() }
    val chatRect = BoolValue("ChatRect", true).displayable { fontChatValue.get() }

    private val logoValue = BoolValue("Logo",true)
    val waterMarkValue = TextValue("Watermark","---QuickMacro---")

    companion object {
        val customClientName = TextValue("CustomClientName","Terra")
        val shadowValue = ListValue("ShadowMode", arrayOf("LiquidBounce", "Default", "Outline"), "LiquidBounce")
    }

    val guiButtonStyle = ListValue("ButtonStyle", arrayOf("Minecraft", "LiquidBounce", "Terra", "Rise"), "Terra")

    private val themeR = IntegerValue("Theme-R",34,0,255)
    private val themeG = IntegerValue("Theme-G",139,0,255)
    private val themeB = IntegerValue("Theme-B",34,0,255)

    fun getButtonRenderer(button: GuiButton): AbstractButtonRenderer? {
        return when (guiButtonStyle.get().toLowerCase()) {
            "rise" -> RiseButtonRenderer(button)
            else -> null
        }
    }

    fun getThemeColorR(): Int {
        return themeR.get()
    }

    fun getThemeColorG(): Int {
        return themeG.get()
    }

    fun getThemeColorB(): Int {
        return themeB.get()
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent?) {
        if (logoValue.get()) {
            RenderUtils.drawCFImg(ResourceLocation("terra/logo.png"),5F,5F,162,47)
        }

        Terra.hud.render(false)
        if (classProvider.isGuiHudDesigner(mc.currentScreen))
            return
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        Terra.hud.update()
    }

    @EventTarget
    fun onKey(event: KeyEvent) {
        Terra.hud.handleKey('a', event.key)
    }

    @EventTarget(ignoreCondition = true)
    fun onScreen(event: ScreenEvent) {
        if (mc.theWorld == null || mc.thePlayer == null) return
        if (state && blurValue.get() && !mc.entityRenderer.isShaderActive() && event.guiScreen != null &&
                !(classProvider.isGuiChat(event.guiScreen) || classProvider.isGuiHudDesigner(event.guiScreen))) mc.entityRenderer.loadShader(classProvider.createResourceLocation("terra" + "/blur.json")) else if (mc.entityRenderer.shaderGroup != null &&
                mc.entityRenderer.shaderGroup!!.shaderGroupName.contains("terra/blur.json")) mc.entityRenderer.stopUseShader()
    }

    @EventTarget(ignoreCondition = true)
    fun onTick(event: TickEvent) {
        Terra.moduleManager.toggleSoundMode = toggleSoundValue.values.indexOf(toggleSoundValue.get())
    }

    init {
        state = true
    }
}