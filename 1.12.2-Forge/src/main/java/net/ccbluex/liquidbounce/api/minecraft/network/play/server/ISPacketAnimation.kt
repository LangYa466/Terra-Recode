/*
 * Terra Hacked Client
 */

package net.ccbluex.liquidbounce.api.minecraft.network.play.server

import net.ccbluex.liquidbounce.api.minecraft.network.IPacket

interface ISPacketAnimation : IPacket {
    val animationType: Int
    val entityID: Int
}