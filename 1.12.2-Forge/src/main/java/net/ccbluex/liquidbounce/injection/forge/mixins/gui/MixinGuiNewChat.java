package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.Terra;
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.EaseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(GuiNewChat.class)
public abstract class MixinGuiNewChat {

    @Shadow
    @Final
    private Minecraft mc;
    @Shadow
    @Final
    private List<ChatLine> drawnChatLines;
    @Shadow
    private int scrollPos;
    @Shadow
    private boolean isScrolled;

    @Shadow
    public abstract int getLineCount();

    @Shadow
    public abstract boolean getChatOpen();

    @Shadow
    public abstract float getChatScale();

    @Shadow
    public abstract int getChatWidth();

    @Inject(method = "drawChat", at = @At("HEAD"), cancellable = true)
    private void drawChat(int p_drawChat_1_, final CallbackInfo callbackInfo) {
        final HUD hud = (HUD) Terra.moduleManager.getModule(HUD.class);

        if (hud.getState() && hud.getFontChatValue().get()) {
            callbackInfo.cancel();
            if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
                int lvt_2_1_ = this.getLineCount();
                boolean lvt_3_1_ = false;
                int lvt_4_1_ = 0;
                int lvt_5_1_ = this.drawnChatLines.size();
                float lvt_6_1_ = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;
                if (lvt_5_1_ > 0) {
                    if (this.getChatOpen()) {
                        lvt_3_1_ = true;
                    }

                    float lvt_7_1_ = this.getChatScale();
                    int lvt_8_1_ = MathHelper.ceil((float) this.getChatWidth() / lvt_7_1_);
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(2.0F, 20.0F, 0.0F);
                    GlStateManager.scale(lvt_7_1_, lvt_7_1_, 1.0F);

                    int lvt_9_1_;
                    int lvt_11_1_;
                    int lvt_14_1_;
                    for (lvt_9_1_ = 0; lvt_9_1_ + this.scrollPos < this.drawnChatLines.size() && lvt_9_1_ < lvt_2_1_; ++lvt_9_1_) {
                        ChatLine lvt_10_1_ = this.drawnChatLines.get(lvt_9_1_ + this.scrollPos);
                        if (lvt_10_1_ != null) {
                            lvt_11_1_ = p_drawChat_1_ - lvt_10_1_.getUpdatedCounter();
                            if (lvt_11_1_ < 200 || lvt_3_1_) {
                                double lvt_12_1_ = (double) lvt_11_1_ / 200.0D;
                                lvt_12_1_ = 1.0D - lvt_12_1_;
                                lvt_12_1_ *= 10.0D;
                                lvt_12_1_ = MathHelper.clamp(lvt_12_1_, 0.0D, 1.0D);
                                lvt_12_1_ *= lvt_12_1_;
                                lvt_14_1_ = (int) (255.0D * lvt_12_1_);
                                if (lvt_3_1_) {
                                    lvt_14_1_ = 255;
                                }

                                lvt_14_1_ = (int) ((float) lvt_14_1_ * lvt_6_1_);
                                ++lvt_4_1_;
                                if (lvt_14_1_ > 3) {
                                    int lvt_15_1_ = 0;
                                    int lvt_16_1_ = -lvt_9_1_ * 9;
                                    if (hud.getChatAnimValue().get() && !lvt_3_1_) {
                                        if (lvt_11_1_ <= 20) {
                                            GL11.glTranslatef((float) (-(lvt_8_1_ + 4) * EaseUtils.easeInQuart(1 - ((lvt_11_1_ + mc.timer.renderPartialTicks) / 20.0))), 0F, 0F);
                                        }
                                        if (lvt_11_1_ >= 180) {
                                            GL11.glTranslatef((float) (-(lvt_8_1_ + 4) * EaseUtils.easeInQuart(((lvt_11_1_ + mc.timer.renderPartialTicks) - 180) / 20.0)), 0F, 0F);
                                        }
                                    }
                                    if (hud.getChatRect().get()) {
                                        Gui.drawRect(lvt_15_1_, lvt_16_1_ - 15, lvt_15_1_ + lvt_8_1_ + 4, lvt_16_1_ - 6, lvt_14_1_ / 2 << 24);
                                    }

                                    GlStateManager.enableBlend();
                                    GlStateManager.enableAlpha();

                                    String lvt_17_1_ = lvt_10_1_.getChatComponent().getFormattedText();
                                    Fonts.font45.drawStringWithShadow(lvt_17_1_, lvt_15_1_ + 2, (lvt_16_1_ - 14), 16777215 + (lvt_14_1_ << 24));
                                    GL11.glColor4f(1, 1, 1, 1);
                                    GlStateManager.resetColor();
                                }
                            }
                        }
                    }

                    if (lvt_3_1_) {
                        lvt_9_1_ = Fonts.font45.getFontHeight();
                        GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                        int lvt_10_2_ = lvt_5_1_ * lvt_9_1_ + lvt_5_1_;
                        lvt_11_1_ = lvt_4_1_ * lvt_9_1_ + lvt_4_1_;
                        int lvt_12_2_ = this.scrollPos * lvt_11_1_ / lvt_5_1_;
                        int lvt_13_1_ = lvt_11_1_ * lvt_11_1_ / lvt_10_2_;
                        if (lvt_10_2_ != lvt_11_1_) {
                            lvt_14_1_ = lvt_12_2_ > 0 ? 170 : 96;
                            int lvt_15_2_ = this.isScrolled ? 13382451 : 3355562;
                            Gui.drawRect(0, -lvt_12_2_, 2, -lvt_12_2_ - lvt_13_1_, lvt_15_2_ + (lvt_14_1_ << 24));
                            Gui.drawRect(2, -lvt_12_2_, 1, -lvt_12_2_ - lvt_13_1_, 13421772 + (lvt_14_1_ << 24));
                        }
                    }

                    GlStateManager.popMatrix();
                }
            }
        }
    }

    @Inject(method = "getChatComponent", at = @At("HEAD"), cancellable = true)
    private void getChatComponent(int p_getChatComponent_1_, int p_getChatComponent_2_, final CallbackInfoReturnable<ITextComponent> callbackInfo) {
        final HUD hud = (HUD) Terra.moduleManager.getModule(HUD.class);

        if (hud.getState() && hud.getFontChatValue().get()) {
            if (this.getChatOpen()) {
                ScaledResolution lvt_3_1_ = new ScaledResolution(this.mc);
                int lvt_4_1_ = lvt_3_1_.getScaleFactor();
                float lvt_5_1_ = this.getChatScale();
                int lvt_6_1_ = p_getChatComponent_1_ / lvt_4_1_ - 3;
                int lvt_7_1_ = p_getChatComponent_2_ / lvt_4_1_ - 27;
                lvt_6_1_ = MathHelper.floor((float) lvt_6_1_ / lvt_5_1_);
                lvt_7_1_ = MathHelper.floor((float) lvt_7_1_ / lvt_5_1_);
                if (lvt_6_1_ >= 0 && lvt_7_1_ >= 0) {
                    int lvt_8_1_ = Math.min(this.getLineCount(), this.drawnChatLines.size());
                    if (lvt_6_1_ <= MathHelper.floor((float) this.getChatWidth() / this.getChatScale()) && lvt_7_1_ < Fonts.font45.getFontHeight() * lvt_8_1_ + lvt_8_1_) {
                        int lvt_9_1_ = lvt_7_1_ / Fonts.font45.getFontHeight() + this.scrollPos;
                        if (lvt_9_1_ >= 0 && lvt_9_1_ < this.drawnChatLines.size()) {
                            ChatLine lvt_10_1_ = this.drawnChatLines.get(lvt_9_1_);
                            int lvt_11_1_ = 0;

                            for (ITextComponent lvt_13_1_ : lvt_10_1_.getChatComponent()) {
                                if (lvt_13_1_ instanceof TextComponentString) {
                                    lvt_11_1_ += Fonts.font45.getStringWidth(GuiUtilRenderComponents.removeTextColorsIfConfigured(((TextComponentString) lvt_13_1_).getText(), false));
                                    if (lvt_11_1_ > lvt_6_1_) {
                                        callbackInfo.setReturnValue(lvt_13_1_);
                                        return;
                                    }
                                }
                            }
                        }

                    }
                }

            }

            callbackInfo.setReturnValue(null);
        }
    }
}
