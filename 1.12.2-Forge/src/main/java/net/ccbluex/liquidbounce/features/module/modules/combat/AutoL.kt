/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntity
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.randomString
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import java.util.*

@ModuleInfo("AutoL", description = "LOL", category = ModuleCategory.COMBAT)
class AutoL : Module() {
    private val delayValue = IntegerValue("ChatDelay", 3000, 2400, 6000)
    private val mode = ListValue("Mode", arrayOf("Custom", "R18", "Pure", "NSX"), "NSX")
    private val textValue = TextValue("CustomText", "Terra Client Free Config 837524402^^").displayable { mode.get().equals("Custom",ignoreCase = true) }
    private val waterMarkValue = BoolValue("Watermark",true)
    private val atAllPlayer = BoolValue("@All",false)
    private val randomString = BoolValue("RandomString",false)

    private val lastAttackTimer = MSTimer()
    var target: IEntity? = null
    private var text = ""
    private val delay = MSTimer()
    private var inCombat = false
    private val attackedEntityList = mutableListOf<IEntity>()

    @EventTarget
    fun onWorld(event: WorldEvent) {
        attackedEntityList.clear()
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        val target = event.targetEntity

        if ((target is IEntity) && EntityUtils.isSelected(target, true)) {
            this.target = target
            if (!attackedEntityList.contains(target)) {
                attackedEntityList.add(target)
            }
        }
        lastAttackTimer.reset()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        attackedEntityList.filter { it.isDead }.forEach {
            playerDeathEvent(it.name!!)
            attackedEntityList.remove(it)
        }
        inCombat = false

        if (!lastAttackTimer.hasTimePassed(1000)) {
            inCombat = true
            return
        }
        if (target != null) {
            if (mc.thePlayer!!.getDistanceToEntity(target!!) > 7 || !inCombat || target!!.isDead) {
                target = null
            } else {
                inCombat = true
            }
        }
    }

    private fun playerDeathEvent(name: String) {
        if (delay.hasTimePassed(delayValue.get().toLong())) {
            playerChat(name)
            delay.reset()
        }
    }

    private fun playerChat(name: String) {
        when (mode.get().toLowerCase()) {
            "custom" -> text = (textValue.get())
            "r18" -> text = getR18Text()
            "pure" -> text = getPureText()
            "nsx" -> text = getNSXText()
        }

        var message = text
        if (waterMarkValue.get()) message = "[${HUD.customClientName.get()}]$message"
        if (atAllPlayer.get()) message = "@$message"
        if (randomString.get()) message += " >${randomString(5 + Random().nextInt(5))}<"

        mc.thePlayer?.sendChatMessage(message)
    }

    private val r18Text = arrayOf(
            "笨蛋..轻一点..唔..要..要去了..快..快停下♡",
            "啊～～轻一点啊～",
            "请温柔一点～要受不了了～～呜呜",
            "～～已经被哥哥～～玩的～要坏掉了",
            "啊～～哥哥～轻点，要高抄了～啊～～哥哥的都流出来了",
            "真是个涩鬼 又瑟进来这么多",
            "啊！好深"
    )

    private val pureText = arrayOf(
            "温柔扑了个空才长记性。",
            "你眉目有千秋偏无我。",
            "我这份悬在半空的喜欢可能飞不过万水千山了。",
            "说不尽的心酸我也只能当笑谈。",
            "已然无关痛痒，又怎骤然心酸。",
            "落日五湖游，烟波处处愁。浮沈千古事，谁与问东流。",
            "我喜欢你，不是情话，是心里话！",
            "我想对你做，春天在樱桃树上的事。",
            "我不会让你哭的，除非在床上。",
            "明明对哥哥的爱不掺水分，可是想你的时候，为什么总是湿湿的。"
    )

    private val nsxText = arrayOf(
        "$name 现在有没有入权了?",
        "给 $name 老Ma子上香^^",
        "你告诉我，是不是这个情况?",
        "SilenceFix is God in QuickMacro",
        "使用Skyrim但是没有脑子的人是 $name 吗",
        "你为什么翻来覆去的",
        "七上八下侮辱 $name 了",
        "$name 是漏防哥吗?",
        "咋了 $name ,要来偷刀?",
        "是不是",
        "对不对",
        "cnm",
        "$name 是不是翻来覆去的",
        "$name 是不是脑残了",
        "我DXG一天圈十万，$name 羡慕死了吧"
    )

    private fun getR18Text(): String {
        return r18Text[RandomUtils.nextInt(0, r18Text.size - 1)]
    }

    private fun getPureText(): String {
        return pureText[RandomUtils.nextInt(0, pureText.size - 1)]
    }

    private fun getNSXText(): String {
        return nsxText[RandomUtils.nextInt(0, nsxText.size - 1)]
    }

    init {
        state = true
    }
}