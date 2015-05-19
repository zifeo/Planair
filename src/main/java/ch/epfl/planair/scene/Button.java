package ch.epfl.planair.scene;

import ch.epfl.planair.config.Utils;
import ch.epfl.planair.specs.Drawable;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public final class Button extends Drawable {

	private Action callback;
	private boolean active;
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private final String text;
	private final PFont font;
	private int color;

	public Button(PApplet parent, int x, int y, int width, int height, String text, Action callback) {
		super(parent, new PVector(x, y, 0));
		this.callback = callback;
		this.active = true;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.text = text;
		this.font = parent.createFont("fonts/SF-Archery-Black/SF_Archery_Black.ttf", 25);
		this.color = parent.color(150);
	}

	@Override
	public void draw() {

		parent.stroke(color);
		parent.fill(parent.color(255));
		parent.rect(x, y, width, height);
		parent.textFont(font);
		parent.textAlign(parent.CENTER, parent.CENTER);
		parent.text(text, x + width / 2, y + height / 2);
	}

	public void disable() { active = false; }

	public void enable() { active = false; }

	@Override
	public void update() {

	}

	private boolean hover() {
		return Utils.in(0, parent.mouseX - x, width) && Utils.in(0, parent.mouseY - y, height);
	}

	public void mousePressed() {
		if (active && hover()) {
			callback.run();
			color = parent.color(255);
		}
	}

	public void mouseReleased() {
		color = parent.color(150);
	}

	public void mouseMoved() {
		if (active) {
			color = parent.color(hover() ? 0 : 150);
		}
	}
}
