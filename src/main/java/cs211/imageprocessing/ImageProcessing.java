package cs211.imageprocessing;

import ch.epfl.planair.visual.Pipeline;
import processing.core.PApplet;
import processing.core.PImage;

public final class ImageProcessing extends PApplet {

	private final String BOARD = "board1.jpg";
	private final int WIDTH = 800;
	private final int HEIGHT = 600;

	@Override
	public void setup() {
		size(WIDTH * 3, HEIGHT);
		noLoop();
	}

	@Override
	public void draw() {

		PImage image = loadImage("board/"+BOARD);
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
