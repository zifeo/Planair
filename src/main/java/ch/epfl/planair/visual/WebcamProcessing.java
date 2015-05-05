package ch.epfl.planair.visual;

import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;

public final class WebcamProcessing extends PApplet {

	private Capture cam;
	private PImage image;
	private Pipeline pipeline;

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
		result = pipeline.hough(result);

		image(result, 0, 0);
	}

}