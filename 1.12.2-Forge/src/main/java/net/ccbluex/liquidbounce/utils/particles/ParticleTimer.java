/*
 * Terra Hacked Client
 */

package net.ccbluex.liquidbounce.utils.particles;

public class ParticleTimer {
    public long lastMS;

    private long getCurrentMS() {
        return System.nanoTime() / 1000000L;
    }

    public final long getElapsedTime() {
        return this.getCurrentMS() - this.lastMS;
    }

    public void reset() {
        this.lastMS = this.getCurrentMS();
    }

}

