package ch.epfl.planair.visual;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.function.IntUnaryOperator;

public final class HueThreshold extends PApplet {

	private HScrollBar thresholdBar1, thresholdBar2;
	private PImage img;

	public void setup() {
		size(800, 600);
		img = loadImage("board/board1.jpg");
		thresholdBar1 = new HScrollBar(this, 0, 580, 800, 20);
		thresholdBar2 = new HScrollBar(this, 0, 550, 800, 20);
		//noLoop();
		frameRate(60);
	}

	public void draw() {
		background(color(0, 0, 0));
		thresholdBar1.update();
		thresholdBar2.update();

		int firstThreshold = (int) (255 * min(thresholdBar1.getPos(), thresholdBar2.getPos()));
		int secondThreshold = (int) (255 * max(thresholdBar1.getPos(), thresholdBar2.getPos()));
		PImage result = thresholding(img, hueThreshold(firstThreshold, secondThreshold, color(0)));

		image(result, 0, 0);
		thresholdBar1.display();
		thresholdBar2.display();
	}

	public IntUnaryOperator hueThreshold(int firstThreshold, int secondThreshold, int other) {
		return v -> firstThreshold <= hue(v) && hue(v) <= secondThreshold ? color(hue(v)) : other;
	}

	public PImage thresholding(PImage source, IntUnaryOperator op) {
		PImage result = createImage(source.width, source.height, RGB);
		for (int i = 0; i < result.width * result.height; ++i) {
			result.pixels[i] = op.applyAsInt(source.pixels[i]);
		}
		return result;
	}

}