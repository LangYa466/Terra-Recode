/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo

@ModuleInfo(name = "AntiCactus", description = "Prevents cactuses from damaging you.", category = ModuleCategory.PLAYER)
class AntiCactus : Module() {

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        if (classProvider.isBlockCactus(event.block))
            event.boundingBox = classProvider.createAxisAlignedBB(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(),
                    event.x + 1.0, event.y + 1.0, event.z + 1.0)
    }
}
