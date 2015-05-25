package ch.epfl.planair.scene.ui;

import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.meta.Utils;
import ch.epfl.planair.specs.Drawable;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PVector;

public class ActionButton extends Button {

	private final Action callback;
	protected final PFont font;

	private String text;
	private int fill;
	private int stroke;

	/**
	 *
	 * @param screen
	 * @param x
	 * @param y
	 * @param sx x correction for mouse hover
	 * @param sy y correction for mouse hover
	 * @param width
	 * @param height
	 * @param callback
	 */
	public ActionButton(PGraphics screen, int x, int y, int sx, int sy, int width, int height, String text, Action callback) {
		super(screen, x, y, sx, sy, width, height);
		this.callback = callback;
		this.text = text;
		this.text = text.toUpperCase();
		this.font = parent.createFont("fonts/SF-Archery-Black/SF_Archery_Black.ttf", 25);
		this.stroke = Consts.COLOR1;
		this.fill = Consts.COLORBG;
	}

	@Override
	public void draw() {
		screen.stroke(stroke);
		screen.fill(fill);
		screen.rect(x, y, width - 1, height - 1);
		screen.fill(Consts.COLOR1);
		screen.textFont(font);
		screen.textAlign(PConstants.CENTER, PConstants.CENTER);
		screen.text(text, x + width / 2, y + height / 2 - 2);
	}

	public void text(String newText) {
		text = newText;
	}

	public String text() {
		return text;
	}

	public void mousePressed() {
		if (active() && hover()) {
			callback.run();
		}
	}

	public void mouseReleased() {
		//fill = Consts.COLORBG;
	}

	public void mouseMoved() {
		if (active()) {
			stroke = hover() ? Consts.RED: Consts.COLOR1;
		}
	}
}
