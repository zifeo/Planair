package ch.epfl.planair.modes;

import processing.core.PApplet;
import processing.event.MouseEvent;

/**
 * Represents a mode or state of the game.
 * A mode should handle mouse events, update and draw function
 */
public abstract class Mode {

	protected final PApplet p;

	/** Define processing bind. */
	public Mode(PApplet p) {
		this.p = p;
	}

	/** Each FPS send a tick that possibly call draw upon updates are done. */
	public void tick() {
		update();
		draw();
	}

	/** Update mode system. */
	public void update() {}
	/** Draw mode frame. */
	public abstract void draw();

	/** Called when entering this mode. */
	public void entered() {}
	/** Called when exiting this mode. */
	public void exited() {}

	/** @see PApplet */
	public void mousePressed() {}
	/** @see PApplet */
	public void mouseReleased() {}

	/** @see PApplet */
	public void mouseMoved() {}
	/** @see PApplet */
	public void mouseDragged() {}

	/** @see PApplet */
	public void mouseWheel(MouseEvent e) {}

	/** @see PApplet */
	public void keyPressed() {}
	/** @see PApplet */
	public void keyReleased() {}

}
