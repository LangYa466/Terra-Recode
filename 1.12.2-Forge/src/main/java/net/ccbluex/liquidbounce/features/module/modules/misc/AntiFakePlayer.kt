package net.ccbluex.liquidbounce.features.module.modules.misc

import com.sun.corba.se.impl.activation.ServerMain.logError
import net.ccbluex.liquidbounce.Terra.fileManager
import net.ccbluex.liquidbounce.event.EntityKilledEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.utils.ClientUtils.displayChatMessage
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.server.SPacketChat
import java.util.regex.Pattern

@ModuleInfo(name = "AntiFakePlayer", description = "Made and Fixed by RyF with ❤", category = ModuleCategory.MISC)
class AntiFakePlayer : Module() {

    private val logStyles = arrayOf(
        "RyFNew",
        "FDPAntibot",
        "FDPChat",
        "Leave",
        "Special",
        "WaWa1",
        "WaWa2",
        "NullClient",
        "Normal",
        "WindX",
        "Old",
        "Arene"
    )
    private val multiKillMessageList = arrayOf(
        "正在大杀特杀",
        "主宰服务器",
        "杀人如麻",
        "无人能挡",
        "杀得变态了",
        "正在像妖怪般杀戮",
        "如同神一般",
        "已经超越神了！拜托谁去杀了他吧"
    )
    private val kitSpecialDeathChats = arrayOf(
        "走着走着突然暴毙了!",
        "Boom！！!"
    )
    private val kitSpecialSkillChats = arrayOf(
        "对你眨眼了!",
        "诅咒了!",
        "并没有使用作弊!"
    )

    // ?
    private val autoModeValue = BoolValue("AutoSwitch", true)

    // 主功能
    // 开自动切换就别自己选了  又不会出问题
    private val modeValue = ListValue(
        "Mode",
        arrayOf("4v4/2v2/1v1", "BWXP32", "BWXP16", "KitBattle"),
        "4V4/2v2/1V1"
    ).displayable { !autoModeValue.get() }

    private val addBotOnKill = BoolValue("NewBotOnKill", false)
    private val addBotOnKillDelay = IntegerValue("BotDelay", 5000, 3000, 8000).displayable { addBotOnKill.get() }


    // 日志模式
    private val printLoggerValue = BoolValue("ShowChatMessage", false)
    private val logStyleValue = ListValue("LogStyle", logStyles, "Normal").displayable { printLoggerValue.get() }

    // 隐藏击杀消息, 支持起床&&职业战争
    private val hideKillChatValue = BoolValue("HideKillChat", false)
    private val showMyKillDeathChatValue =
        BoolValue("ShowMyKillDeathChat", false).displayable { hideKillChatValue.get() }

    // 职业战争自定义延迟
    private val kitCustomDelayValue =
        IntegerValue("KitCustomDelay", 4700, 4000, 8000).displayable { modeValue.get().toLowerCase() == "kitbattle" }

    // 职业战争隐藏银币消息
    private val kitHideKillCoinValue =
        BoolValue("HideKitBattleCoinChat", true).displayable { modeValue.get().toLowerCase() == "kitbattle" }

    // 职业战争隐藏连死消息
    private val kitHideDeathStreakValue =
        BoolValue("HideKitDeathStreakChat", false).displayable { modeValue.get().toLowerCase() == "kitbattle" }

    // 职业奇死
    private val kitHideSpecialDeathValue =
        BoolValue("HideKitSpecialDeathChat", false).displayable { modeValue.get().toLowerCase() == "kitbattle" }

    // 职业技能
    private val kitHideSkillChatValue =
        BoolValue("HideKitSkillChat", false).displayable { modeValue.get().toLowerCase() == "kitbattle" }
    private val kitHidePlayerUpgradeChatValue =
        BoolValue("HideKitUpgradeChat", false).displayable { modeValue.get().toLowerCase() == "kitbattle" }

    // 隐藏连杀消息, 支持职业战争
    private val hideMultiKillChatValue = BoolValue("HideMultiKillChat", true)
    private val showMyMultiKillChatValue =
        BoolValue("ShowMyMultiKillChat", true).displayable { hideMultiKillChatValue.get() }

    // 用原版 §k 隐藏英文
    private val fakeNameProtectValue = BoolValue("ShittyNameProtect(OnlyEnglish", false)

    // 屏蔽Antigetname消息 不影响无效化
    private val hideAntiGetNameValue = BoolValue("IgnoreAntiGetname", false)
    private val chatHiddenValue = BoolValue("IgnoredChat", false).displayable { hideAntiGetNameValue.get() }

    // 自动切日志模式
    private val autoSwitchLogger = BoolValue("AutoSwitchLogger", false)
    private val autoSwitchModeValue = ListValue(
        "AutoSwitchMode", arrayOf(
            "Random",
            "List"
        ), "Random"
    ).displayable { autoSwitchLogger.get() }
    private val autoSwitchDelay =
        IntegerValue("AutoSwitchDelay", 3000, 1500, 7000).displayable { autoSwitchLogger.get() }

    // 短tag设置
    private val sTShowOptions = BoolValue("ShowShortTagOptions", false)

    // 隐藏所有
    private val sTHideAll = BoolValue("HideAllTag", false).displayable { sTShowOptions.get() }

    // 隐藏模式
    private val sTHideMode = BoolValue("HideMode", false).displayable { sTShowOptions.get() && !sTHideAll.get() }

    // 隐藏日志模式
    private val sTHideLogStyle =
        BoolValue("HideLogStyle", false).displayable { sTShowOptions.get() && !sTHideAll.get() }

    // 隐藏无敌人数量开关
    private val sTHideBotsCounter =
        BoolValue("HideBotsCounter", false).displayable { sTShowOptions.get() && !sTHideAll.get() }

    // 设置
    private val sTHideBotsCounterSetting = ListValue(
        "HideBotsCounterMode",
        arrayOf("FullHide", "OnlyNumber"),
        "OnlyNumber"
    ).displayable { sTShowOptions.get() && !sTHideAll.get() && sTHideBotsCounter.get() }

    // 隐藏所有自动切日志
    private val sTHideAllAutoSwitcher =
        BoolValue("HideAllAutoSwitcher", false).displayable { sTShowOptions.get() && !sTHideAll.get() }

    // 隐藏状态开关
    private val sTHideAutoSwitcherState = BoolValue(
        "HideAutoSwitcherState",
        false
    ).displayable { sTShowOptions.get() && !sTHideAllAutoSwitcher.get() && !sTHideAll.get() }

    // 隐藏状态设置
    private val sTHideAutoSwitcherStateSetting = ListValue(
        "HideAutoSwitcherMode",
        arrayOf("FullHide", "OnlyShowState"),
        "FullHide"
    ).displayable { sTShowOptions.get() && !sTHideAllAutoSwitcher.get() && sTHideAutoSwitcherState.get() && !sTHideAll.get() }

    // 隐藏模式
    private val sTHideAutoSwitcherMode = BoolValue(
        "HideAutoSwitcherMode",
        false
    ).displayable { sTShowOptions.get() && !sTHideAllAutoSwitcher.get() && !sTHideAll.get() }

    // 隐藏延迟
    private val sTHideAutoSwitcherDelay = BoolValue(
        "HideAutoSwitcherDelay",
        false
    ).displayable { sTShowOptions.get() && !sTHideAllAutoSwitcher.get() && !sTHideAll.get() }

    // 变量
    private var bots = 0
    private var logNumber = 0
    private val ms = MSTimer()
    private var protectedname = ""

    // 关闭模块清除bot
    @EventTarget
    override fun onDisable() {
        bots = 0
        clearAll()
        super.onDisable()
    }

    /*
    主功能
    playerDeathAction 是无敌人
    playerDeathMsgAction 是日志
     */
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet.unwrap()
        if ((packet is SPacketChat) && !packet.chatComponent.unformattedText.contains(":") && (packet.chatComponent.unformattedText.startsWith(
                "起床战争"
            ) || packet.chatComponent.unformattedText.startsWith("[起床战争") || packet.chatComponent.unformattedText.startsWith(
                "花雨庭"
            ))
        ) {
            val chat = packet.chatComponent.unformattedText
            when (modeValue.get().toLowerCase()) {
                // 4v4 2v2 1v1 起床
                "4v4/2v2/1v1" -> {
                    val matcher = Pattern.compile("杀死了 (.*?)\\(").matcher(chat)
                    val matcher2 = Pattern.compile("起床战争>> (.*?) (\\(((.*?)死了!))").matcher(chat)
                    val matcher3 = Pattern.compile("杀死了 (.*?)\\[").matcher(chat)
                    val matcher4 = Pattern.compile("起床战争>> (.*?) \\[").matcher(chat)
                    if (matcher.find()) {
                        val name = matcher.group(1).trim()
                        playerDeathAction(name, 4988)
                        playerDeathMsgAction(event)
                    }
                    if (matcher2.find()) {
                        val name = matcher2.group(1).trim()
                        playerDeathAction(name, 4988)
                        playerDeathMsgAction(event)
                    }
                    if (matcher3.find()) {
                        val name = matcher3.group(1).trim()
                        playerDeathAction(name, 4988)
                        playerDeathMsgAction(event)
                    }
                    if (matcher4.find()) {
                        val name = matcher4.group(1).trim()
                        playerDeathAction(name, 4988)
                        playerDeathMsgAction(event)
                    }
                }

                // 经验32 起床
                "bwxp32" -> {
                    val matcher = Pattern.compile("杀死了 (.*?)\\(").matcher(chat)
                    val matcher2 = Pattern.compile("起床战争 >> (.*?) (\\(((.*?)死了!))").matcher(chat)
                    val matcher3 = Pattern.compile("起床战争 >> (.*?)(\\(((.*?)死了!))").matcher(chat)
                    if (matcher.find()) {
                        val name = matcher.group(1).trim()
                        playerDeathAction(name, 7400)
                        playerDeathMsgAction(event)
                    }
                    if (matcher2.find()) {
                        val name = matcher2.group(1).trim()
                        playerDeathAction(name, 4988)
                        playerDeathMsgAction(event)
                    }
                    if (matcher3.find()) {
                        val name = matcher3.group(1).trim()
                        playerDeathAction(name, 4988)
                        playerDeathMsgAction(event)
                    }
                }

                // 经验16 起床
                "bwxp16" -> {
                    val matcher = Pattern.compile("击败了 (.*?)!").matcher(chat)
                    val matcher2 = Pattern.compile("玩家 (.*?)死了！").matcher(chat)
                    if (matcher.find()) {
                        val name = matcher.group(1).trim()
                        playerDeathAction(name, 9700)
                        playerDeathMsgAction(event)
                    }
                    if (matcher2.find()) {
                        val name = matcher2.group(1).trim()
                        playerDeathAction(name, 9700)
                        playerDeathMsgAction(event)
                    }
                }

                // 职业战争 不用测试了 有效
                "kitbattle" -> {
                    if (chat.startsWith("花雨庭 >>") && chat.contains("你的 coins 被修正为")) return
                    val matcher = Pattern.compile("击杀了(.*?) !").matcher(chat)
                    val matcher2 = Pattern.compile("花雨庭 >>(.*?) 被").matcher(chat)
                    if (matcher.find()) {
                        val name = matcher.group(1).trim()
                        playerDeathAction(name, kitCustomDelayValue.get().toLong())
                        playerDeathMsgAction(event)
                    }
                    if (matcher2.find()) {
                        val name = matcher2.group(1).trim()
                        playerDeathAction(name, kitCustomDelayValue.get().toLong())
                        playerDeathMsgAction(event)
                    }
                }
            }
        }

        // 屏蔽Antigetname
        if (packet is SPacketChat && hideAntiGetNameValue.get() && packet.chatComponent.unformattedText.contains(
                ":"
            ) && !packet.chatComponent.unformattedText.contains(
                (
                        mc.thePlayer!!.displayNameString + ":")
            ) && (packet.chatComponent.unformattedText.contains(
                "起床战争"
            ) && !packet.chatComponent.unformattedText.contains(
                "01:00:00 是这个地图的记录!"
            ) && !packet.chatComponent.unformattedText.contains(
                "之队队设置一个新的记录:"
            )
                    )
        ) {
            event.cancelEvent()
            if (chatHiddenValue.get()) {
                displayChatMessage("§b${HUD.customClientName.get()} §7» §c隐藏了AntiGetname消息。")
            }
        }

        // 隐藏连杀消息
        if (packet is SPacketChat) {
            val chat = packet.chatComponent.unformattedText
            for (it in multiKillMessageList) {
                if (chat.startsWith("起床战争")) continue
                if ((chat.contains(it) || chat.equals(it)) && !(showMyMultiKillChatValue.get() && chat.contains(mc.thePlayer!!.displayNameString))) {
                    if (hideMultiKillChatValue.get()) event.cancelEvent()
                }
            }
        }

        // 职业战争相关
        if (packet is SPacketChat) {
            val chat = packet.chatComponent.unformattedText
            if (modeValue.get().toLowerCase() == "kitbattle" && chat.startsWith("花雨庭")) {
                // 职业隐藏银币消息
                if (kitHideKillCoinValue.get() && ((chat.startsWith("花雨庭 >>你消灭") && chat.contains("% 的伤害并且获得了") && chat.endsWith(
                        "硬币!"
                    )) || chat.contains("你的 coins 被修正为"))
                ) {
                    event.cancelEvent()
                }

                // 职业隐藏连杀消息
                if ((chat.contains("花雨庭 >>") && chat.contains("完成了") && chat.contains("连杀!")) && !(showMyMultiKillChatValue.get() && chat.contains(
                        mc.thePlayer!!.displayNameString
                    ))
                ) {
                    if (hideMultiKillChatValue.get()) event.cancelEvent()
                }

                // 职业隐藏连死消息
                if ((chat.contains("花雨庭 >>") && (
                            chat.contains("has ended his deathstreak and lost his buff") ||
                                    chat.contains("is now receiving a buff for his deathstreak") ||
                                    chat.contains("终结了他的连续死亡") ||
                                    chat.contains("获得了一个buff因为他刚刚完成了")
                            ))
                ) {
                    if (kitHideDeathStreakValue.get()) event.cancelEvent()
                }

                // 职业隐藏奇奇怪怪死亡消息 (不完全
                for (it in kitSpecialDeathChats) {
                    if ((chat.contains(it) || chat.equals(it))) {
                        if (kitHideSpecialDeathValue.get()) event.cancelEvent()
                    }
                }

                // 职业隐藏技能使用消息 (不完全
                for (it in kitSpecialSkillChats) {
                    if ((chat.contains(it) || chat.equals(it))) {
                        if (kitHideSkillChatValue.get()) event.cancelEvent()
                    }
                }

                // 职业隐藏升级消息
                if (kitHidePlayerUpgradeChatValue.get() && chat.contains("通过击杀获得胜点的方式晋级为"))
                    event.cancelEvent()
            }
        }

        // 自动切换模式 不清楚是否有效
        if (packet is SPacketChat && autoModeValue.get()) {
            val chat = packet.chatComponent.unformattedText
            if (chat.startsWith("花雨庭 >>") && modeValue.get().toLowerCase() != "kitbattle") {
                modeValue.set("KitBattle")
                displayChatMessage("§7[§dAntiFP§7] §f自动切换模式为§c职业战争§7。")
            }
            if (chat.startsWith("起床战争>>") && modeValue.get().toLowerCase() != "4v4/2v2/1v1") {
                modeValue.set("4v4/2v2/1v1")
                displayChatMessage("§7[§dAntiFP§7] §f自动切换模式为§c4v4/2v2/1v1§7。")
            }
            if (chat.startsWith("起床战争 >>") && modeValue.get().toLowerCase() != "bwxp32") {
                modeValue.set("BWXP32")
                displayChatMessage("§7[§dAntiFP§7] §f自动切换模式为§c经验32§7。")
            }
            if (chat.startsWith("[起床战争]") && modeValue.get().toLowerCase() != "bwxp16") {
                modeValue.set("BWXP16")
                displayChatMessage("§7[§dAntiFP§7] §f自动切换模式为§c经验16§7。")
            }
        }
    }

    // 自动切换日志
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (bots < 0) bots = 0
        if (autoSwitchLogger.get() && ms.hasTimePassed(autoSwitchDelay.get().toLong())) {
            when (autoSwitchModeValue.get().toLowerCase()) {
                "random" -> logStyleValue.set(logStyles[RandomUtils.nextInt(0, logStyles.size - 1)])
                "list" -> {
                    if (logNumber != logStyles.size - 1) {
                        logNumber++
                        logStyleValue.set(logStyles[logNumber])
                    } else {
                        logNumber = 0
                        logStyleValue.set(logStyles[logNumber])
                    }
                }
            }
            ms.reset()
        }
    }

    // 隐藏击杀&死亡消息
    private fun playerDeathMsgAction(event: PacketEvent) {
        val packet = event.packet.unwrap()
        if ((packet is SPacketChat) && hideKillChatValue.get() && !(showMyKillDeathChatValue.get() && packet.chatComponent.unformattedText.contains(
                mc.thePlayer!!.displayNameString
            ))
        ) {
            event.cancelEvent()
        }
    }

    // 加减无敌人
    private fun playerDeathAction(name: String, cd: Long) {
        try {
            bots++
            fileManager.friendsConfig.addFriend(name)
        } catch (ex: Exception) {
            ex.printStackTrace()
            logError("添加 $name 失败。")
        }
        protectedname = if (fakeNameProtectValue.get()) "§7§k$name" else "§7$name"
        if (printLoggerValue.get()) printLogger(protectedname, "add")
        Thread {
            try {
                Thread.sleep(cd)
                protectedname = if (fakeNameProtectValue.get()) "§7§k$name" else "§7$name"
                fileManager.friendsConfig.removeFriend(name)
                bots--
                if (printLoggerValue.get()) printLogger(protectedname, "remove")
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }.start()
    }

    // 日志
    private fun printLogger(name: String, mode: String) {
        when (mode.toLowerCase()) {
            "add" -> {
                when (logStyleValue.get().toLowerCase()) {
                    "ryfnew" -> displayChatMessage("§aAdded§f HYT Bot §7-> §e$name")
                    "fdpantibot" -> displayChatMessage("§7[§cAntiBot§7] §fAdded §7$name§f due to it being a bot.")
                    "fdpchat" -> displayChatMessage("§f[§c!§f] §7>> §aAdded §6HYT bot§f[$name§f]§6.")
                    "leave" -> displayChatMessage("§8[§eWARNING§8] §6添加无敌人: $name")
                    "special" -> displayChatMessage("§a$name§d被§bRyF§d吃掉啦! §bCiallo(∠・ω< )⌒☆")
                    "wawa1" -> displayChatMessage("§7=> §fAdded Bot §7$name§f.")
                    "wawa2" -> displayChatMessage("§f玩家死亡: §7$name")
                    "nullclient" -> displayChatMessage("§7[§cAntiBots§7] §fAdded a bot(§7$name§f)")
                    "normal" -> displayChatMessage("§fAdded HYT Bot: §7$name")
                    "windx" -> displayChatMessage("§aClient §7=> §aAdded §fa bot(§7$name§f)")
                    "old" -> displayChatMessage("§d添加无敌人：§7$name")
                    "arene" -> displayChatMessage("§fAdd a Bot(§7$name§f)")
                }
            }

            "remove" -> {
                when (logStyleValue.get().toLowerCase()) {
                    "ryfnew" -> displayChatMessage("§cRemoved§f HYT Bot §7-> §e$name")
                    "fdpantibot" -> displayChatMessage("§7[§cAntiBot§7] §fRemoved §7$name§f due to respawn.")
                    "fdpchat" -> displayChatMessage("§f[§c!§f] §7>> §cRemoved §6HYT bot§f[$name§f]§6.")
                    "leave" -> displayChatMessage("§8[§eWARNING§8] §6删除无敌人: $name")
                    "special" -> displayChatMessage("§a$name§d被§bRyF§d吐出来咯~ §bCiallo(∠・ω< )⌒☆")
                    "wawa1" -> displayChatMessage("§7=> §fRemoved Bot §7$name§f.")
                    "wawa2" -> displayChatMessage("§f玩家重生: §7$name")
                    "nullclient" -> displayChatMessage("§7[§cAntiBots§7] §fRemoved a bot(§7$name§f)")
                    "normal" -> displayChatMessage("§fRemoved HYT Bot: §7$name")
                    "windx" -> displayChatMessage("§aClient §7=> §cRemoved §fa bot(§7$name§f)")
                    "old" -> displayChatMessage("§d删除无敌人：§7$name")
                    "arene" -> displayChatMessage("§fDel a Bot(§7$name§f)")
                }
            }
        }
    }

    // sb
    private fun clearAll() {
        fileManager.friendsConfig.clearFriends()
        bots = 0
    }

    // tag获取
    private fun tagReturner(): String {
        var tag = ""
        if (sTHideAll.get()) {
            tag = ""
        } else {
            if (!sTHideMode.get()) tag = modeValue.get()
            if (!sTHideLogStyle.get() && printLoggerValue.get()) tag =
                if (tag != "") "$tag, ${logStyleValue.get()}" else logStyleValue.get()

            if (!sTHideBotsCounter.get()) {
                if (tag != "") tag = "$tag, Bots: $bots" else "Bots: $bots"
            } else {
                if (sTHideBotsCounterSetting.get().toLowerCase() == "onlynumber") if (tag != "") tag =
                    "$tag, $bots" else bots
            }

            if (autoSwitchLogger.get() && !sTHideAllAutoSwitcher.get()) {
                if (tag != "" && !sTHideAutoSwitcherState.get()) tag =
                    "$tag, AutoSwitch:${if (autoSwitchLogger.get()) "On" else "Off"}"
                if (sTHideAutoSwitcherState.get() && sTHideAutoSwitcherStateSetting.get()
                        .toLowerCase() == "onlyshowstate"
                ) {
                    tag =
                        if (tag != "") "$tag, ${if (autoSwitchLogger.get()) "On" else "Off"}" else if (autoSwitchLogger.get()) "On" else "Off"
                }

                if (!sTHideAutoSwitcherMode.get()) tag =
                    if (tag != "") "$tag, ${autoSwitchModeValue.get()}" else autoSwitchModeValue.get()

                if (!sTHideAutoSwitcherDelay.get()) tag =
                    if (tag != "") "$tag, ${autoSwitchDelay.get()}" else "${autoSwitchDelay.get()}"
            }
        }
        return tag
    }

    @EventTarget
    fun onEntityKilled(event: EntityKilledEvent) {
        val target = event.targetEntity!!.name!!
        if (addBotOnKill.get()) playerDeathAction(target, addBotOnKillDelay.get().toLong())
    }

    override val tag: String?
        get() = if (!sTHideAll.get()) tagReturner() else null
}