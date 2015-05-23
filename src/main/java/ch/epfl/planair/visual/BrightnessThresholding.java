package ch.epfl.planair.visual;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * A testing class, used to try out the brightness thresholding
 * part on a static Image.
 */
public final class BrightnessThresholding extends PApplet {

	private HScrollBar thresholdBar;
	private PImage image;
	private Pipeline pipeline;

	@Override
	public void setup() {
		size(800, 600);
		image = loadImage("board/board1.jpg");
		thresholdBar = new HScrollBar(this, 0, 580, 800, 20);
		frameRate(15);
		pipeline = new Pipeline(this);
	}

	@Override
	public void draw() {
		thresholdBar.update();
		int thresholdValue = (int) (thresholdBar.getPos() * 255);

		background(color(0, 0, 0));
		image(image, 0, 0);

		PImage result = pipeline.binaryBrightnessThreshold(image, thresholdValue, 0, 255);
		//PImage result = pipeline.inverseBinaryBrightnessThreshold(image, thresholdValue, 0, 255);
		//PImage result = pipeline.truncateBrightnessThreshold(image, thresholdValue);
		//PImage result = pipeline.toZeroBrightnessThreshold(image, thresholdValue, 0);
		//PImage result = pipeline.inverseToZeroBrightnessThreshold(image, thresholdValue);

		image(result, 0, 0);
		thresholdBar.display();
	}

}