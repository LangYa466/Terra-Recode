package net.ccbluex.liquidbounce.injection.forge.mixins.util;

import net.ccbluex.liquidbounce.Terra;
import net.ccbluex.liquidbounce.event.MoveInputEvent;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MovementInputFromOptions.class)
public class MixinMovementInputFromOptions extends MovementInput {

    @Final
    @Shadow
    private GameSettings gameSettings;

    @Inject(method = "updatePlayerMoveState", at = @At("HEAD"), cancellable = true)
    public void updatePlayerMoveState(CallbackInfo callbackInfo) {
        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        if (this.gameSettings.keyBindForward.isKeyDown()) {
            ++this.moveForward;
            this.forwardKeyDown = true;
        } else {
            this.forwardKeyDown = false;
        }

        if (this.gameSettings.keyBindBack.isKeyDown()) {
            --this.moveForward;
            this.backKeyDown = true;
        } else {
            this.backKeyDown = false;
        }

        if (this.gameSettings.keyBindLeft.isKeyDown()) {
            ++this.moveStrafe;
            this.leftKeyDown = true;
        } else {
            this.leftKeyDown = false;
        }

        if (this.gameSettings.keyBindRight.isKeyDown()) {
            --this.moveStrafe;
            this.rightKeyDown = true;
        } else {
            this.rightKeyDown = false;
        }

        this.jump = this.gameSettings.keyBindJump.isKeyDown();
        this.sneak = this.gameSettings.keyBindSneak.isKeyDown();

        final MoveInputEvent event = new MoveInputEvent(moveForward, moveStrafe, jump, sneak, 0.3D);
        Terra.eventManager.callEvent(event);

        final double sneakMultiplier = event.getSneakSlowDownMultiplier();

        this.moveForward = event.getForward();
        this.moveStrafe = event.getStrafe();
        this.jump = event.getJump();
        this.sneak = event.getSneak();

        if (this.sneak) {
            this.moveStrafe = (float)((double)this.moveStrafe * sneakMultiplier);
            this.moveForward = (float)((double)this.moveForward *sneakMultiplier);
        }

        callbackInfo.cancel();
    }
}
