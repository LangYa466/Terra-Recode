package net.ccbluex.liquidbounce.utils.misc

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.features.special.Recorder

object Transformer {
    fun transform(string: String): String {
        var text = string
        text = text.replace("%L%", "Ｌ")
        text = text.replace("%l%", "Ｌ")
        text = text.replace("%cname%", Terra.CLIENT_NAME)
        text = text.replace("%cversion%", Terra.CLIENT_VERSION.toString())
        text = text.replace("%cdev%", Terra.CLIENT_CREATOR)
        text = text.replace("%cdev2%", Terra.CLIENT_CREATOR.replace("F", "Ｆ"))
        text = text.replace("%kill%", Recorder.killCounts.toString())
        text = text.replace("%win%", Recorder.win.toString())
        text = text.replace("%total%", Recorder.totalPlayed.toString())

        return text
    }

    fun getPlayTime(spaceBetweenWords: Boolean): String {
        return if (spaceBetweenWords) {
            "${Recorder.hour} 时 ${Recorder.minute} 分 ${Recorder.second} 秒"
        } else {
            "${Recorder.hour}时${Recorder.minute}分${Recorder.second}秒"
        }
    }

}