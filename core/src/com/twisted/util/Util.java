package com.twisted.util;

public class Util {

    /**
     * Calculates the hyperbolic arc sin of x.
     */
    public static double asinh(double x){
        return Math.log(x + Math.sqrt(x*x + 1.0));
    }

    /**
     * Converts logical (serverside) radians to visual degrees (for libgdx rendering).
     */
    public static float logicalRadToVisualDeg(float radians){
        return 180 * radians/(float)Math.PI - 90;
    }

}
