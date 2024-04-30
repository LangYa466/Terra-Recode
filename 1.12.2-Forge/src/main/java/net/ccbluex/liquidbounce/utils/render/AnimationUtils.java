/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.utils.render;

public class AnimationUtils {
    public static float lstransition(float now, float desired, double speed) {
        final double dif = Math.abs(desired - now);
        float a = (float) Math.abs((desired - (desired - (Math.abs(desired - now)))) / (100 - (speed * 10)));
        float x = now;

        if (dif > 0) {
            if (now < desired)
                x += a * RenderUtils.deltaTime;
            else if (now > desired)
                x -= a * RenderUtils.deltaTime;
        } else
            x = desired;

        if (Math.abs(desired - x) < 10.0E-3 && x != desired)
            x = desired;

        return x;
    }

    /**
     * In-out-easing function
     * https://github.com/jesusgollonet/processing-penner-easing
     * @param t Current iteration
     * @param d Total iterations
     * @return Eased value
     */
    public static float easeOut(float t, float d) {
        return (t = t / d - 1) * t * t + 1;
    }

    public static float animate(float target, float current, float speed) {
        if (current == target) return current;

        boolean larger = target > current;
        if (speed < 0.0F) {
            speed = 0.0F;
        } else if (speed > 1.0F) {
            speed = 1.0F;
        }

        double dif = Math.max(target, (double) current) - Math.min(target, (double) current);
        double factor = dif * (double) speed;
        if (factor < 0.1D) {
            factor = 0.1D;
        }

        if (larger) {
            current += (float) factor;
            if (current >= target) current = target;
        } else if (target < current) {
            current -= (float) factor;
            if (current <= target) current = target;
        }

        return current;
    }
}
