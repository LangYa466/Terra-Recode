/*
 * Terra Hacked Client
 */

package net.ccbluex.liquidbounce.api

import net.ccbluex.liquidbounce.api.minecraft.client.IMinecraft

interface Wrapper {
    val classProvider: IClassProvider
    val minecraft: IMinecraft
    val functions: IExtractedFunctions


}