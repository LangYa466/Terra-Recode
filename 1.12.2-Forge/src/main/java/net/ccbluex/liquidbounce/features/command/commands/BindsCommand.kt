/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import org.lwjgl.input.Keyboard

class BindsCommand : Command("binds") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            if (args[1].equals("clear", true)) {
                for (module in Terra.moduleManager.modules)
                    module.keyBind = Keyboard.KEY_NONE

                chat("已移除所有模块热键.")
                return
            }

            if (args[1].equals("reset", true)) {
                for (module in Terra.moduleManager.modules)
                    module.keyBind = module::class.java.getAnnotation(ModuleInfo::class.java).keyBind

                chat("已重置绑定模块列表.")
                return
            }
        }

        chat("§c§l已绑定模块")
        Terra.moduleManager.modules.filter { it.keyBind != Keyboard.KEY_NONE }.forEach {
            ClientUtils.displayChatMessage("§6> §c${it.name}: §a§l${Keyboard.getKeyName(it.keyBind)}")
        }
        chatSyntax("binds clear 输入该指令清除所有绑定热键")
    }
}