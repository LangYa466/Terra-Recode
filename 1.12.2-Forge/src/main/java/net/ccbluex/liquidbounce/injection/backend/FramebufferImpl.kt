/*
 * Terra Hacked Client
 */

package net.ccbluex.liquidbounce.injection.backend

import net.ccbluex.liquidbounce.api.minecraft.client.shader.IFramebuffer
import net.minecraft.client.shader.Framebuffer

class FramebufferImpl(val wrapped: Framebuffer) : IFramebuffer {
    override fun bindFramebuffer(b: Boolean) = wrapped.bindFramebuffer(b)

    override fun equals(other: Any?): Boolean {
        return other is FramebufferImpl && other.wrapped == this.wrapped
    }
}

inline fun IFramebuffer.unwrap(): Framebuffer = (this as FramebufferImpl).wrapped
inline fun Framebuffer.wrap(): IFramebuffer = FramebufferImpl(this)