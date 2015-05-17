package cs211.imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.util.List;

public final class ImageProcessing extends PApplet {

	private final static String BOARD = "board1.jpg";
	private final static String BOARD2 = "board2.jpg";
	private final static String BOARD3 = "board3.jpg";
	private final static String BOARD4 = "board4.jpg";
	private final static int SCALE = 2;
	private final static int WIDTH = 800 / SCALE;
	private final static int HEIGHT = 600 / SCALE;
	private final static int CORNER_RADIUS = 20 / SCALE;
	private final static int ACCHEIGHT = (int) (Math.PI / Constants.PIPELINE_DISCRETIZATION_STEPS_PHI) + 2;
	private final static int ACCWIDTH = (int) (((WIDTH * SCALE + HEIGHT * SCALE) * 2 + 1) / Constants.PIPELINE_DISCRETIZATION_STEPS_R) + 2;

	@Override
	public void setup() {
		size(2 * WIDTH + ACCWIDTH / SCALE, 4 * HEIGHT);
		noLoop();
	}

	@Override
	public void draw() {

		PipelineM3 pipeline = new PipelineM3(this);
		int orange = color(255, 80, 0);
		fill(orange);
		stroke(orange);

		run(loadImage("board/"+BOARD), pipeline, 0);
		run(loadImage("board/"+BOARD2), pipeline, 3 * HEIGHT);
		run(loadImage("board/"+BOARD3), pipeline, 2 * HEIGHT);
		run(loadImage("board/"+BOARD4), pipeline, 1 * HEIGHT);
	}

	private void run(PImage result, PipelineM3 pipeline, int h) {

		image(result, 0 / SCALE, h, WIDTH, HEIGHT);
		result = pipeline.selectHueThreshold(result, 95, 140, 0);
		result = pipeline.selectBrightnessThreshold(result, 20, 180, 0);
		result = pipeline.selectSaturationThreshold(result, 70, 255, 0);
		result = pipeline.convolute(result, PipelineM3.gaussianKernel);
		result = pipeline.binaryBrightnessThreshold(result, 20, 0, 180);

		result = pipeline.sobel(result, 0.35f, 255, 0);
		image(result, WIDTH + ACCWIDTH / SCALE, h, WIDTH, HEIGHT); // SOBEL
		//image(result, 0, h); // SOBEL

		int[] accumulator = pipeline.houghAccumulator(result);
		PImage houghImg = createImage(ACCWIDTH, ACCHEIGHT, ALPHA);
		for (int i = 0; i < accumulator.length; ++i) {
			houghImg.pixels[i] = color(min(255, accumulator[i]));
			int value = accumulator[i];
			int color = 0;
			if (value > 255) color = color(255);
			else if (value > 200) color = color(255, 0, 0);
			else color = color(accumulator[i]);
			houghImg.pixels[i] = color;
		}
		houghImg.updatePixels();
		image(houghImg, WIDTH, h, ACCWIDTH / SCALE, HEIGHT); // ACCUMULATOR

		List<PVector> lines = pipeline.hough(result, accumulator);
		List<PVector> plane = pipeline.getPlane(result, lines);

		if (!plane.isEmpty()) {

			pipeline.debugPlotLine(result, plane.subList(4, 8), SCALE, h); // LINES
			for (PVector corner : plane.subList(0, 4)) {
				ellipse(corner.x / SCALE, corner.y / SCALE + h, CORNER_RADIUS, CORNER_RADIUS); // CORNER
			}

		} else {
			println("No cycle detected");
		}
	}

}
