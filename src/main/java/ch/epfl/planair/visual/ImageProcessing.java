package ch.epfl.planair.visual;

import processing.core.PApplet;
import processing.core.PImage;

public final class ImageProcessing extends PApplet {

	@Override
	public void setup() {
		size(800, 600);
		noLoop();
	}

	@Override
	public void draw() {

		PImage image = loadImage("board/board1.jpg");
		Pipeline pipeline = new Pipeline(this);

		PImage result = image;

		result = pipeline.selectHueThreshold(result, 80, 125, 0);
		result = pipeline.selectBrightnessThreshold(result, 30, 255, 0);
		result = pipeline.selectSaturationThreshold(result, 80, 255, 0);
		result = pipeline.convolute(result, Pipeline.gaussianKernel);
		result = pipeline.sobel(result, 0.35f);
		pipeline.debugPlotLine(result, pipeline.hough(result));

		image(result, 0, 0);
	}

}
