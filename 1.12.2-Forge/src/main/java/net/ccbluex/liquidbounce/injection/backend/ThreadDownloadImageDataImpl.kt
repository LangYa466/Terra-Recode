/*
 * Terra Hacked Client
 */

package net.ccbluex.liquidbounce.injection.backend

import net.ccbluex.liquidbounce.api.minecraft.client.render.IThreadDownloadImageData
import net.minecraft.client.renderer.ThreadDownloadImageData

class ThreadDownloadImageDataImpl<T : ThreadDownloadImageData>(wrapped: T) : AbstractTextureImpl<T>(wrapped), IThreadDownloadImageData {
    override fun equals(other: Any?): Boolean {
        return other is ThreadDownloadImageDataImpl<*> && other.wrapped == this.wrapped
    }
}

inline fun IThreadDownloadImageData.unwrap(): ThreadDownloadImageData = (this as ThreadDownloadImageDataImpl<*>).wrapped
inline fun ThreadDownloadImageData.wrap(): IThreadDownloadImageData = ThreadDownloadImageDataImpl(this)