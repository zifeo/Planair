package ch.epfl.planair.config;

import processing.core.PVector;

public final class Utils {

    private Utils() {}

    public static float trim(float value, float min, float max) { return value > max ? max : value < min ? min : value; }

    public static float trim(float value, float bound) { return trim(value, -bound, bound); }

    public static PVector nullVector() { return new PVector(0, 0, 0); }

    public static PVector maxVector() { return new PVector(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE); }

    public static PVector minVector() { return new PVector(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY); }

	public static void require(boolean predicat, String message) {
		if (!predicat) throw new IllegalArgumentException(message);
	}

    public static void require(double min, double value, double max, String message) {
        require(in(min, value, max), message);
    }

    public static boolean in(double low, double value, double high) {
        return low <= value && value <= high;
    }

}