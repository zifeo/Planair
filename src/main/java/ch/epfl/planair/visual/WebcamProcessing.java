package ch.epfl.planair.visual;

import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;

public final class WebcamProcessing extends PApplet {

	private Capture cam;
	private PImage img;

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
	}

	@Override
	public void draw() {
		if (cam.available()) {
			cam.read();
		}

		img = cam.get();
		image(img, 0, 0);

		Pipeline pipeline = new Pipeline(this);

		image(img, 0, 0);
	}

}