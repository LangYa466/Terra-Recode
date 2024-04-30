/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.backend.WorldClientImpl
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.injection.backend.utils.unwrap
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.block.BlockLiquid
import net.minecraft.block.BlockWeb
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "NoObstacle", description = "OMG.", category = ModuleCategory.MOVEMENT)
class NoObstacle : Module() {
    private val noWaterValue = BoolValue("NoWater", true)
    private val noLiquid = BoolValue("DisLiquid", false).displayable { noWaterValue.get() }
    private val noWebValue = BoolValue("NoWeb", true)
    private val noLavaValue = BoolValue("NoLava", true)
    private val disRangeValue = IntegerValue("Range", 10, 2, 10)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val theWorld = mc.theWorld
        val searchBlocks = BlockUtils.searchBlocks(disRangeValue.get())
        val searchBlocks2 = BlockUtils.searchBlocks(3)

        for (block in searchBlocks) {
            val blockPos = block.key.unwrap()
            val blocks = block.value.unwrap()

            if (blocks is BlockLiquid && noWaterValue.get()) {
                mc2.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.DOWN))
                mc2.player.inWater = false
                if (noLiquid.get()) {
                    (theWorld as WorldClientImpl).wrapped.setBlockToAir(blockPos)
                }
            }

            if (blocks == Blocks.LAVA && noLavaValue.get()) {
                mc2.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.DOWN))
                (theWorld as WorldClientImpl).wrapped.setBlockToAir(blockPos)
            }

        }

        for (block in searchBlocks2) {
            val blockPos = block.key.unwrap()
            val blocks = block.value.unwrap()

            if (blocks is BlockWeb && noWebValue.get()) {
                mc2.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.DOWN))
                mc2.player.isInWeb = false
            }
        }

    }
}
