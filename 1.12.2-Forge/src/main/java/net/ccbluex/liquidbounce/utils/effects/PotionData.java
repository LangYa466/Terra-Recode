package net.ccbluex.liquidbounce.utils.effects;

import net.ccbluex.liquidbounce.api.minecraft.potion.IPotion;

public class PotionData {
    public final IPotion potion;
    public final Translate translate;
    public final int level;
    public int maxTimer = 0;
    public float animationX = 0;

    public PotionData(IPotion potion, Translate translate, int level) {
        this.potion = potion;
        this.translate = translate;
        this.level = level;
    }

    public float getAnimationX() {
        return animationX;
    }

    public IPotion getPotion() {
        return potion;
    }

    public int getMaxTimer() {
        return maxTimer;
    }
}