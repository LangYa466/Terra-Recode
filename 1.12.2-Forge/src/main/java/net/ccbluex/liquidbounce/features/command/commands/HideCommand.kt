/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils

class HideCommand : Command("hide") {

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            when {
                args[1].equals("list", true) -> {
                    chat("§c§l隐藏模块列表")
                    Terra.moduleManager.modules.filter { !it.array }.forEach {
                        ClientUtils.displayChatMessage("§6> §c${it.name}")
                    }
                    return
                }

                args[1].equals("clear", true) -> {
                    for (module in Terra.moduleManager.modules)
                        module.array = true

                    chat("清除所有已隐藏模块.")
                    return
                }

                args[1].equals("reset", true) -> {
                    for (module in Terra.moduleManager.modules)
                        module.array = module::class.java.getAnnotation(ModuleInfo::class.java).array

                    chat("已重置隐藏模块列表.")
                    return
                }

                else -> {
                    // Get module by name
                    val module = Terra.moduleManager.getModule(args[1])

                    if (module == null) {
                        chat("模块 §a§l${args[1]}§3 未找到.")
                        return
                    }

                    // Find key by name and change
                    module.array = !module.array

                    // Response to user
                    chat("模块 §a§l${module.name}§3 现在 §a§l${if (module.array) "可见" else "不可见"}§3 在列表.")
                    playEdit()
                    return
                }
            }
        }

        chatSyntax("hide <module/list/clear/reset>")
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