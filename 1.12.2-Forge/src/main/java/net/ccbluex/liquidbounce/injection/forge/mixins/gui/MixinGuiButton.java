/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.Terra;
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.injection.backend.FontRendererImpl;
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.button.AbstractButtonRenderer;
import net.ccbluex.liquidbounce.utils.render.AnimationUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.*;

import java.awt.*;

import static net.ccbluex.liquidbounce.utils.MinecraftInstance.mc2;

@Mixin(GuiButton.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiButton extends Gui {

    @Unique
    protected final AbstractButtonRenderer terra$buttonRenderer = Terra.moduleManager.getModule(HUD.class).getButtonRenderer((GuiButton) (Object) this);
    @Unique
    private float terra$cut;
    @Unique
    private float terra$alpha;
    @Unique
    private float terra$moveX = 0F;
    @Shadow
    protected abstract int getHoverState(boolean p_getHoverState_1_);

    @Shadow
    @Final
    protected static ResourceLocation BUTTON_TEXTURES;
    @Shadow
    public boolean visible;
    @Shadow
    public int x;
    @Shadow
    public int y;
    @Shadow
    public int width;
    @Shadow
    public int height;
    @Shadow
    public boolean enabled;
    @Shadow
    public String displayString;
    @Shadow
    protected boolean hovered;

    @Shadow
    protected abstract void mouseDragged(Minecraft mc, int mouseX, int mouseY);

    /**
     * @author CCBlueX
     * @reason Terra
     */
    @Overwrite
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            final FontRenderer fontRenderer =
                    mc.getLanguageManager().isCurrentLocaleUnicode() ? mc.fontRenderer : ((FontRendererImpl) Fonts.font40).getWrapped();
            hovered = (mouseX >= this.x && mouseY >= this.y &&
                    mouseX < this.x + this.width && mouseY < this.y + this.height);

            final int delta = RenderUtils.deltaTime;
            final float speedDelta = 0.01F * delta;

            final HUD hud = Terra.moduleManager.getModule(HUD.class);
            //Theme Color
            int themeColorR = hud.getThemeColorR();
            int themeColorG = hud.getThemeColorG();
            int themeColorB = hud.getThemeColorB();
            Color buttonColor = this.enabled ? new Color(themeColorR, themeColorG, themeColorB) : new Color(71, 71, 71);

            if (enabled && hovered) {
                // LiquidBounce
                terra$cut += 0.05F * delta;
                if (terra$cut >= 4) terra$cut = 4;
                terra$alpha += 0.3F * delta;
                if (terra$alpha >= 210) terra$alpha = 210;

                // LiquidBounce+
                terra$moveX = AnimationUtils.animate(this.width - 2F, terra$moveX, speedDelta);
            } else {
                // LiquidBounce
                terra$cut -= 0.05F * delta;
                if (terra$cut <= 0) terra$cut = 0;
                terra$alpha -= 0.3F * delta;
                if (terra$alpha <= 120) terra$alpha = 120;

                // LiquidBounce+
                terra$moveX = AnimationUtils.animate(0F, terra$moveX, speedDelta);
            }

            float roundCorner = Math.max(0F, 2.4F + terra$moveX - (this.width - 2.4F));

            switch (hud.getGuiButtonStyle().get().toLowerCase()) {
                case "minecraft":
                    mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
                    int i = this.getHoverState(this.hovered);
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    GlStateManager.blendFunc(770, 771);
                    this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
                    this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
                    this.mouseDragged(mc, mouseX, mouseY);
                    int j = 14737632;

                    if (!this.enabled) {
                        j = 10526880;
                    } else if (this.hovered) {
                        j = 16777120;
                    }

                    this.drawCenteredString(mc2.fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
                    break;
                case "liquidbounce":
                    Gui.drawRect(this.x + (int) this.terra$cut, this.y,
                            this.x + this.width - (int) this.terra$cut, this.y + this.height,
                            this.enabled ? new Color(0F, 0F, 0F, this.terra$alpha / 255F).getRGB() :
                                    new Color(0.5F, 0.5F, 0.5F, 0.5F).getRGB());
                    break;
                case "terra":
                    RenderUtils.drawRoundedRect(this.x, this.y, this.x + this.width, this.y + this.height, 2.4F, new Color(0, 0, 0, 150).getRGB());
                    RenderUtils.customRounded(this.x, this.y, this.x + 2.4F + terra$moveX, this.y + this.height, 2.4F, roundCorner, roundCorner, 2.4F, buttonColor.getRGB());
                    break;
                case "rise":
                    float startX = x;
                    float endX = x + width;
                    float endY = y + height;
                    Color idk;
                    if (hovered) {
                        idk = new Color(60, 60, 60, 150);
                    } else {
                        idk = new Color(31, 31, 31, 150);
                    }
                    RenderUtils.drawRect(
                            startX, y, endX, endY, idk);
                    assert terra$buttonRenderer != null;
                    terra$buttonRenderer.render(mouseX, mouseY, mc);
            }

            if (hud.getGuiButtonStyle().get().equalsIgnoreCase("minecraft")) return;

            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            mouseDragged(mc, mouseX, mouseY);

            AWTFontRenderer.Companion.setAssumeNonVolatile(true);

            fontRenderer.drawStringWithShadow(displayString,
                    (float) ((this.x + this.width / 2) -
                            fontRenderer.getStringWidth(displayString) / 2),
                    this.y + (this.height - 5) / 2F - 2, 14737632);

            AWTFontRenderer.Companion.setAssumeNonVolatile(false);

            GlStateManager.resetColor();
        }
    }
}