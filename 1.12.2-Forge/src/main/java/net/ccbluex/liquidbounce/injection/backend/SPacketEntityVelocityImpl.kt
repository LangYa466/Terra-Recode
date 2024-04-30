/*
 * Terra Hacked Client
 */

package net.ccbluex.liquidbounce.injection.backend

import net.ccbluex.liquidbounce.api.minecraft.network.play.server.ISPacketEntityVelocity
import net.minecraft.network.play.server.SPacketEntityVelocity

class SPacketEntityVelocityImpl<T : SPacketEntityVelocity>(wrapped: T) : PacketImpl<T>(wrapped), ISPacketEntityVelocity {
    override var motionX: Int
        get() = wrapped.motionX
        set(value) {
            wrapped.motionX = value
        }
    override var motionY: Int
        get() = wrapped.motionY
        set(value) {
            wrapped.motionY = value
        }
    override var motionZ: Int
        get() = wrapped.motionZ
        set(value) {
            wrapped.motionZ = value
        }
    override val entityID: Int
        get() = wrapped.entityID

}

inline fun ISPacketEntityVelocity.unwrap(): SPacketEntityVelocity = (this as SPacketEntityVelocityImpl<*>).wrapped
inline fun SPacketEntityVelocity.wrap(): ISPacketEntityVelocity = SPacketEntityVelocityImpl(this)