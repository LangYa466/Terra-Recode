package net.ccbluex.liquidbounce.features.special

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntityLivingBase
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.timer.MSTimer

class CombatManager : Listenable, MinecraftInstance() {
    private val lastAttackTimer = MSTimer()

    var inCombat = false
    var target: IEntityLivingBase? = null
        private set
    private val attackedEntityList = mutableListOf<IEntityLivingBase>()


    @EventTarget
    fun onAttack(event: AttackEvent) {
        val target = event.targetEntity

        if ((target is IEntityLivingBase) && EntityUtils.isSelected(target, true)) {
            this.target = target
            if (!attackedEntityList.contains(target)) {
                attackedEntityList.add(target)
            }
        }
        lastAttackTimer.reset()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc2.player.isDead) attackedEntityList.clear()
        attackedEntityList.filter { it.isDead }.forEach {
            Recorder.killCounts++
            Terra.eventManager.callEvent(EntityKilledEvent(it))
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

    override fun handleEvents() = true
}