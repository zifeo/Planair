package ch.epfl.planair;

import processing.core.PVector;

public final class Utils {

    private Utils() {}

    public static float trim(float value, float min, float max) {
        return value > max ? max : value < min ? min : value;
    }

    public static float trim(float value, float bound) {
        return trim(value, -bound, bound);
    }


}