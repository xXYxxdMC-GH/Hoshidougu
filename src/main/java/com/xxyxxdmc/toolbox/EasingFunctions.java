package main.java.com.xxyxxdmc.toolbox;

public class EasingFunctions {

    public static double easeLinear(double time) {
        return time;
    }

    public static double easeInQuad(double time) {
        return time * time;
    }

    public static double easeOutQuad(double time) {
        return time * (2 - time);
    }

    public static double easeInOutQuad(double time) {
        return (time < 0.5) ? 2 * time * time : -1 + (4 - 2 * time) * time;
    }

    public static double easeInCubic(double time) {
        return time * time * time;
    }

    public static double easeOutCubic(double time) {
        return (--time) * time * time + 1;
    }

    public static double easeInOutCubic(double time) {
        return (time < 0.5) ? 4 * time * time * time : (time - 1) * (2 * time - 2) * (2 * time - 2) + 1;
    }

    public static double easeInSine(double time) {
        return 1 - Math.cos(time * (Math.PI / 2));
    }

    public static double easeOutSine(double time) {
        return Math.sin(time * (Math.PI / 2));
    }

    public static double easeInOutSine(double time) {
        return 0.5 * (1 - Math.cos(Math.PI * time));
    }

    public static double easeInExpo(double time) {
        return (time == 0) ? 0 : Math.pow(2, 10 * (time - 1));
    }

    public static double easeOutExpo(double time) {
        return (time == 1) ? 1 : 1 - Math.pow(2, -10 * time);
    }

    public static double easeInOutExpo(double time) {
        if (time == 0) return 0;
        if (time == 1) return 1;
        return (time < 0.5) ? 0.5 * Math.pow(2, 20 * time - 10) : 1 - 0.5 * Math.pow(2, -20 * time + 10);
    }

    public static double easeX2(double time) {
        return -Math.pow(time-1, 2)+1;
    }
}


