package ch.epfl.planair.modes;

import processing.core.PApplet;
import processing.event.MouseEvent;

/**
 * Represents a mode or state of the game.
 * A mode should handle mouse events, update and draw function
 */
public abstract class Mode {

	protected final PApplet p;

	public Mode(PApplet p) {
		this.p = p;
	}

	public void update() {} // call before draw
	public abstract void draw();

	public void mousePressed() {}
	public void mouseReleased() {}

	public void mouseMoved() {}
	public void mouseDragged() {}

	public void mouseWheel(MouseEvent e) {}

	public void keyPressed() {}
	public void keyReleased() {}

}
