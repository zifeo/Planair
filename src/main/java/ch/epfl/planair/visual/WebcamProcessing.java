package ch.epfl.planair.visual;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public final class WebcamProcessing extends PApplet {

	private Capture cam;
	private PImage image;
	private Pipeline pipeline;
	private QuadGraph quad;
	private TwoDThreeD twoDThreeD;

	@Override
	public void setup() {

		size(640, 480);
		String[] cameras = Capture.list();
		if (cameras.length == 0) {
			println("No camera :( !");
			exit();
		} else {
			println("Cameras:");
			for (int i = 0; i < cameras.length; i++) {
				println(cameras[i]);
			}
			cam = new Capture(this, 640, 480, 15);
			// cam = new Capture(this, cameras[4]);
			cam.start();
		}
		pipeline = new Pipeline(this);
		quad = new QuadGraph();

		twoDThreeD = new TwoDThreeD(cam.width, cam.height);
	}

	@Override
	public void draw() {
		if (cam.available()) {
			cam.read();
		}

		image = cam.get();
		image(image, 0, 0);
		PImage result = image;

		result = pipeline.selectHueThreshold(result, 80, 125, 0);
		result = pipeline.selectBrightnessThreshold(result, 30, 255, 0);
		result = pipeline.selectSaturationThreshold(result, 80, 255, 0);
		result = pipeline.convolute(result, Pipeline.gaussianKernel);
		result = pipeline.sobel(result, 0.35f);

		// Partie QUAD a refactorer
		List<PVector> lines = pipeline.hough(result);
		quad.build(lines, image.width, image.height);

		for (int[] cycle : quad.findCycles()) {

			PVector l1 = lines.get(cycle[0]);
			PVector l2 = lines.get(cycle[1]);
			PVector l3 = lines.get(cycle[2]);
			PVector l4 = lines.get(cycle[3]);
			// (intersection() is a simplified version of the
			// intersections() method you wrote last week, that simply
			// return the coordinates of the intersection between 2 lines)
			PVector c12 = intersection(l1, l2);
			PVector c23 = intersection(l2, l3);
			PVector c34 = intersection(l3, l4);
			PVector c41 = intersection(l4, l1);

			if(quad.isConvex(c12,c23, c34, c41) &&
					quad.validArea(c12, c23, c34, c41, 500000, 60000) &&
					quad.nonFlatQuad(c12, c23, c34, c41)) {
				fill(Color.ORANGE.getRGB());
				quad(c12.x, c12.y, c23.x, c23.y, c34.x, c34.y, c41.x, c41.y);

				System.out.println(twoDThreeD.get3DRotations(Arrays.asList(c12, c23, c34, c41)));
			}
		}
		// Fin QUAD
		pipeline.debugPlotLine(result, lines);
		//image(result, 0, 0);
	}

	public static PVector intersection(PVector line1, PVector line2) {

		double sin_t1 = Math.sin(line1.y);
		double sin_t2 = Math.sin(line2.y);
		double cos_t1 = Math.cos(line1.y);
		double cos_t2 = Math.cos(line2.y);
		float r1 = line1.x;
		float r2 = line2.x;

		double denom = cos_t2 * sin_t1 - cos_t1 * sin_t2;

		int x = (int) ((r2 * sin_t1 - r1 * sin_t2) / denom);
		int y = (int) ((-r2 * cos_t1 + r1 * cos_t2) / denom);

		return new PVector(x, y);
	}
}