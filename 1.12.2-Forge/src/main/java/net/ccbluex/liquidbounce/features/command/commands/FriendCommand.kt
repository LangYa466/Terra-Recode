/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.utils.misc.StringUtils

class FriendCommand : Command("friend", "friends") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            val friendsConfig = Terra.fileManager.friendsConfig

            when {
                args[1].equals("add", ignoreCase = true) -> {
                    if (args.size > 2) {
                        val name = args[2]

                        if (name.isEmpty()) {
                            chat("这个名字是空的.")
                            return
                        }

                        if (if (args.size > 3) friendsConfig.addFriend(name, StringUtils.toCompleteString(args, 3)) else friendsConfig.addFriend(name)) {
                            Terra.fileManager.saveConfig(friendsConfig)
                            chat("§a§l$name§3 玩家已添加至白名单.")
                            playEdit()
                        } else
                            chat("该玩家已在白名单列表.")
                        return
                    }
                    chatSyntax("friend add <name> [alias]")
                    return
                }

                args[1].equals("remove", ignoreCase = true) -> {
                    if (args.size > 2) {
                        val name = args[2]

                        if (friendsConfig.removeFriend(name)) {
                            Terra.fileManager.saveConfig(friendsConfig)
                            chat("§a§l$name§3 玩家已移除白名单.")
                            playEdit()
                        } else
                            chat("该玩家不在白名单.")
                        return
                    }
                    chatSyntax("friend remove <name>")
                    return
                }

                args[1].equals("clear", ignoreCase = true) -> {
                    val friends = friendsConfig.friends.size
                    friendsConfig.clearFriends()
                    Terra.fileManager.saveConfig(friendsConfig)
                    chat("清除 $friends 个白名单玩家.")
                    return
                }

                args[1].equals("list", ignoreCase = true) -> {
                    chat("你的白名单玩家列表:")

                    for (friend in friendsConfig.friends)
                        chat("§7> §a§l${friend.playerName} §c(§7§l${friend.alias}§c)")

                    chat("你有 §c${friendsConfig.friends.size}§3 个白名单玩家.")
                    return
                }
            }
        }

        chatSyntax("friend <add/remove/list/clear>")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("add", "remove", "list", "clear").filter { it.startsWith(args[0], true) }
            2 -> {
                when (args[0].toLowerCase()) {
                    "add" -> {
                        return mc.theWorld!!.playerEntities
                                .filter { (it.name?.startsWith(args[1], true) ?: false) }
                                .map { it.name!! }
                    }
                    "remove" -> {
                        return Terra.fileManager.friendsConfig.friends
                                .map { it.playerName }
                                .filter { it.startsWith(args[1], true) }
                    }
                }
                return emptyList()
            }
            else -> emptyList()
        }
    }
}