/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.block;

import net.ccbluex.liquidbounce.Terra;
import net.ccbluex.liquidbounce.event.BlockBBEvent;
import net.ccbluex.liquidbounce.features.module.modules.combat.Criticals;
import net.ccbluex.liquidbounce.features.module.modules.exploit.GhostHand;
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall;
import net.ccbluex.liquidbounce.features.module.modules.render.XRay;
import net.ccbluex.liquidbounce.features.module.modules.world.NoSlowBreak;
import net.ccbluex.liquidbounce.injection.backend.AxisAlignedBBImplKt;
import net.ccbluex.liquidbounce.injection.backend.BlockImplKt;
import net.ccbluex.liquidbounce.injection.backend.utils.BackendExtentionsKt;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@Mixin(Block.class)
@SideOnly(Side.CLIENT)
public abstract class MixinBlock {
    @Shadow
    @Final
    protected BlockStateContainer blockState;

    /**
     * @author CCBlueX
     * @reason Terra
     */
    @Overwrite
    protected static void addCollisionBoxToList(BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable AxisAlignedBB blockBox) {
        if (blockBox != null) {
            AxisAlignedBB axisalignedbb = blockBox.offset(pos);

            WorldClient world = Minecraft.getMinecraft().world;

            if (world != null) {
                BlockBBEvent blockBBEvent = new BlockBBEvent(BackendExtentionsKt.wrap(pos), BlockImplKt.wrap(world.getBlockState(pos).getBlock()), AxisAlignedBBImplKt.wrap(axisalignedbb));
                Terra.eventManager.callEvent(blockBBEvent);

                axisalignedbb = blockBBEvent.getBoundingBox() == null ? null : AxisAlignedBBImplKt.unwrap(blockBBEvent.getBoundingBox());
            }

            if (axisalignedbb != null && entityBox.intersects(axisalignedbb)) {
                collidingBoxes.add(axisalignedbb);
            }
        }
    }

    @Shadow
    public abstract AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos);

    // Has to be implemented since a non-virtual call on an abstract method is illegal
    @Shadow
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return null;
    }

    @Inject(method = "shouldSideBeRendered", at = @At("HEAD"), cancellable = true)
    private void shouldSideBeRendered(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        final XRay xray = (XRay) Terra.moduleManager.getModule(XRay.class);

        if (Objects.requireNonNull(xray).getState())
            //noinspection SuspiciousMethodCalls
            callbackInfoReturnable.setReturnValue(xray.getXrayBlocks().contains(BlockImplKt.wrap((Block) (Object) this)));
    }

    @Inject(method = "isCollidable", at = @At("HEAD"), cancellable = true)
    private void isCollidable(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        final GhostHand ghostHand = (GhostHand) Terra.moduleManager.getModule(GhostHand.class);

        if (Objects.requireNonNull(ghostHand).getState() && !(ghostHand.getBlockValue().get() == Block.getIdFromBlock((Block) (Object) this)))
            callbackInfoReturnable.setReturnValue(false);
    }

    @Inject(method = "getAmbientOcclusionLightValue", at = @At("HEAD"), cancellable = true)
    private void getAmbientOcclusionLightValue(final CallbackInfoReturnable<Float> floatCallbackInfoReturnable) {
        if (Objects.requireNonNull(Terra.moduleManager.getModule(XRay.class)).getState())
            floatCallbackInfoReturnable.setReturnValue(1F);
    }

    @Inject(method = "getPlayerRelativeBlockHardness", at = @At("RETURN"), cancellable = true)
    public void modifyBreakSpeed(IBlockState state, EntityPlayer playerIn, World worldIn, BlockPos pos, final CallbackInfoReturnable<Float> callbackInfo) {
        float f = callbackInfo.getReturnValue();

        // NoSlowBreak
        final NoSlowBreak noSlowBreak = (NoSlowBreak) Terra.moduleManager.getModule(NoSlowBreak.class);
        if (Objects.requireNonNull(noSlowBreak).getState()) {
            if (noSlowBreak.getWaterValue().get() && playerIn.isInsideOfMaterial(Material.WATER) &&
                    !EnchantmentHelper.getAquaAffinityModifier(playerIn)) {
                f *= 5.0F;
            }

            if (noSlowBreak.getAirValue().get() && !playerIn.onGround) {
                f *= 5.0F;
            }
        } else if (playerIn.onGround) { // NoGround
            final NoFall noFall = (NoFall) Terra.moduleManager.getModule(NoFall.class);
            final Criticals criticals = (Criticals) Terra.moduleManager.getModule(Criticals.class);

            if (Objects.requireNonNull(noFall).getState() && noFall.modeValue.get().equalsIgnoreCase("NoGround") ||
                    Objects.requireNonNull(criticals).getState() && criticals.getModeValue().get().equalsIgnoreCase("NoGround")) {
                f /= 5F;
            }
        }

        callbackInfo.setReturnValue(f);
    }
}