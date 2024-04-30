package net.ccbluex.liquidbounce.utils;

import net.minecraft.util.ResourceLocation;

import java.util.Random;

public class RandomImgUtils {
    static Random random = new Random();
    static String[] list = {
            "bg1.png",
            "bg2.png",
            "bg3.png",
            "bg5.png",
            "bg6.png",
            "bg7.png",
            "bg8.png",
            "bg9.png"
    };
    private static final String bg = list[random.nextInt(list.length)];

    static String[] list2 = {
            "splash.png"
    };
    private static final String splash = list2[random.nextInt(list2.length)];

    public static ResourceLocation getBackGround() {
        return new ResourceLocation("terra/background/" + bg);
    }

    public static ResourceLocation getSplash() {
        return new ResourceLocation("/assets/minecraft/terra/" + splash);
    }
}

