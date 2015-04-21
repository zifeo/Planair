package ch.epfl.planair.visual;

import java.util.function.IntUnaryOperator;
import processing.core.PApplet;
import processing.core.PImage;

public final class BrightnessThreshold extends PApplet {

	private HScrollBar thresholdBar;
	private PImage img;

	public void setup() {
		size(800, 600);
		img = loadImage("board/board1.jpg");
		thresholdBar = new HScrollBar(this, 0, 580, 800, 20);
		//noLoop();
		frameRate(60);
	}

	public void draw() {
		background(color(0, 0, 0));
		thresholdBar.update();

		int thresholdValue = (int) (255 * thresholdBar.getPos());

		// binaryThreshold or inverseBinaryThreshold
		PImage result = thresholding(img, binaryThreshold(thresholdValue, color(0), color(255)));

		image(result, 0, 0);
		thresholdBar.display();
	}

	public IntUnaryOperator binaryThreshold(int threshold, int min, int max) {
		return v -> brightness(v) > threshold ? max: min;
	}

	public IntUnaryOperator inverseBinaryThreshold(int threshold, int min, int max) {
		return binaryThreshold(threshold, max, min);
	}

	public PImage thresholding(PImage source, IntUnaryOperator op) {
		PImage result = createImage(source.width, source.height, RGB);
		for (int i = 0; i < result.width * result.height; ++i) {
			result.pixels[i] = op.applyAsInt(source.pixels[i]);
		}
		return result;
	}

}