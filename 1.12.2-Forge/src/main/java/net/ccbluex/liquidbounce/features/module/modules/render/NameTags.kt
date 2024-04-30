package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntityLivingBase
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.injection.backend.Backend
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.GlowUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.potion.Potion
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.roundToInt

@ModuleInfo("NameTags","Changes the scale of the nametags so you can always read them.",ModuleCategory.RENDER)
class NameTags : Module() {
    private val healthValue = BoolValue("Health", false)
    private val distanceValue = BoolValue("Distance", false)
    private val clearNamesValue = BoolValue("ClearNames", false)
    private val armorValue = BoolValue("Armor", true)
    private val potionValue = BoolValue("Potions", true)
    private val localValue = BoolValue("LocalPlayer", true)
    private val nfpValue = BoolValue("NoFirstPerson", true).displayable { localValue.get() }
    private val backgroundAlphaValue = IntegerValue("Background-Alpha", 80, 0, 255)
    private val scaleValue = FloatValue("Scale", 1F, 0.1F, 4F)

    private val inventoryBackground = ResourceLocation("textures/gui/container/inventory.png")

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
        GL11.glPushMatrix()

        // Disable lightning and depth test
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glDisable(GL11.GL_DEPTH_TEST)

        GL11.glEnable(GL11.GL_LINE_SMOOTH)

        // Enable blend
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        for (entity in mc.theWorld!!.loadedEntityList) {
            if (!EntityUtils.isSelected(entity, false) && (!localValue.get() || entity != mc.thePlayer || (nfpValue.get() && mc2.gameSettings.thirdPersonView == 0)))
                continue

            renderNameTag(entity.asEntityLivingBase(),
                if (clearNamesValue.get())
                    ColorUtils.stripColor(entity.displayName?.unformattedText) ?: continue
                else
                    (entity.displayName ?: continue).unformattedText
            )
        }

        GL11.glPopMatrix()
        GL11.glPopAttrib()

        // Reset color
        GL11.glColor4f(1F, 1F, 1F, 1F)
    }

    private fun renderNameTag(entity: IEntityLivingBase, tag: String) {
        val thePlayer = mc.thePlayer ?: return

        // Modify tag
        val bot = AntiBot.isBot(entity)
        val nameColor = if (bot) "§3" else if (entity.invisible) "§6" else if (entity.sneaking) "§4" else "§7"
        val distanceText = if (distanceValue.get()) "§7${thePlayer.getDistanceToEntity(entity).roundToInt()}m " else ""
        val healthText = if (healthValue.get()) "§7§c " + entity.health.toInt() + " HP" else ""

        val text = "$nameColor$distanceText$tag$healthText"

        // Push
        GL11.glPushMatrix()

        // Translate to player position
        val timer = mc.timer
        val renderManager = mc.renderManager


        GL11.glTranslated( // Translate to player position with render pos and interpolate it
            entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks - renderManager.renderPosX,
            entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks - renderManager.renderPosY + entity.eyeHeight.toDouble() + 0.55,
            entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks - renderManager.renderPosZ
        )

        GL11.glRotatef(-mc.renderManager.playerViewY, 0F, 1F, 0F)
        GL11.glRotatef(mc.renderManager.playerViewX, 1F, 0F, 0F)


        // Scale
        var distance = thePlayer.getDistanceToEntity(entity) * 0.25f

        if (distance < 1F)
            distance = 1F

        val scale = distance / 100f * scaleValue.get()

        GL11.glScalef(-scale, -scale, scale)

        AWTFontRenderer.assumeNonVolatile = true

        // Draw NameTag
        val width = Fonts.font40.getStringWidth(text)
        val width2 = Fonts.font40.getStringWidth(text) / 2
        val height1 = Fonts.font40.fontHeight + 2F
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)

        RenderUtils.drawGradientRound(-width2.toFloat(), -20f, width.toFloat(), height1, 5F,Color(0, 0, 0, backgroundAlphaValue.get()),Color(0, 0, 0, backgroundAlphaValue.get()),Color(0, 0, 0, backgroundAlphaValue.get()),Color(0, 0, 0, backgroundAlphaValue.get()))

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        Fonts.font35.drawString(text, (-width/2).toFloat() + 10f, -18f, Color.WHITE.rgb, false)
        AWTFontRenderer.assumeNonVolatile = false

        //Potion
        var foundPotion = false
        if (potionValue.get() && classProvider.isEntityPlayer(entity)) {
            val potions = entity.activePotionEffects.map { Potion.getPotionById(it.potionID) }.filter { it!!.hasStatusIcon() }
            if (potions.isNotEmpty()) {
                foundPotion = true

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)
                GlStateManager.disableLighting()
                GlStateManager.enableTexture2D()

                val minX = (potions.size * -20) / 2

                var index = 0

                GL11.glPushMatrix()
                GlStateManager.enableRescaleNormal()
                for (potion in potions) {
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)
                    mc2.textureManager.bindTexture(inventoryBackground)
                    val i1 = potion!!.statusIconIndex
                    RenderUtils.drawTexturedModalRect(
                        minX + index * 20,
                        -42,
                        0 + i1 % 8 * 18,
                        198 + i1 / 8 * 18,
                        18,
                        18,
                        0F
                    )
                    index++
                }
                GlStateManager.disableRescaleNormal()
                GL11.glPopMatrix()

                GlStateManager.enableAlpha()
                GlStateManager.disableBlend()
                GlStateManager.enableTexture2D()
            }
        }

        //Armor
        if (armorValue.get() && classProvider.isEntityPlayer(entity)) {
            mc.renderItem.zLevel = -147F

            val indices: IntArray = if (Backend.MINECRAFT_VERSION_MINOR == 8) (0..4).toList().toIntArray() else intArrayOf(0, 1, 2, 3, 5, 4)

            for (index in indices) {
                val equipmentInSlot = entity.getEquipmentInSlot(index) ?: continue

                mc.renderItem.renderItemAndEffectIntoGUI(equipmentInSlot, -50 + index * 20, if (potionValue.get() && foundPotion) -62 else -42)
            }

            GlStateManager.enableAlpha()
            GlStateManager.disableBlend()
            GlStateManager.enableTexture2D()
        }

        // Pop
        GL11.glPopMatrix()
    }
}