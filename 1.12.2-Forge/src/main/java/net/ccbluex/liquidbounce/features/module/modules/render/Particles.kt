/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntityLivingBase
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.particles.EvictingList
import net.ccbluex.liquidbounce.utils.particles.Particle
import net.ccbluex.liquidbounce.utils.particles.ParticleTimer
import net.ccbluex.liquidbounce.utils.particles.Vec3
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import java.util.function.Consumer

@ModuleInfo(name = "Particles", description = "Beautiful", category = ModuleCategory.RENDER)
class Particles : Module() {
    private val amount = IntegerValue("Amount", 10, 1, 20)
    private val physics = BoolValue("Physics", true)
    private val particles: MutableList<Particle> = EvictingList(100)
    private val timer = ParticleTimer()
    private var target: IEntityLivingBase? = null

    @EventTarget
    fun onAttack(event: AttackEvent) {
        target = event.targetEntity!!.asEntityLivingBase()
    }

    @EventTarget
    fun onMotion(event: MotionEvent?) {
        if (target != null && target!!.hurtTime >= 9 && mc2.player.getDistance(
                target!!.posX,
                target!!.posY,
                target!!.posZ
            ) < 10
        ) {
            for (i in 0 until amount.get()) particles.add(
                Particle(
                    Vec3(
                        target!!.posX + (Math.random() - 0.5) * 0.5,
                        target!!.posY + Math.random() * 1 + 0.5,
                        target!!.posZ + (Math.random() - 0.5) * 0.5
                    )
                )
            )
            target = null
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        if (particles.isEmpty()) return
        var i = 0
        while (i <= timer.elapsedTime / 1E+11) {
            if (physics.get()) particles.forEach(Consumer { obj: Particle -> obj.update() }) else particles.forEach(
                Consumer { obj: Particle -> obj.updateWithoutPhysics() })
            i++
        }
        particles.removeIf { particle: Particle ->
            mc2.player.getDistanceSq(
                particle.position.xCoord,
                particle.position.yCoord,
                particle.position.zCoord
            ) > 50 * 10
        }
        timer.reset()
        RenderUtils.renderParticles(particles)
    }
}