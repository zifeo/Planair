package ch.epfl.planair.visual;

import cs211.imageprocessing.PipelineM3;
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
	public void setup(){
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

		//image =  loadImage("board/board4.jpg");
		image = cam.get();
		image(image, 0, 0);
		PImage result = image;

		result = pipeline.selectHueThreshold(result, 80, 125, 0);
		//result = pipeline.selectHueThreshold(result, 95, 140, 0);
		result = pipeline.selectBrightnessThreshold(result, 30, 240, 0);
		result = pipeline.selectSaturationThreshold(result, 80, 255, 0);
		result = pipeline.convolute(result, PipelineM3.gaussianKernel);
		result = pipeline.binaryBrightnessThreshold(result, 20, 0, 180);
		result = pipeline.sobel(result, 0.35f);

		// Partie QUAD a refactorer
		List<PVector> lines = pipeline.hough(result);
		List<PVector> corners = pipeline.getPlane(result, lines);

		if(corners.size() > 3) {
			fill(Color.ORANGE.getRGB());
			quad(corners.get(0).x, corners.get(0).y,
					corners.get(1).x, corners.get(1).y,
					corners.get(2).x, corners.get(2).y,
					corners.get(3).x, corners.get(3).y);

			PVector r = twoDThreeD.get3DRotations(Arrays.asList(corners.get(0), corners.get(1),
					corners.get(2), corners.get(3)));
			System.out.println("rx = " + (int)Math.toDegrees(r.x) + ", ry = " + (int)Math.toDegrees(r.y) + ", rz = " + (int)Math.toDegrees(r.z) + "°");
		}


		// Fin QUAD
		pipeline.debugPlotLine(result, lines);
		//image(result, 0, 0);
	}





}