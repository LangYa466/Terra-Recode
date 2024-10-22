/*
 * Terra Hacked Client
 */

package net.ccbluex.liquidbounce.injection.backend

import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketKeepAlive
import net.minecraft.network.play.client.CPacketKeepAlive

class CPacketKeepAliveImpl<T : CPacketKeepAlive>(wrapped: T) : PacketImpl<T>(wrapped), ICPacketKeepAlive {

}

inline fun ICPacketKeepAlive.unwrap(): CPacketKeepAlive = (this as CPacketKeepAliveImpl<*>).wrapped
inline fun CPacketKeepAlive.wrap(): ICPacketKeepAlive = CPacketKeepAliveImpl(this)