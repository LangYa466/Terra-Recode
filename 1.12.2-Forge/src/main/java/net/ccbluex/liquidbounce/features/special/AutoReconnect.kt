/*
 * Terra Hacked Client
 */

package net.ccbluex.liquidbounce.features.special

object AutoReconnect {
    const val MAX = 60000
    const val MIN = 1000

    var isEnabled = true
        private set
    var delay = 5000
        set(value) {
            isEnabled = value < MAX

            field = value
        }
}