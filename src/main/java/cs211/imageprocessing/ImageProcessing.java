package cs211.imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.util.List;

public final class ImageProcessing extends PApplet {

	private final static String BOARD = "board1.jpg";
	private final static int WIDTH = 800;
	private final static int HEIGHT = 600;
	private final static int CORNER_RADIUS = 20;
	private final static int ACCHEIGHT = (int) (Math.PI / Constants.PIPELINE_DISCRETIZATION_STEPS_PHI) + 2;
	private final static int ACCWIDTH = (int) (((WIDTH + HEIGHT) * 2 + 1) / Constants.PIPELINE_DISCRETIZATION_STEPS_R) + 2;

	@Override
	public void setup() {
		size(2 * WIDTH + ACCWIDTH, HEIGHT);
		noLoop();
	}

	@Override
	public void draw() {

		PipelineM3 pipeline = new PipelineM3(this);
		int orange = color(255, 80, 0);
		fill(orange);
		stroke(orange);
		PImage result = loadImage("board/"+BOARD);

		image(result, 0, 0, WIDTH, HEIGHT); // original image

		result = pipeline.selectHueThreshold(result, 95, 140, 0);
		result = pipeline.selectBrightnessThreshold(result, 20, 180, 0);
		result = pipeline.selectSaturationThreshold(result, 70, 255, 0);
		result = pipeline.convolute(result, PipelineM3.gaussianKernel);
		result = pipeline.binaryBrightnessThreshold(result, 20, 0, 180);
		result = pipeline.sobel(result, 0.35f, 255, 0);

		image(result, WIDTH + ACCWIDTH, 0, WIDTH, HEIGHT); // SOBEL image

		int[] accumulator = pipeline.houghAccumulator(result);
		PImage houghImage = createImage(ACCWIDTH, ACCHEIGHT, ALPHA);

		for (int i = 0; i < accumulator.length; ++i) {
			houghImage.pixels[i] = color(min(255, accumulator[i]));
		}

		houghImage.updatePixels();
		image(houghImage, WIDTH, 0, ACCWIDTH , HEIGHT); // ACCUMULATOR

		List<PVector> lines = pipeline.hough(result, accumulator);
		List<PVector> plane = pipeline.getPlane(result, lines);

		if (!plane.isEmpty()) {

			pipeline.debugPlotLine(result, plane.subList(4, 8)); // LINES
			for (PVector corner : plane.subList(0, 4)) {
				ellipse(corner.x , corner.y, CORNER_RADIUS, CORNER_RADIUS); // CORNER
			}

		} else {
			println("No plane detected");
		}
	}

}
