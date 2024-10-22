/*
 * Terra Hacked Client
 */

package net.ccbluex.liquidbounce.injection.backend

import net.ccbluex.liquidbounce.api.minecraft.client.renderer.IRenderGlobal
import net.minecraft.client.renderer.RenderGlobal

class RenderGlobalImpl(val wrapped: RenderGlobal) : IRenderGlobal {
    override fun loadRenderers() = wrapped.loadRenderers()

    override fun equals(other: Any?): Boolean {
        return other is RenderGlobalImpl && other.wrapped == this.wrapped
    }
}

inline fun IRenderGlobal.unwrap(): RenderGlobal = (this as RenderGlobalImpl).wrapped
inline fun RenderGlobal.wrap(): IRenderGlobal = RenderGlobalImpl(this)