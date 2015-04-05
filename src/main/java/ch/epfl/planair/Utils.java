package ch.epfl.planair;

/**
 * Created by Nicolas on 04.04.15.
 */
public final class Utils {

    private Utils() {}

    static public float trim(float value, float min, float max) {
        return value > max ? max : value < min ? min : value;
    }

    static public float trim(float value, float bound) {
        return trim(value, -bound, bound);
    }
}
