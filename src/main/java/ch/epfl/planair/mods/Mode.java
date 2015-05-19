package ch.epfl.planair.mods;

import processing.core.PApplet;
import processing.event.MouseEvent;

public abstract class Mode {

	protected final PApplet parent;

	public Mode(PApplet parent) {
		this.parent = parent;
	}

	public abstract void draw();

	public abstract void update();

	public void mousePressed() {}
	public void mouseReleased() {}

	public void mouseMoved() {}
	public void mouseDragged() {}

	public void mouseWheel(MouseEvent e) {}

	public void keyPressed() {}
	public void keyReleased() {}

}
