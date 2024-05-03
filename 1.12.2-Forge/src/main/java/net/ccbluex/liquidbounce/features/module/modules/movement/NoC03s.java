package net.ccbluex.liquidbounce.features.module.modules.movement;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.MoveEvent;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.event.WorldEvent;
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.injection.backend.PacketImplKt;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketPlayer;

import java.util.LinkedList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

@ModuleInfo(name = "NoC03s", description = "OMG", category = ModuleCategory.MOVEMENT, autoDisable = EnumAutoDisableType.GAME_END)
public class NoC03s extends Module {
    private final BoolValue betterValue = new BoolValue("Better", false);
    private final LinkedList<CPacketConfirmTransaction> packets = new LinkedList<>();

    @EventTarget
    public void onMove(MoveEvent event) {
        event.zero();
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        final Packet<?> packet = PacketImplKt.unwrap(event.getPacket());

        if (packet instanceof CPacketPlayer) {
            event.cancelEvent();
        }

        if (packet instanceof CPacketConfirmTransaction && betterValue.get()) {
            packets.add((CPacketConfirmTransaction) packet);
            event.cancelEvent();
        }
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        this.toggle();
        for (CPacketConfirmTransaction transaction : packets) {
            mc2.getConnection().sendPacket(transaction);
        }
        packets.clear();
    }

    @Override
    public void onDisable() {
        for (CPacketConfirmTransaction transaction : packets) {
            mc2.getConnection().sendPacket(transaction);
        }
        packets.clear();
    }
}