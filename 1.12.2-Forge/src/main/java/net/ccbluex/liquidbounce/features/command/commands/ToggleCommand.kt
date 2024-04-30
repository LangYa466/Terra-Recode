/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.features.command.Command

class ToggleCommand : Command("toggle", "t") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            val module = Terra.moduleManager.getModule(args[1])

            if (module == null) {
                chat("模块 '${args[1]}' 未找到.")
                return
            }

            if (args.size > 2) {
                val newState = args[2].toLowerCase()

                if (newState == "on" || newState == "off") {
                    module.state = newState == "on"
                } else {
                    chatSyntax("toggle <module> [on/off]")
                    return
                }
            } else {
                module.toggle()
            }

            chat("${if (module.state) "启用" else "禁用"} 模块 §8${module.name}§3.")
            return
        }

        chatSyntax("toggle <module> [on/off]")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        val moduleName = args[0]

        return when (args.size) {
            1 -> Terra.moduleManager.modules
                    .map { it.name }
                    .filter { it.startsWith(moduleName, true) }
                    .toList()

            else -> emptyList()
        }
    }

}
