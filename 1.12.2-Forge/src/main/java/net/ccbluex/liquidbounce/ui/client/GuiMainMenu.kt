package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.Terra.CLIENT_VERSION
import net.ccbluex.liquidbounce.api.minecraft.client.gui.IGuiButton
import net.ccbluex.liquidbounce.api.util.WrappedGuiScreen
import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import java.awt.Color


class GuiMainMenu : WrappedGuiScreen() {
    val hud = Terra.moduleManager[HUD::class.java]
    private var currentX = 0f
    private var currentY = 0f

    private var photoIndex = 1
    private var timeHelper = MSTimer()

    override fun initGui() {
        val defaultHeight = representedScreen.height / 4.5 + 18

        representedScreen.buttonList.add(classProvider.createGuiButton(1, representedScreen.width - 120,
                (defaultHeight + 24).toInt(), 100, 20, "Single Game"))
        representedScreen.buttonList.add(classProvider.createGuiButton(2, representedScreen.width - 120,
                (defaultHeight + 48).toInt(), 100, 20, "Multi Game"))
        representedScreen.buttonList.add(classProvider.createGuiButton(102, representedScreen.width - 120,
                (defaultHeight + 72).toInt(), 100, 20, "Background"))
        representedScreen.buttonList.add(classProvider.createGuiButton(100, representedScreen.width - 120,
                (defaultHeight + 96).toInt(), 100, 20, "AltManager"))
        representedScreen.buttonList.add(classProvider.createGuiButton(0, representedScreen.width - 120,
                (defaultHeight + 120).toInt(), 100, 20, "Options"))
        representedScreen.buttonList.add(classProvider.createGuiButton(4, representedScreen.width - 120,
                (defaultHeight + 144).toInt(), 100, 20, "Exit"))

    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val height = representedScreen.height
        val h = representedScreen.height
        val w = representedScreen.width
        val res = ScaledResolution(mc2)
        val xDiff: Float = ((mouseX - h / 2).toFloat() - this.currentX) / res.scaleFactor.toFloat()
        val yDiff: Float = ((mouseY - w / 2).toFloat() - this.currentY) / res.scaleFactor.toFloat()
        this.currentX += xDiff * 0.3f
        this.currentY += yDiff * 0.3f


        GlStateManager.translate(this.currentX / 30.0f, this.currentY / 15.0f, 0.0f)
        RenderUtils.drawImage4(drawBackGround(), -30, -30, res.scaledWidth + 60, res.scaledHeight + 60)
        GlStateManager.translate(-this.currentX / 30.0f, -this.currentY / 15.0f, 0.0f)

        RenderUtils.drawRect(representedScreen.width - 142, 0, representedScreen.width, representedScreen.height, Color(0, 0, 0, 80).rgb)
        Fonts.font95.drawCenteredString("Terra   ", ((95 / 2) - (Fonts.font95.getStringWidth("Terra   ") / 2) + representedScreen.width - 70.5).toFloat(), representedScreen.height / 6F, Color(hud.getThemeColorR(),hud.getThemeColorG(),hud.getThemeColorB()).rgb, true)



        //Update log
        var length = 2
        val list = Terra.UPDATE_LIST

        for ((i, _: String) in list.withIndex()) {

            length += if (i <= 0) {
                Fonts.font35.drawString(list[i], 2F, length.toFloat(), Color(255, 255, 255, 255).rgb, true)
                Fonts.font35.fontHeight + 2
            } else {
                Fonts.font35.drawString(list[i], 2F, length.toFloat(), Color(255, 255, 255, 255).rgb, true)
                Fonts.font35.fontHeight + 2
            }
        }

        Fonts.font40.drawString("Forge Terra b$CLIENT_VERSION", 4f, height - 12f, Color(255, 255, 255, 200).rgb, true)

        representedScreen.superDrawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: IGuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(classProvider.createGuiOptions(this.representedScreen, mc.gameSettings))
            1 -> mc.displayGuiScreen(classProvider.createGuiSelectWorld(this.representedScreen))
            2 -> mc.displayGuiScreen(classProvider.createGuiMultiplayer(this.representedScreen))
            4 -> mc.shutdown()
            100 -> mc.displayGuiScreen(classProvider.wrapGuiScreen(GuiAltManager(this.representedScreen)))
            101 -> mc.displayGuiScreen(classProvider.wrapGuiScreen(GuiServerStatus(this.representedScreen)))
            102 -> mc.displayGuiScreen(classProvider.wrapGuiScreen(GuiBackground(this.representedScreen)))
            103 -> mc.displayGuiScreen(classProvider.wrapGuiScreen(GuiModsMenu(this.representedScreen)))
            108 -> mc.displayGuiScreen(classProvider.wrapGuiScreen(GuiContributors(this.representedScreen)))
        }
    }

    override fun updateScreen() {
        if (timeHelper.hasTimePassed(42)) {
            if (photoIndex + 1 > 80) {
                photoIndex = 1
            } else {
                photoIndex += 1
            }
            timeHelper.reset()
        }
    }

    private fun drawBackGround(): ResourceLocation {
        val photoIndexString = photoIndex.toString()
        return ResourceLocation("terra/newbg/$photoIndexString.jpg")
    }


}