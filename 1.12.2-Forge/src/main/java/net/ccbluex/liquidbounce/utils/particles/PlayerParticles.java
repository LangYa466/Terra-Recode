/*
 * Terra Hacked Client
 */

package net.ccbluex.liquidbounce.utils.particles;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;

public class PlayerParticles {

    public static Block getBlock(final double offsetX, final double offsetY, final double offsetZ) {
        return MinecraftInstance.mc2.world.getBlockState(new BlockPos(offsetX, offsetY, offsetZ)).getBlock();
    }

}
