package ch.epfl.planair.mods;

import ch.epfl.planair.Planair;
import ch.epfl.planair.visual.Pipeline;
import ch.epfl.planair.visual.TwoDThreeD;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;

import java.util.List;


public final class SetupMode extends Mode {

	private enum Step {
		HUE, BRIGHTNESS, SATURATION, SOBEL
	}

	private final Pipeline pipeline;
	private final TwoDThreeD twoDThreeD;
	private final Capture webcam;
	private Step status;

	public SetupMode(PApplet p, Capture webcam) {
		super(p);
		this.pipeline = new Pipeline(p);
		this.twoDThreeD = new TwoDThreeD(webcam.width, webcam.height);
		this.webcam = webcam;
		this.status = Step.HUE;
	}

	@Override
	public void update() {


	}

	@Override
	public void draw() {
		if (webcam.available()) {

			webcam.read();
			PImage image = webcam.get();
			snap(image);

			pipeline.selectHueThreshold(image, 80, 125, 0);

			pipeline.selectBrightnessThreshold(image, 30, 240, 0);

			pipeline.selectSaturationThreshold(image, 80, 255, 0);

			pipeline.binaryBrightnessThreshold(image, 20, 0, 180);

			pipeline.convolute(image, Pipeline.gaussianKernel);

			pipeline.sobel(image, 0.35f);

			// Partie QUAD a refactorer
			List<PVector> lines = pipeline.hough(image);
			List<PVector> corners = pipeline.getPlane(image, lines);

			if (corners.size() >= 8) {
				PVector r = twoDThreeD.get3DRotations(corners.subList(0, 4));

				/*rx.set(Float.floatToIntBits(r.x));
				ry.set(Float.floatToIntBits(r.z));
				rz.set(Float.floatToIntBits(-r.y));
*/
				//p.println(r.x + " " + r.y);
			}

		}
	}

	private void snap(PImage image) {
		p.image(image, (p.width - webcam.width) / 2, (p.height - webcam.height) / 2 - 100);
	}

	@Override
	public void entered() {
		webcam.start();
	}

	@Override
	public void exited() {
		webcam.stop();
	}

	@Override
	public void mousePressed() {
		Planair.become(MenuMode.class);
	}

	@Override
	public void keyPressed() {
		switch (p.keyCode) {
			case 27: Planair.become(MenuMode.class); p.key = 0; break; // ESC
		}
	}

}
