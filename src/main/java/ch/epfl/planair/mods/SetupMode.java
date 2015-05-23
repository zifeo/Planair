package ch.epfl.planair.mods;

import ch.epfl.planair.Planair;
import ch.epfl.planair.visual.Pipeline;
import processing.core.PApplet;

public final class SetupMode extends Mode {

	public final Pipeline pipeline;

	public SetupMode(PApplet p) {
		super(p);
		this.pipeline = new Pipeline(p);
	}

	@Override
	public void update() {


	}

	@Override
	public void draw() {

		/*pipeline.selectHueThreshold(image, 80, 125, 0);
		//result = pipeline.selectHueThreshold(result, 95, 140, 0);
		pipeline.selectBrightnessThreshold(image, 30, 240, 0);
		pipeline.selectSaturationThreshold(image, 80, 255, 0);
		pipeline.binaryBrightnessThreshold(image, 20, 0, 180);
		pipeline.convolute(image, Pipeline.gaussianKernel);
		pipeline.sobel(image, 0.35f);*/

	}

	@Override
	public void mousePressed() {
		Planair.become(MenuMode.class);
	}
}
