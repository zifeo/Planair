package ch.epfl.planair.visual;

import java.util.function.IntUnaryOperator;

import ch.epfl.planair.config.Utils;
import processing.core.PApplet;
import processing.core.PImage;

public final class BrightnessThresholding extends PApplet {

	private HScrollBar thresholdBar;
	private PImage img;
	private Pipeline pipeline;

	@Override
	public void setup() {
		size(800, 600);
		img = loadImage("board/board1.jpg");
		thresholdBar = new HScrollBar(this, 0, 580, 800, 20);
		frameRate(60);
		pipeline = new Pipeline(this);
	}

	@Override
	public void draw() {
		background(color(0, 0, 0));
		thresholdBar.update();
		int thresholdValue = (int) (255 * thresholdBar.getPos());

		PImage result = pipeline.threshold(img, binaryThreshold(thresholdValue, 0, 255));
		//PImage result = pipeline.threshold(img, inverseBinaryThreshold(thresholdValue, 0, 255));
		//PImage result = pipeline.threshold(img, truncateThreshold(thresholdValue));
		//PImage result = pipeline.threshold(img, toZeroThreshold(thresholdValue, 0));
		//PImage result = pipeline.threshold(img, inverseToZeroThreshold(thresholdValue));

		image(result, 0, 0);
		thresholdBar.display();
	}

	/**
	 * A binary threshold based on brightness.
	 * If input reaches the limit, max color is set, otherwise min color.
	 *
	 * @param threshold brighness limit (0-255)
	 * @param minColor greyscale (0-255)
	 * @param maxColor greyscale (0-255)
	 * @throws IllegalArgumentException when min or max color are invalid
	 * @return
	 */
	public IntUnaryOperator binaryThreshold(int threshold, int minColor, int maxColor) {
		Utils.require(0, minColor, 255, "invalid grey color");
		Utils.require(0, maxColor, 255, "invalid grey color");
		return v -> brightness(v) > threshold ? color(maxColor): color(minColor);
	}

	public IntUnaryOperator inverseBinaryThreshold(int threshold, int minColor, int maxColor) {
		return binaryThreshold(threshold, maxColor, minColor);
	}

	public IntUnaryOperator truncateThreshold(int threshold) {
		Utils.require(0, threshold, 255, "invalid grey color");
		return v -> brightness(v) > threshold ? color(threshold): color(brightness(v));
	}

	public IntUnaryOperator toZeroThreshold(int threshold, int minColor) {
		Utils.require(0, threshold, 255, "invalid grey color");
		Utils.require(0, minColor, 255, "invalid grey color");
		return v -> brightness(v) > threshold ? color(brightness(v)): color(minColor);
	}

	public IntUnaryOperator inverseToZeroThreshold(int threshold) {
		Utils.require(0, threshold, 255, "invalid grey color");
		return v -> brightness(v) > threshold ? color(brightness(v)): color(threshold);
	}

}