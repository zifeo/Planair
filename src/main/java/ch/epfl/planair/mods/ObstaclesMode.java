package ch.epfl.planair.mods;

import ch.epfl.planair.Planair;
import processing.core.PApplet;

public final class ObstaclesMode extends Mode {


	public ObstaclesMode(PApplet parent) {
		super(parent);
	}

	@Override
	public void draw() {

	}

	@Override
	public void update() {

	}

	public void keyReleased() {
		switch (parent.keyCode) {
			case 16: Planair.become(PlayMode.class); break; // SHIFT
		}
	}

}
