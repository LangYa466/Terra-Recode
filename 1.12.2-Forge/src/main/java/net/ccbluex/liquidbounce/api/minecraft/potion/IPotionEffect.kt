/*
 * Terra Hacked Client
 */

package net.ccbluex.liquidbounce.api.minecraft.potion

interface IPotionEffect {
    fun getDurationString(): String

    val amplifier: Int
    val duration: Int
    val potionID: Int
}