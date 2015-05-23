package ch.epfl.planair.mods;

import ch.epfl.planair.Planair;
import processing.core.PApplet;

/**
 * The mode where the user can setup and tune
 * webcam settings
 */
public final class SetupMode extends Mode {

	public SetupMode(PApplet p) {
		super(p);
	}

	@Override
	public void draw() {

	}

	@Override
	public void mousePressed() {
		Planair.become(MenuMode.class);
	}
}
