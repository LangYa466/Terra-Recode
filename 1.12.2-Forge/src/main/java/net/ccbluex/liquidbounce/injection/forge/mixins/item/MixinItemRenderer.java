/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.item;

import net.ccbluex.liquidbounce.Terra;
import net.ccbluex.liquidbounce.features.module.modules.client.Animations;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.render.AntiBlind;
import net.ccbluex.liquidbounce.utils.timer.MSTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
@SideOnly(Side.CLIENT)
public abstract class MixinItemRenderer {

    float delay = 0.0F;
    MSTimer rotateTimer = new MSTimer();

    @Shadow
    @Final
    private Minecraft mc;
    @Shadow
    private ItemStack itemStackOffHand;

    @Shadow
    private ItemStack itemStackMainHand;

    @Shadow
    protected abstract void renderMapFirstPerson(float p_187463_1_, float p_187463_2_, float p_187463_3_);

    @Shadow
    protected abstract void transformFirstPerson(EnumHandSide hand, float swingProgress);


    @Shadow
    protected abstract void transformEatFirstPerson(float p_187454_1_, EnumHandSide hand, ItemStack stack);

    @Shadow
    protected abstract void renderArmFirstPerson(float p_187456_1_, float p_187456_2_, EnumHandSide p_187456_3_);

    @Shadow
    protected abstract void renderMapFirstPersonSide(float p_187465_1_, EnumHandSide hand, float p_187465_3_, ItemStack stack);

    @Shadow
    protected abstract void transformSideFirstPerson(EnumHandSide hand, float p_187459_2_);

    @Shadow
    public abstract void renderItemSide(EntityLivingBase entitylivingbaseIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform, boolean leftHanded);

    private static void transformSideFirstPersonBlock(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(side * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        GlStateManager.translate(side * -0.1414214, 0.08, 0.1414214);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
        GlStateManager.rotate((float) (f * -20.0F), 0, 1, 0);
        GlStateManager.rotate((float) (f1 * -20.0F), 0, 0, 1);
        GlStateManager.rotate((float) (f1 * -80.0F), 1, 0, 0);
        GlStateManager.scale(Animations.Scale.get(), Animations.Scale.get(), Animations.Scale.get());
    }

    private static void Push(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(side * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        GlStateManager.translate(side * -0.1414214, 0.08, 0.1414214);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
        GlStateManager.rotate((float) (f * -10.0F), 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate((float) (f1 * -10.0F), 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate((float) (f1 * -10.0F), 1.0F, 1.0F, 1.0F);
    }

    private static void WindMill(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(side * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        GlStateManager.translate(side * -0.1414214, 0.08, 0.1414214);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
        GlStateManager.rotate((float) (f * -20.0F), 0, 1, 0);
        GlStateManager.rotate((float) (f1 * -20.0F), 0, 0, 1);
        GlStateManager.rotate((float) (f1 * -50.0F), 1, 0, 0);

    }

    private static void Flux(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(side * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        GlStateManager.translate(side * -0.1414214, 0.08, 0.1414214);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
        GlStateManager.rotate((float) (f * -20.0F), 0, 1, 0);
        GlStateManager.rotate((float) (f1 * -20.0F), 0, 0, 1);
        GlStateManager.rotate((float) (f1 * -30.0F), 1, 0, 0);
    }

    private static void ETB(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, equippedProg * -0.6F, 0.0F);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        float var3 = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
        float var4 = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
        GlStateManager.rotate(var3 * -34.0F, 0.0F, 1.0F, 0.2F);
        GlStateManager.rotate(var4 * -20.7F, 0.2F, 0.1F, 1.0F);
        GlStateManager.rotate(var4 * -68.6F, 1.3F, 0.1F, 0.2F);
        GlStateManager.scale(Animations.Scale.get(), Animations.Scale.get(), Animations.Scale.get());
    }

    private static void sigmaold(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, equippedProg * -0.6F, 0.0F);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        float var3 = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
        float var4 = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
        GlStateManager.rotate(var3 * -15F, 0.0F, 1.0F, 0.2F);
        GlStateManager.rotate(var4 * -10F, 0.2F, 0.1F, 1.0F);
        GlStateManager.rotate(var4 * -30F, 1.3F, 0.1F, 0.2F);
    }

    private static void Zoom(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, swingProgress * -0.6F, 0.0F);
        GlStateManager.rotate(-102.25F, 1, 0, 0);
        GlStateManager.rotate(side * 13.365F, 0, 1, 0);
        GlStateManager.rotate(side * 78.050003F, 0, 0, 1);
        float var3 = MathHelper.sin(equippedProg * equippedProg * (float) Math.PI);
        float var4 = MathHelper.sin(MathHelper.sqrt(equippedProg) * (float) Math.PI);
        GlStateManager.rotate(var3 * -20.0F, 0.0F, 0.0F, 0.0F);
        GlStateManager.rotate(var4 * -20.0F, 0.0F, 0.0F, 0.0F);
        GlStateManager.rotate(var4 * -20.0F, 0.0F, 0.0F, 0.0F);
        GlStateManager.scale(Animations.Scale.get(), Animations.Scale.get(), Animations.Scale.get());
    }

    private static void old(EnumHandSide p_187459_1_, float equippedProg, float swingProgress) {
        int side = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.translate(side * 0.56f, -0.52f + equippedProg * -0.6f, -0.71999997f);
        float f = MathHelper.sin((swingProgress * swingProgress * (float) Math.PI));
        float f1 = MathHelper.sin((MathHelper.sqrt(swingProgress) * (float) Math.PI));
        GlStateManager.rotate(side * (45.0f + f * -20.0f), 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(side * f1 * -20.0f, 0.0f, 0.0f, 1.0f);
        GlStateManager.rotate(f1 * -80.0f, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(side * -45.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.scale(0.9f, 0.9f, 0.9f);
        GlStateManager.translate(-0.2f, 0.126f, 0.2f);
        GlStateManager.rotate(-102.25f, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(side * 15.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(side * 80.0f, 0.0f, 0.0f, 1.0f);
    }

    /**
     * @author CCBlueX
     * @reason Terra
     */
    @Overwrite
    public void renderItemInFirstPerson(AbstractClientPlayer player, float p_187457_2_, float p_187457_3_, EnumHand hand, float partialTicks, ItemStack stack, float p_187457_7_) {
        float f = -0.4F * MathHelper.sin(MathHelper.sqrt(partialTicks) * (float) Math.PI);
        float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt(partialTicks) * ((float) Math.PI * 2F));
        float f8 = player.getSwingProgress(partialTicks);
        float f2 = -0.2F * MathHelper.sin(partialTicks * (float) Math.PI);
        AbstractClientPlayer abstractclientplayer = this.mc.player;
        float f0 = abstractclientplayer.getSwingProgress(partialTicks);
        boolean flag = hand == EnumHand.MAIN_HAND;
        EnumHandSide enumhandside = flag ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
        GlStateManager.pushMatrix();

        if (stack.isEmpty()) {
            if (flag && !player.isInvisible()) {
                this.renderArmFirstPerson(p_187457_7_, partialTicks, enumhandside);
            }
        } else if (stack.getItem() instanceof ItemMap) {
            if (flag && this.itemStackOffHand.isEmpty()) {
                this.renderMapFirstPerson(p_187457_3_, p_187457_7_, partialTicks);
            } else {
                this.renderMapFirstPersonSide(p_187457_7_, enumhandside, partialTicks, stack);
            }
        } else {
            if (!(stack.getItem() instanceof ItemShield)) {

                final KillAura killAura = (KillAura) Terra.moduleManager.getModule(KillAura.class);

                boolean flag1 = enumhandside == EnumHandSide.RIGHT;

                if (player.isHandActive() && player.getItemInUseCount() > 0 && player.getActiveHand() == hand) {
                    int j = flag1 ? 1 : -1;

                    EnumAction enumaction = killAura.getBlockingStatus() ? EnumAction.BLOCK : stack.getItemUseAction();

                    switch (enumaction) {
                        case NONE:
                            this.transformSideFirstPerson(enumhandside, 0F);
                            break;
                        case BLOCK:
                            transformSideFirstPersonBlock(enumhandside, p_187457_7_, partialTicks);
                            //this.transformSideFirstPerson(enumhandside, p_187457_7_);
                            break;
                        case EAT:
                        case DRINK:
                            this.transformEatFirstPerson(p_187457_2_, enumhandside, stack);
                            this.transformSideFirstPerson(enumhandside, p_187457_7_);
                            if (Animations.INSTANCE.getRotateItems() && Animations.INSTANCE.getRotateItemsWhenEatingOrDrinkingValue().get())
                                rotateItemAnim();
                            break;
                        case BOW:
                            this.transformSideFirstPerson(enumhandside, p_187457_7_);
                            GlStateManager.translate((float) j * -0.2785682F, 0.18344387F, 0.15731531F);
                            GlStateManager.rotate(-13.935F, 1.0F, 0.0F, 0.0F);
                            GlStateManager.rotate((float) j * 35.3F, 0.0F, 1.0F, 0.0F);
                            GlStateManager.rotate((float) j * -9.785F, 0.0F, 0.0F, 1.0F);
                            float f5 = (float) stack.getMaxItemUseDuration() - ((float) this.mc.player.getItemInUseCount() - p_187457_2_ + 1.0F);
                            float f6 = f5 / 20.0F;
                            f6 = (f6 * f6 + f6 * 2.0F) / 3.0F;

                            if (f6 > 1.0F) {
                                f6 = 1.0F;
                            }

                            if (f6 > 0.1F) {
                                float f7 = MathHelper.sin((f5 - 0.1F) * 1.3F);
                                float f3 = f6 - 0.1F;
                                float f4 = f7 * f3;
                                GlStateManager.translate(f4 * 0.0F, f4 * 0.004F, f4 * 0.0F);
                            }

                            GlStateManager.translate(f6 * 0.0F, f6 * 0.0F, f6 * 0.04F);
                            GlStateManager.scale(1.0F, 1.0F, 1.0F + f6 * 0.2F);
                            GlStateManager.rotate((float) j * 45.0F, 0.0F, -1.0F, 0.0F);
                            break;
                    }
                } else {
                    mc.player.getHeldItemMainhand().getItem();
                    if ((mc.player.getHeldItemMainhand().getItem() instanceof ItemSword)
                            && Animations.INSTANCE.getBlockValue().get()
                            && ((killAura.getTarget() != null && killAura.getBlockingStatus())
                            || mc.gameSettings.keyBindUseItem.pressed
                    )) {
                        GlStateManager.translate(Animations.itemPosX.get(), Animations.itemPosY.get(), Animations.itemPosZ.get());
                        float SP = (Animations.INSTANCE.getSPValue().get() ? p_187457_7_ : 0);
                        switch (Animations.INSTANCE.getModeValue().get().toLowerCase()) {
                            case "1.7": {
                                transformSideFirstPersonBlock(enumhandside, SP, partialTicks);
                                if (Animations.INSTANCE.getRotateItems() && Animations.INSTANCE.getRotateItemWhenBlockingValue().get())
                                    rotateItemAnim();
                                break;
                            }
                            case "1.8": {
                                old(enumhandside, SP, partialTicks);
                                if (Animations.INSTANCE.getRotateItems() && Animations.INSTANCE.getRotateItemWhenBlockingValue().get())
                                    rotateItemAnim();
                                break;
                            }
                            case "old": {
                                transformSideFirstPersonBlock(enumhandside, -0.1F + SP, partialTicks);
                                if (Animations.INSTANCE.getRotateItems() && Animations.INSTANCE.getRotateItemWhenBlockingValue().get())
                                    rotateItemAnim();
                                break;
                            }
                            case "new1.8": {
                                transformSideFirstPersonBlock(enumhandside, p_187457_7_, partialTicks);
                                float f4 = MathHelper.sin(MathHelper.sqrt(partialTicks) * 3.83f);
                                GlStateManager.translate(0.0f, 0.0f, 0.0f);
                                GlStateManager.rotate(-f4 * 0.0f, 0.0f, 0.0f, 0.0f);
                                GlStateManager.rotate(-f4 * 35.0f, 58.0f, 23.0f, 45.0f);
                                if (Animations.INSTANCE.getRotateItems() && Animations.INSTANCE.getRotateItemWhenBlockingValue().get())
                                    rotateItemAnim();
                                break;
                            }

                            case "push": {
                                Push(enumhandside, SP, partialTicks);
                                if (Animations.INSTANCE.getRotateItems() && Animations.INSTANCE.getRotateItemWhenBlockingValue().get())
                                    rotateItemAnim();
                                break;
                            }
                            case "windmill": {
                                WindMill(enumhandside, -0.2F + SP, partialTicks);
                                if (Animations.INSTANCE.getRotateItems() && Animations.INSTANCE.getRotateItemWhenBlockingValue().get())
                                    rotateItemAnim();
                                break;
                            }
                            case "flux": {
                                Flux(enumhandside, SP, partialTicks);
                                if (Animations.INSTANCE.getRotateItems() && Animations.INSTANCE.getRotateItemWhenBlockingValue().get())
                                    rotateItemAnim();
                                break;
                            }
                            case "etb": {
                                ETB(enumhandside, SP, partialTicks);
                                if (Animations.INSTANCE.getRotateItems() && Animations.INSTANCE.getRotateItemWhenBlockingValue().get())
                                    rotateItemAnim();
                                break;
                            }
                            case "sigmaold": {
                                sigmaold(enumhandside, SP, partialTicks);
                                if (Animations.INSTANCE.getRotateItems() && Animations.INSTANCE.getRotateItemWhenBlockingValue().get())
                                    rotateItemAnim();
                                break;
                            }
                            case "zoom": {
                                Zoom(enumhandside, SP, partialTicks);
                                if (Animations.INSTANCE.getRotateItems() && Animations.INSTANCE.getRotateItemWhenBlockingValue().get())
                                    rotateItemAnim();
                                break;
                            }
                        }
                        GlStateManager.scale(Animations.Scale.get(), Animations.Scale.get(), Animations.Scale.get());
                    } else {
                        GlStateManager.translate(Animations.itemPosX.get(), Animations.itemPosY.get(), Animations.itemPosZ.get());
                        int i = flag1 ? 1 : -1;
                        GlStateManager.translate((float) i * f, f1, f2);
                        this.transformSideFirstPerson(enumhandside, p_187457_7_);
                        this.transformFirstPerson(enumhandside, partialTicks);
                        this.rotateItemAnim();
                        GlStateManager.scale(Animations.Scale.get(), Animations.Scale.get(), Animations.Scale.get());
                    }
                }

                this.renderItemSide(player, stack, flag1 ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !flag1);
            }
        }

        GlStateManager.popMatrix();
    }


    /**
     * @author CCBlueX
     * @reason Terra
     */


    @Inject(method = "renderFireInFirstPerson", at = @At("HEAD"), cancellable = true)
    private void renderFireInFirstPerson(final CallbackInfo callbackInfo) {
        final AntiBlind antiBlind = (AntiBlind) Terra.moduleManager.getModule(AntiBlind.class);

        if (antiBlind.getState() && antiBlind.getFireEffect().get()) callbackInfo.cancel();
    }

    private void rotateItemAnim() {
        if (Animations.INSTANCE.getRotateItems()) {
            if (Animations.INSTANCE.getTransformFirstPersonRotate().get().equalsIgnoreCase("RotateY")) {
                GlStateManager.rotate(this.delay, 0.0F, 1.0F, 0.0F);
            }
            if (Animations.INSTANCE.getTransformFirstPersonRotate().get().equalsIgnoreCase("RotateXY")) {
                GlStateManager.rotate(this.delay, 1.0F, 1.0F, 0.0F);
            }

            if (Animations.INSTANCE.getTransformFirstPersonRotate().get().equalsIgnoreCase("Custom")) {
                GlStateManager.rotate(this.delay, Animations.INSTANCE.getRotateX().get(), Animations.INSTANCE.getRotateY().get(), Animations.INSTANCE.getRotateZ().get());
            }

            if (this.rotateTimer.hasTimePassed(1)) {
                ++this.delay;
                this.delay = this.delay + Animations.INSTANCE.getSpeedRotate().get();
                this.rotateTimer.reset();
            }
            if (this.delay > 360.0F) {
                this.delay = 0.0F;
            }
        }
    }

}