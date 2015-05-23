package ch.epfl.planair.mods;

import ch.epfl.planair.Planair;
import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.scene.ui.Button;
import ch.epfl.planair.visual.Pipeline;
import ch.epfl.planair.visual.PipelineOnPlace;
import ch.epfl.planair.visual.TwoDThreeD;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.video.Capture;


public final class SetupMode extends Mode {

	private enum Step {
		HUE, BRIGHTNESS, SATURATION, SOBEL
	}

	private final PipelineOnPlace pipeline;
	private final TwoDThreeD twoDThreeD;
	private final Capture webcam;
	private final Button next;
	private final Button previous;
	private final PGraphics panel;
	private final int offsetX;
	private final int offsetY;

	private Step status;

	public SetupMode(PApplet p, Capture webcam) {
		super(p);
		this.pipeline = new PipelineOnPlace(p);
		this.twoDThreeD = new TwoDThreeD(webcam.width, webcam.height);
		this.webcam = webcam;
		this.status = Step.HUE;
		this.panel = p.createGraphics(webcam.width, 80, PApplet.P2D);
		this.offsetX = (p.width - webcam.width) / 2;
		this.offsetY = (p.height - webcam.height) / 2;
		this.previous = new Button(
				panel,
				0,
				80 - Consts.MENU_ITEM_HEIGHT,
				- this.offsetX,
				- this.offsetY + webcam.height - 40,
				webcam.width / 2 - 5,
				Consts.MENU_ITEM_HEIGHT,
				"Previous",
				()-> p.println("")
		);
		this.next = new Button(
				panel,
				webcam.width / 2 + 5,
				80 - Consts.MENU_ITEM_HEIGHT,
				this.offsetX,
				this.offsetY + webcam.height - 40,
				webcam.width / 2 - 5,
				Consts.MENU_ITEM_HEIGHT,
				"Next",
				()-> p.println("")
		);
	}

	@Override
	public void update() {


	}

	@Override
	public void draw() {
		if (webcam.available()) {

			webcam.read();
			PImage image = webcam.get();
			//snap(image);

			panel.beginDraw();
			panel.background(Consts.COLORBG);
			previous.draw();
			next.draw();
			panel.endDraw();

			p.noLights();
			p.image(panel, offsetX, offsetY + webcam.height - 40);
			p.lights();

			p.image(image, offsetX, offsetY - 50);

			pipeline.selectHueThreshold(image, 80, 125, 0);

			if (status.compareTo(Step.HUE) <= 0) return;

			pipeline.selectBrightnessThreshold(image, 30, 240, 0);

			pipeline.selectSaturationThreshold(image, 80, 255, 0);

			pipeline.binaryBrightnessThreshold(image, 20, 0, 180);

			pipeline.convolute(image, Pipeline.gaussianKernel);

			pipeline.sobel(image, 0.35f);

			// Partie QUAD a refactorer
			//List<PVector> lines = pipeline.hough(image);
			//List<PVector> corners = pipeline.getPlane(image, lines);

			/*if (corners.size() >= 8) {
				PVector r = twoDThreeD.get3DRotations(corners.subList(0, 4));

				rx.set(Float.floatToIntBits(r.x));
				ry.set(Float.floatToIntBits(r.z));
				rz.set(Float.floatToIntBits(-r.y));

				//p.println(r.x + " " + r.y);
			}*/

		}
	}


	@Override
	public void entered() {
		webcam.start();
		webcam.frameRate = 2;

	}

	@Override
	public void exited() {
		webcam.stop();
	}

	@Override public void mousePressed() {
		previous.mousePressed();
		next.mousePressed();
	}
	@Override public void mouseReleased() {
		previous.mouseReleased();
		next.mouseReleased();
	}
	@Override public void mouseMoved() {
		previous.mouseMoved();
		next.mouseMoved();
	}

	@Override
	public void keyPressed() {
		switch (p.keyCode) {
			case 27: Planair.become(MenuMode.class); p.key = 0; break; // ESC
		}
	}

}
