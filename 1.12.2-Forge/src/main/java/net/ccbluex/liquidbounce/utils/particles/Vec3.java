/*
 * Terra Hacked Client
 */

package net.ccbluex.liquidbounce.utils.particles;

public class Vec3 {

    /**
     * X coordinate of Vec3D
     */
    public double xCoord;

    /**
     * Y coordinate of Vec3D
     */
    public double yCoord;

    /**
     * Z coordinate of Vec3D
     */
    public double zCoord;

    public Vec3(double x, double y, double z) {
        if (x == -0.0D) {
            x = 0.0D;
        }

        if (y == -0.0D) {
            y = 0.0D;
        }

        if (z == -0.0D) {
            z = 0.0D;
        }

        this.xCoord = x;
        this.yCoord = y;
        this.zCoord = z;
    }

    public String toString() {
        return "(" + this.xCoord + ", " + this.yCoord + ", " + this.zCoord + ")";
    }

}
