package net.ccbluex.liquidbounce.utils.misc

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.features.module.modules.exploit.Disabler
import net.ccbluex.liquidbounce.utils.MathUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.client.Minecraft
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.CPacketConfirmTransaction

object PacketUtils : MinecraftInstance() {
    private val packets = ArrayList<Packet<INetHandlerPlayServer>>()
    private var disabler = Terra.moduleManager.getModule(Disabler::class.java)

    @JvmStatic
    fun send(packet: Packet<*>?) {
        if (mc.thePlayer != null) {
            Minecraft.getMinecraft().connection!!.sendPacket(packet)
        }
    }

    @JvmStatic
    fun sendPacketC0F() {
        if (!disabler.getGrimPost()) {
            send(
                CPacketConfirmTransaction(
                    MathUtils.getRandom(102, 1000024123),
                    MathUtils.getRandom(102, 1000024123).toShort(), true
                ) as Packet<*>?
            )
        }
    }

    @JvmStatic
    fun sendPacketNoEvent(packet: Packet<INetHandlerPlayServer>) {
        packets.add(packet)
        mc2.connection!!.sendPacket(packet)
    }

    @JvmStatic
    fun getPacketType(packet: Packet<*>): PacketType {
        val className = packet.javaClass.simpleName
        if (className.startsWith("C", ignoreCase = true)) {
            return PacketType.CLIENTSIDE
        } else if (className.startsWith("S", ignoreCase = true)) {
            return PacketType.SERVERSIDE
        }
        return PacketType.UNKNOWN
    }

    enum class PacketType {
        SERVERSIDE,
        CLIENTSIDE,
        UNKNOWN
    }
}