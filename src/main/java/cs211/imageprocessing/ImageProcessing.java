package cs211.imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.util.List;

/**
 * Milestone 3 class.
 * Show the selected board, its haugh image, its accumulator image, borders and corners.
 */
public final class ImageProcessing extends PApplet {

	private final static String BOARD = "board4.jpg";
	private final static float SCALE = 0.7f;
	private final static int WIDTH = 800;
	private final static int HEIGHT = 600;
	private final static int CORNER_RADIUS = 20;
	private final static int ACCHEIGHT = (int) (Math.PI / Constants.PIPELINE_DISCRETIZATION_STEPS_PHI) + 2;
	private final static int ACCWIDTH = (int) (((WIDTH + HEIGHT) * 2 + 1) / Constants.PIPELINE_DISCRETIZATION_STEPS_R) + 2;

	@Override
	public void setup() {
		size((int) ((2 * WIDTH + ACCWIDTH) * SCALE), (int) (HEIGHT * SCALE));
		noLoop();
	}

	@Override
	public void draw() {
		scale(SCALE);

		PImage original = loadImage("board/"+BOARD);
		PipelineM3 pipeline = new PipelineM3(this);
		final int orange = color(255, 80, 0);
		fill(orange);
		stroke(orange);

		PImage result = original;
		result = pipeline.selectHueThreshold(result, 95, 140, 0);
		result = pipeline.selectBrightnessThreshold(result, 20, 180, 0);
		result = pipeline.selectSaturationThreshold(result, 70, 255, 0);
		result = pipeline.convolute(result, PipelineM3.gaussianKernel);
		result = pipeline.binaryBrightnessThreshold(result, 20, 0, 180);
		result = pipeline.sobel(result, 0.35f, 255, 0);

		int[] accumulator = pipeline.houghAccumulator(result);
		PImage houghImage = createImage(ACCWIDTH, ACCHEIGHT, ALPHA);
		for (int i = 0; i < accumulator.length; ++i) {
			houghImage.pixels[i] = color(min(255, accumulator[i]));
		}
		List<PVector> lines = pipeline.hough(result, accumulator);
		List<PVector> plane = pipeline.getPlane(result, lines);

		image(original, 0, 0, WIDTH, HEIGHT); // ORIGINAL image
		pipeline.debugPlotLine(result, plane.subList(4, 8)); // LINES
		for (PVector corner : plane.subList(0, 4)) {
			ellipse(corner.x , corner.y, CORNER_RADIUS, CORNER_RADIUS); // CORNER
		}
		image(result, WIDTH + ACCWIDTH, 0, WIDTH, HEIGHT); // SOBEL image
		image(houghImage, WIDTH, 0, ACCWIDTH , HEIGHT); // ACCUMULATOR image

	}

}
