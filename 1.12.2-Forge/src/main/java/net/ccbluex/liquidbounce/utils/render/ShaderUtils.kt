package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.utils.render.shader.shaders.RoundedRectShader
import java.awt.Color

/**
 * @author LangYa466
 * @date 2024/3/27 19:55
 */

object ShaderUtils {

    fun drawRoundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color) {
        RoundedRectShader.draw(x, y, width, height, radius, color)
    }

    fun drawRoundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Int) {
        RoundedRectShader.draw(x, y, width, height, radius, Color(color))
    }

    fun drawFilledCircle(x: Float, y: Float, radius: Float, color: Color) {
        RoundedRectShader.draw(x - radius, y - radius, x + radius, y + radius, radius, color)
    }
}