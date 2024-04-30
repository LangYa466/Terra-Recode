package net.ccbluex.liquidbounce.features.module.modules.client

import me.qingyou.terra.sound.SoundPlayer
import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntityLivingBase
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EntityKilledEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.Block
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.SPacketSpawnGlobalEntity
import net.minecraft.util.EnumParticleTypes

@ModuleInfo("HitEffect","Shit",ModuleCategory.CLIENT)
class HitEffect : Module() {
    private val attackMod = ListValue("AttackMod", arrayOf("Fire","Blood","Heart","None"),"None")
    private val amount = IntegerValue("Amount",10,1,20).displayable { !attackMod.get().equals("None",true) }
    private val attackSoundMod = ListValue("AttackSound", arrayOf("Mouse","Dig","None"),"None")
    private val killMod = ListValue("KillMod", arrayOf("Flame","Lighting","None"),"Lighting")

    var target: IEntityLivingBase? = null

    @EventTarget
    fun onAttack(event: AttackEvent) {
        // 判断事件中的目标实体是否为IEntityLivingBase类型，如果是，将目标实体赋值给target变量
        if (event.targetEntity is IEntityLivingBase) target = event.targetEntity
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.isPre() && target != null && target!!.hurtTime >= 3 && mc.thePlayer!!.getDistance(target!!.posX, target!!.posY, target!!.posZ) < 6) {
            //Effect
            if (mc.thePlayer!!.ticksExisted > 3) {
                when (attackMod.get().toLowerCase()) {
                    "fire" -> {
                        for (i in 0..amount.get()) {
                            mc2.effectRenderer.spawnEffectParticle(EnumParticleTypes.LAVA.particleID, target!!.posX, target!!.posY, target!!.posZ, target!!.posX, target!!.posY, target!!.posZ)
                        }
                    }
                    "blood" -> {
                        for (i in 0..amount.get()) {
                            mc2.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, target!!.posX, target!!.posY + target!!.height - 0.75, target!!.posZ, 0.0, 0.0, 0.0, Block.getStateId(
                                Blocks.REDSTONE_BLOCK.defaultState))
                        }
                    }
                    "heart" -> {
                        mc2.effectRenderer.spawnEffectParticle(EnumParticleTypes.HEART.particleID, target!!.posX, target!!.posY, target!!.posZ, target!!.posX, target!!.posY, target!!.posZ)
                    }
                }
            }
            //Sound
            if (mc.thePlayer!!.ticksExisted > 4) {
                when (attackSoundMod.get().toLowerCase()) {
                    "mouse" -> SoundPlayer().playSound(SoundPlayer.SoundType.MOUSE, Terra.moduleManager.toggleVolume)
                    "dig" -> mc.soundHandler.playSound("minecraft:block.anvil.break", 1f)
                }
            }
        }
        target = null
    }

    @EventTarget
    fun onEntityKilled(event: EntityKilledEvent) {
        val entity = event.targetEntity!!.unwrap()
        when (killMod.get().toLowerCase()) {
            "flame" -> {
                for (i in 0..10) {
                    mc2.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.FLAME)
                }
                mc.soundHandler.playSound("item.fireCharge.use", 0.5f)
            }
            "lighting" -> {
                mc.netHandler2.handleSpawnGlobalEntity(SPacketSpawnGlobalEntity(EntityLightningBolt(mc2.world, entity.posX, entity.posY, entity.posZ, true)))
                mc.soundHandler.playSound("entity.lightning.impact", 0.5f)
            }
        }

    }

}