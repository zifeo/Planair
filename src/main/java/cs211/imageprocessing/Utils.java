package cs211.imageprocessing;

/**
 * Modified version of our Utils class for assignment 3.
 */
public final class Utils {

    private Utils() {}

	public static void require(boolean predicat, String message) {
		if (!predicat) throw new IllegalArgumentException(message);
	}

    public static void require(double min, double value, double max, String message) {
        require(min <= value && value <= max, message);
    }

}