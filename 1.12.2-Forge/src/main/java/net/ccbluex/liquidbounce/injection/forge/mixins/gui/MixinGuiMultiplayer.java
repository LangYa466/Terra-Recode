/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.Terra;
import net.ccbluex.liquidbounce.features.special.BungeeCordSpoof;
import net.ccbluex.liquidbounce.injection.backend.GuiScreenImplKt;
import net.ccbluex.liquidbounce.ui.client.GuiAntiForge;
import net.ccbluex.liquidbounce.ui.client.tools.GuiTools;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMultiplayer.class)
public abstract class MixinGuiMultiplayer extends MixinGuiScreen {

    private GuiButton bungeeCordSpoofButton;

    @Inject(method = "initGui", at = @At("RETURN"))
    private void initGui(CallbackInfo callbackInfo) {
        buttonList.add(new GuiButton(997, 5, 8, 98, 20, "AntiForge"));
        buttonList.add(bungeeCordSpoofButton = new GuiButton(998, 108, 8, 98, 20, "BungeeCord Spoof: " + (BungeeCordSpoof.enabled ? "On" : "Off")));
        buttonList.add(new GuiButton(999, width - 104, 8, 98, 20, "Tools"));
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void actionPerformed(GuiButton button, CallbackInfo callbackInfo) {
        switch (button.id) {
            case 997:
                mc.displayGuiScreen(GuiScreenImplKt.unwrap(Terra.wrapper.getClassProvider().wrapGuiScreen(new GuiAntiForge(GuiScreenImplKt.wrap((GuiScreen) (Object) this)))));
                break;
            case 998:
                BungeeCordSpoof.enabled = !BungeeCordSpoof.enabled;
                bungeeCordSpoofButton.displayString = "BungeeCord Spoof: " + (BungeeCordSpoof.enabled ? "On" : "Off");
                Terra.fileManager.saveConfig(Terra.fileManager.valuesConfig);
                break;
            case 999:
                mc.displayGuiScreen(GuiScreenImplKt.unwrap(Terra.wrapper.getClassProvider().wrapGuiScreen(new GuiTools(GuiScreenImplKt.wrap((GuiScreen) (Object) this)))));
                break;
        }
    }
}