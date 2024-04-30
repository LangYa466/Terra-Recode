/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.Terra;
import net.ccbluex.liquidbounce.api.IClassProvider;
import net.ccbluex.liquidbounce.api.IExtractedFunctions;
import net.ccbluex.liquidbounce.api.minecraft.client.IMinecraft;
import net.minecraft.client.Minecraft;

public class MinecraftInstance {
    public static final IMinecraft mc = Terra.wrapper.getMinecraft();
    public static final Minecraft mc2 = Minecraft.getMinecraft();
    public static final IClassProvider classProvider = Terra.INSTANCE.getWrapper().getClassProvider();
    public static final IExtractedFunctions functions = Terra.INSTANCE.getWrapper().getFunctions();
}
