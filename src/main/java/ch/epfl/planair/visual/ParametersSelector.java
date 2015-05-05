package ch.epfl.planair.visual;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.util.List;

public final class ParametersSelector extends PApplet {

	private HScrollBar thresholdBar1, thresholdBar2;
	private PImage image;
	private Pipeline pipeline;

	@Override
	public void setup() {
		size(800, 600);
		image = loadImage("board/board1.jpg");
		thresholdBar1 = new HScrollBar(this, 0, 580, 800, 20);
		thresholdBar2 = new HScrollBar(this, 0, 550, 800, 20);
		frameRate(30);
		pipeline = new Pipeline(this);
	}

	@Override
	public void draw() {
		background(color(0, 0, 0));
		thresholdBar1.update();
		thresholdBar2.update();
		int firstThreshold = (int) (255 * min(thresholdBar1.getPos(), thresholdBar2.getPos()));
		int secondThreshold = (int) (255 * max(thresholdBar1.getPos(), thresholdBar2.getPos()));

		PImage result = image;
		result = pipeline.selectHueThreshold(result, 80, 125, 0);
		result = pipeline.selectBrightnessThreshold(result, 30, 255, 0);
		result = pipeline.selectSaturationThreshold(result, 80, 255, 0);
		result = pipeline.convolute(result, Pipeline.gaussianKernel);
		result = pipeline.sobel(result, 0.35f);
		pipeline.debugPlotLine(result, pipeline.hough(result));
		//println("first: "+firstThreshold+"\tsecond: "+secondThreshold);

		image(result, 0, 0);
		thresholdBar1.display();
		thresholdBar2.display();
	}

}