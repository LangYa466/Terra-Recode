package net.ccbluex.liquidbounce.features.module.modules.render

import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import java.awt.Color
import kotlin.math.roundToInt

@ModuleInfo(name = "HealthHud", description = "OMG.", category = ModuleCategory.RENDER)
class HealthHud : Module() {
    private val cColorValue = BoolValue("CustomColor", false)
    private val colorRedValue = IntegerValue("R", 255, 0, 255).displayable { cColorValue.get() }
    private val colorGreenValue = IntegerValue("G", 255, 0, 255).displayable { cColorValue.get() }
    private val colorBlueValue = IntegerValue("B", 255, 0, 255).displayable { cColorValue.get() }
    private val renderHeartValue = BoolValue("RenderHeart", false)
    private val customXValue = FloatValue("CustomX", 0F, -20F, 20F)
    private val customYValue = FloatValue("CustomY", 0F, -20F, 20F)
    private val xBoostValue = FloatValue("XBoost", 0F, -10F, 10F)
    private val yBoostValue = FloatValue("YBoost", 0F, -10F, 10F)


    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val sr = classProvider.createScaledResolution(mc)
        val healthNum = ((mc2.player.health * 10.0f).roundToInt() / 20.0f * 10F).roundToInt() / 10f
        val abNum = ((mc2.player.absorptionAmount * 10.0f).roundToInt() / 20.0f * 10F).roundToInt() / 10f

        //自定义字体时
        val abString = if (mc2.player.absorptionAmount <= 0.0f) "" else "§e$abNum§6❤"

        //自定义字体
        val text = "$healthNum§c❤ $abString"
        val c = if (cColorValue.get()) Color(
            colorRedValue.get(),
            colorGreenValue.get(),
            colorBlueValue.get()
        ).rgb else getHealthColor(mc.thePlayer!!.health, mc.thePlayer!!.maxHealth)

        mc.fontRendererObj.drawCenteredString(
            text,
            (sr.scaledWidth / 2f + customXValue.get() * xBoostValue.get()),
            (sr.scaledHeight / 2 - 30f + customYValue.get() * yBoostValue.get()),
            c,
            true
        )


        if (!renderHeartValue.get()) return


        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        mc2.textureManager.bindTexture(ResourceLocation("textures/gui/icons.png"))
        var i2 = 0
        while (i2 < mc2.player.maxHealth / 2) {
            mc2.ingameGUI.drawTexturedModalRect(
                ((sr.scaledWidth / 2) - mc2.player.maxHealth / 2.0f * 10.0f / 2.0f + 9 + (i2 * 8) + customXValue.get() * xBoostValue.get()),
                (sr.scaledHeight / 2 - 40F + customYValue.get() * yBoostValue.get()),
                16,
                0,
                9,
                9
            )
            ++i2
        }
        i2 = 0
        while (i2 < mc2.player.health / 2.0) {
            mc2.ingameGUI.drawTexturedModalRect(
                ((sr.scaledWidth / 2) - mc2.player.maxHealth / 2.0f * 10.0f / 2.0f + 9 + (i2 * 8) + customXValue.get() * xBoostValue.get()),
                (sr.scaledHeight / 2 - 40f + customYValue.get() * yBoostValue.get()),
                52,
                0,
                9,
                9
            )
            ++i2
        }
    }

    private fun getHealthColor(health: Float, maxHealth: Float): Int {
        var displayhealth = health
        if (maxHealth > 20) {
            displayhealth = 20f
        }
        val fractions = floatArrayOf(0f, 0.5f, 1f)
        val colors = arrayOf(Color.RED, Color.YELLOW, Color.GREEN)
        val progress = displayhealth * 5 * 0.01f
        val customColor = BlendUtils.blendColors(fractions, colors, progress)!!.brighter()
        return customColor.rgb
    }
}
