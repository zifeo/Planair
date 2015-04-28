package ch.epfl.planair.visual;

import java.util.function.IntUnaryOperator;

import ch.epfl.planair.config.Utils;
import processing.core.PApplet;
import processing.core.PImage;

public final class BrightnessThresholding extends PApplet {

	private HScrollBar thresholdBar;
	private PImage img;

	@Override
	public void setup() {
		size(800, 600);
		img = loadImage("board/board1.jpg");
		thresholdBar = new HScrollBar(this, 0, 580, 800, 20);
		frameRate(60);
	}

	@Override
	public void draw() {
		background(color(0, 0, 0));
		thresholdBar.update();

		int thresholdValue = (int) (255 * thresholdBar.getPos());

		PImage result = thresholding(img, binaryThreshold(thresholdValue, 0, 255));
		//PImage result = thresholding(img, inverseBinaryThreshold(thresholdValue, 0, 255));
		//PImage result = thresholding(img, truncateThreshold(thresholdValue));
		//PImage result = thresholding(img, toZeroThreshold(thresholdValue, 0));
		//PImage result = thresholding(img, inverseToZeroThreshold(thresholdValue));

		image(result, 0, 0);
		thresholdBar.display();
	}

	/**
	 * A binary threshold based on brightness.
	 * If input reaches the limit, max color is set, otherwise min color.
	 *
	 * @param threshold brighness limit (0-255)
	 * @param min grey color (0-255)
	 * @param max grey color (0-255)
	 * @throws IllegalArgumentException when min or max color are invalid
	 * @return
	 */
	private IntUnaryOperator binaryThreshold(int threshold, int min, int max) {
		Utils.require(0, min, 255, "invalid grey color");
		Utils.require(0, max, 255, "invalid grey color");
		return v -> brightness(v) > threshold ? color(max): color(min);
	}

	private IntUnaryOperator inverseBinaryThreshold(int threshold, int min, int max) {
		return binaryThreshold(threshold, max, min);
	}

	private IntUnaryOperator truncateThreshold(int threshold) {
		Utils.require(0, threshold, 255, "invalid grey color");
		return v -> brightness(v) > threshold ? color(threshold): color(brightness(v));
	}

	private IntUnaryOperator toZeroThreshold(int threshold, int min) {
		Utils.require(0, threshold, 255, "invalid grey color");
		Utils.require(0, min, 255, "invalid grey color");
		return v -> brightness(v) > threshold ? color(brightness(v)): color(min);
	}

	private IntUnaryOperator inverseToZeroThreshold(int threshold) {
		Utils.require(0, threshold, 255, "invalid grey color");
		return v -> brightness(v) > threshold ? color(brightness(v)): color(threshold);
	}

	public PImage thresholding(PImage source, IntUnaryOperator op) {
		PImage result = createImage(source.width, source.height, RGB);
		for (int i = 0; i < result.width * result.height; ++i) {
			result.pixels[i] = op.applyAsInt(source.pixels[i]);
		}
		return result;
	}

}