package ch.epfl.planair.scene.ui;

import ch.epfl.planair.meta.Utils;
import ch.epfl.planair.specs.Drawable;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PVector;

public abstract class Button extends Drawable {

	protected final PGraphics screen;
	private Action callback;

	protected final int x;
	protected final int y;
	protected final int sx;
	protected final int sy;
	protected final int width;
	protected final int height;
	protected final PFont font;

	private boolean active;

	/**
	 *
	 * @param screen
	 * @param x
	 * @param y
	 * @param sx x correction for mouse hover
	 * @param sy y correction for mouse hover
	 * @param width
	 * @param height
	 */
	public Button(PGraphics screen, int x, int y, int sx, int sy, int width, int height) {
		super(screen.parent, new PVector(x, y, 0));
		this.screen = screen;
		this.active = true;
		this.x = x;
		this.y = y;
		this.sx = sx;
		this.sy = sy;
		this.width = width;
		this.height = height;
		this.font = parent.createFont("fonts/SF-Archery-Black/SF_Archery_Black.ttf", 25);
	}

	@Override
	public abstract void draw();

	public void disable() { active = false; }

	public void enable() { active = false; }

	public PFont font() {
		return font;
	}

	public boolean active() {
		return active;
	}

	public boolean hover() {
		return Utils.in(0, relatifX(), width) &&
				Utils.in(0, relatifY(), height);
	}

	public int relatifX() {
		return relatifX(parent.mouseX);
	}
	public int relatifX(int mx) {
		return mx - parent.width / 2 - sx - x;
	}
	public int relatifY() {
		return relatifY(parent.mouseY);
	}
	public int relatifY(int mx) {
		return mx - parent.height / 2 - sy - y;
	}

}