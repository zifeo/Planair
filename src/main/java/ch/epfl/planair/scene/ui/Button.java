package ch.epfl.planair.scene.ui;

import ch.epfl.planair.meta.Constants;
import ch.epfl.planair.scene.ui.Action;
import ch.epfl.planair.meta.Utils;
import ch.epfl.planair.specs.Drawable;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PVector;

public final class Button extends Drawable {

	private Action callback;
	private boolean active;
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private final String text;
	private final PFont font;
	private int fill;
	private int stroke;
	private final PGraphics screen;

	public Button(PGraphics screen, int x, int y, int width, int height, String text, Action callback) {
		super(screen.parent, new PVector(x, y, 0));
		this.screen = screen;
		this.callback = callback;
		this.active = true;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.text = text.toUpperCase();
		this.font = parent.createFont("fonts/SF-Archery-Black/SF_Archery_Black.ttf", 25);
		this.stroke = Constants.COLOR1;
		this.fill = Constants.COLORBG;
	}

	@Override
	public void draw() {
		screen.stroke(stroke);
		screen.fill(fill);
		screen.rect(x, y, width - 1, height - 1);
		screen.fill(Constants.COLOR1);
		screen.textFont(font);
		screen.textAlign(parent.CENTER, parent.CENTER);
		screen.text(text, x + width / 2, y + height / 2 - 2);
	}

	public void disable() { active = false; }

	public void enable() { active = false; }

	public boolean hover() {
		return Utils.in(0, parent.mouseX - (parent.width - Constants.MENU_WIDTH) / 2 - x, width) &&
				Utils.in(0, parent.mouseY - parent.height / 4 - y, height);
	}

	public void mousePressed() {
		if (active && hover()) {
			callback.run();
		}
	}

	public void mouseReleased() {
		//fill = Constants.COLORBG;
	}

	public void mouseMoved() {
		if (active) {
			stroke = hover() ? Constants.RED: Constants.COLOR1;
		}
	}
}
