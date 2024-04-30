/*
 * Terra Hacked Client
 */

package net.ccbluex.liquidbounce.api.minecraft.potion

interface IPotion {
    val liquidColor: Int
    val id: Int
    val name: String
    val hasStatusIcon: Boolean
    val statusIconIndex: Int
}