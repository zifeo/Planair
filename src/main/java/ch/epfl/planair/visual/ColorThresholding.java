package ch.epfl.planair.visual;

import ch.epfl.planair.config.Utils;
import processing.core.PApplet;
import processing.core.PImage;

import java.util.function.IntUnaryOperator;

public final class ColorThresholding extends PApplet {

	private HScrollBar thresholdBar1, thresholdBar2;
	private PImage img;
	private Pipeline pipeline;

	@Override
	public void setup() {
		size(800, 600);
		img = loadImage("board/board1.jpg");
		thresholdBar1 = new HScrollBar(this, 0, 580, 800, 20);
		thresholdBar2 = new HScrollBar(this, 0, 550, 800, 20);
		frameRate(60);
		pipeline = new Pipeline(this);
	}

	@Override
	public void draw() {
		background(color(0, 0, 0));
		thresholdBar1.update();
		thresholdBar2.update();
		int firstThreshold = (int) (255 * min(thresholdBar1.getPos(), thresholdBar2.getPos()));
		int secondThreshold = (int) (255 * max(thresholdBar1.getPos(), thresholdBar2.getPos()));

		PImage result = pipeline.threshold(img, hueThreshold(firstThreshold, secondThreshold, 0));

		image(result, 0, 0);
		thresholdBar1.display();
		thresholdBar2.display();
	}

	public IntUnaryOperator hueThreshold(int firstThreshold, int secondThreshold, int otherColor) {
		Utils.require(0, otherColor, 255, "invalid grey color");
		return v -> firstThreshold <= hue(v) && hue(v) <= secondThreshold ? color(hue(v)) : color(otherColor);
	}

}