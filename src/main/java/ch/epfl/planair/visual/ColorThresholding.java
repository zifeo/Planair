package ch.epfl.planair.visual;

import processing.core.PApplet;
import processing.core.PImage;

public final class ColorThresholding extends PApplet {

	private HScrollBar thresholdBar1, thresholdBar2;
	private PImage image;
	private Pipeline pipeline;

	@Override
	public void setup() {
		size(800, 600);
		image = loadImage("board/board3.jpg");
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

		println(thresholdBar1.getPos()+" : "+thresholdBar2.getPos());
		PImage result = pipeline.selectHueThreshold(image, firstThreshold, secondThreshold, 0);

		image(result, 0, 0);
		thresholdBar1.display();
		thresholdBar2.display();
	}

}