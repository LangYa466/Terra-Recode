/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command

class PingCommand : Command("ping") {

    override fun execute(args: Array<String>) {
        chat("§3你的延迟为 §a${mc.netHandler.getPlayerInfo(mc.thePlayer!!.uniqueID)!!.responseTime}ms§3.")
    }

}