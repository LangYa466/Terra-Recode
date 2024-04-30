package me.qingyou.terra.sound;

import net.ccbluex.liquidbounce.Terra;
import net.minecraft.client.Minecraft;

public class Sound {
    public static final Minecraft mc = Minecraft.getMinecraft();
    public static Sound INSTANCE;
    public Sound() {
        new SoundPlayer().playSound(SoundPlayer.SoundType.BEGIN, Terra.moduleManager.getToggleVolume());
    }
}

