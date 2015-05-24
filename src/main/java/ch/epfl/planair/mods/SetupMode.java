package ch.epfl.planair.mods;

import ch.epfl.planair.Planair;
import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.meta.PipelineConfig;
import ch.epfl.planair.scene.ui.ActionButton;
import ch.epfl.planair.scene.ui.Range;
import ch.epfl.planair.visual.PipelineOnPlace;
import processing.core.*;
import processing.video.Capture;

import java.util.List;

public final class SetupMode extends Mode {

	private final PipelineOnPlace pipeline;
	private final Capture webcam;
	private final ActionButton nextActionButton;
	private final ActionButton previousActionButton;
	private final Range rangeButton;
	private final PGraphics panel;
	private final int offsetX;
	private final int offsetY;

	private PipelineConfig.Step status;
	private PipelineConfig config;

	public SetupMode(PApplet p, Capture webcam, PipelineConfig config) {
		super(p);
		this.pipeline = new PipelineOnPlace(p);
		this.webcam = webcam;
		this.status =  PipelineConfig.Step.HUE;
		this.offsetX = (p.width - webcam.width) / 2;
		this.offsetY = (p.height - webcam.height) / 2;
		this.panel = p.createGraphics(webcam.width, Consts.MENU_ITEM_HEIGHT + 10 + Consts.SCROLL_HEIGHT, PApplet.P2D);
		this.previousActionButton = new ActionButton(
				this.panel,
				0,
				10 + Consts.SCROLL_HEIGHT,
				-webcam.width / 2,
				webcam.height / 2 - 40,
				webcam.width / 2 - 5,
				Consts.MENU_ITEM_HEIGHT,
				"Previous",
				this::previous
		);
		this.nextActionButton = new ActionButton(
				this.panel,
				webcam.width / 2 + 5,
				10 + Consts.SCROLL_HEIGHT,
				-webcam.width / 2,
				webcam.height / 2 - 40,
				webcam.width / 2 - 5,
				Consts.MENU_ITEM_HEIGHT,
				"Next",
				this::next
		);
		this.config = config;
		this.rangeButton = new Range(
				this.panel,
				0,
				0,
				-webcam.width / 2,
				webcam.height / 2 - 40,
				webcam.width,
				Consts.SCROLL_HEIGHT,
				this.config.lowerUnit(PipelineConfig.Step.HUE),
				this.config.upperUnit(PipelineConfig.Step.HUE)
		);
	}

	@Override
	public void update() {
		config.lower(status, rangeButton.min());
		config.upper(status, rangeButton.max());
	}

	@Override
	public void draw() {
		if (webcam.available()) {

			webcam.read();
			PImage image = webcam.get();
			drawPlanel();

			p.cursor(nextActionButton.hover() || previousActionButton.hover() || rangeButton.hover() ? PConstants.HAND : PConstants.ARROW);
			p.noLights();
			p.image(panel, offsetX, offsetY + webcam.height - 40);
			p.lights();

			pipeline.selectHueThreshold(image, config.lower(PipelineConfig.Step.HUE), config.upper(PipelineConfig.Step.HUE), 0);

			if (status.compareTo(PipelineConfig.Step.HUE) > 0) {
				pipeline.selectBrightnessThreshold(image, config.lower(PipelineConfig.Step.BRIGHTNESS), config.upper(PipelineConfig.Step.BRIGHTNESS), 0);
			}

			if (status.compareTo(PipelineConfig.Step.BRIGHTNESS) > 0) {
				pipeline.selectSaturationThreshold(image, config.lower(PipelineConfig.Step.SATURATION), config.upper(PipelineConfig.Step.SATURATION), 0);
			}

			if (status.compareTo(PipelineConfig.Step.SATURATION) > 0) {
				pipeline.binaryBrightnessThreshold(image, config.lower(PipelineConfig.Step.SOBEL), 0, 180);
				pipeline.convolute(image);
				pipeline.sobel(image, 0.35f);
			}
			p.image(image, offsetX, offsetY - 50);

			if (status.compareTo(PipelineConfig.Step.SATURATION) > 0) {
				List<PVector> lines = pipeline.hough(image);
				List<PVector> corners = pipeline.getPlane(image, lines);

				if (!corners.isEmpty()) {
					p.fill(Consts.RED);
					for (PVector corner : corners.subList(0, 4)) {
						p.ellipse(offsetX + corner.x, offsetY - 50 + corner.y, 10, 10);
					}
				}
			}

		}
	}


	private void drawPlanel() {
		panel.beginDraw();
		rangeButton.draw();
		previousActionButton.draw();
		nextActionButton.draw();
		panel.endDraw();
	}

	private void previous() {
		update();
		int previous = status.ordinal() - 1;
		if (previous < 0) {
			Planair.become(MenuMode.class);
		} else {
			toStep(previous);
		}
	}

	private void next() {
		update();
		int next = status.ordinal() + 1;
		if (next >= PipelineConfig.Step.values().length) {
			Planair.become(MenuMode.class);
		} else {
			toStep(next);
		}
	}

	private void toStep(int index) {
		PipelineConfig.Step[] steps = PipelineConfig.Step.values();
		assert index < steps.length;
		status = steps[index];
		rangeButton.min(config.lowerUnit(status));
		rangeButton.max(config.upperUnit(status));
		previousActionButton.text(index == 0 ? "Back": "Previous");
		nextActionButton.text(index + 1 == steps.length ? "Finish": "Next");
	}

	@Override
	public void entered() {
		webcam.start();
		toStep(0);
		previousActionButton.text("Back");
		nextActionButton.text("Next");
		previousActionButton.mouseMoved();
		nextActionButton.mouseMoved();
	}

	@Override
	public void exited() {
		webcam.stop();
	}

	@Override public void mousePressed() {
		previousActionButton.mousePressed();
		nextActionButton.mousePressed();
	}
	@Override public void mouseReleased() {
		previousActionButton.mouseReleased();
		nextActionButton.mouseReleased();
	}
	@Override public void mouseMoved() {
		previousActionButton.mouseMoved();
		nextActionButton.mouseMoved();
	}
	@Override public void mouseDragged() {
		rangeButton.mouseDragged();
	}

	@Override
	public void keyPressed() {
		switch (p.keyCode) {
			case 27: Planair.become(MenuMode.class); p.key = 0; break; // ESC
		}
	}

}
