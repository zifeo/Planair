package ch.epfl.planair.mods;

import processing.core.PApplet;
import processing.event.MouseEvent;

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
