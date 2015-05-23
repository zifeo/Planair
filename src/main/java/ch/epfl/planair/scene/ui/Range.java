package ch.epfl.planair.scene.ui;

import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.meta.Utils;
import processing.core.PApplet;
import processing.core.PGraphics;

public class Range extends Button {

	private Cursor focus;
	private final Cursor cursor1;
	private final Cursor cursor2;
	private final int weight;

	private int fill;
	private int stroke;

	/**
	 * @param screen
	 * @param x
	 * @param y
	 * @param sx x correction for mouse hover
	 * @param sy y correction for mouse hover
	 * @param width
	 * @param height
	 */
	public Range(PGraphics screen, int x, int y, int sx, int sy, int width, int height, double c1, double c2) {
		super(screen, x, y, sx, sy, width, height);
		Utils.require(0, c1, 1, "invalid position for cursor 1");
		Utils.require(0, c2, 1, "invalid position for cursor 2");
		this.focus = null;
		this.cursor1 = new Cursor((int) (c1 * width));
		this.cursor2 = new Cursor((int) (c2 * width));
		this.weight = 10;
		this.stroke = Consts.COLOR1;
		this.fill = Consts.COLORBG;
	}

	@Override
	public void update() {

	}

	@Override
	public void draw() {
		screen.stroke(stroke);
		screen.fill(fill);
		screen.rect(x, y, width - 1, height - 1);
		screen.fill(Consts.COLOR1);
		screen.rect(x + cursor1.pos - weight, y, 2 * weight - 1, height - 1);
		screen.rect(x + cursor2.pos - weight, y, 2 * weight - 1, height - 1);
	}

	public void min(double c1) {
		Utils.require(0, c1, 1, "invalid position for cursor 1");
		cursor1.pos = (int) (c1 * width);
	}

	public void max(double c2) {
		Utils.require(0, c2, 1, "invalid position for cursor 1");
		cursor1.pos = (int) (c2 * width);
	}

	public float min() {
		return PApplet.min(cursor1.pos, cursor2.pos) * 1f / width;
	}

	public float max() {
		return PApplet.max(cursor1.pos, cursor2.pos) * 1f / width;
	}

	@Override
	public boolean hover() {
		if (super.hover()) {
			if (Utils.in(cursor1.pos - 2 * weight, relatifX(p.pmouseX), cursor1.pos + 2 * weight) && focus != cursor2) {
				focus = cursor1;
				return true;
			}
			if (Utils.in(cursor2.pos - 2 * weight, relatifX(p.pmouseX), cursor2.pos + 2 * weight) && focus != cursor1) {
				focus = cursor2;
				return true;
			}
		}

		focus = null;
		return false;
	}

	public void mouseDragged() {
		if (active() && hover()) {
			focus.pos = (int) Utils.trim(relatifX(), weight, width - weight);
		}
	}

	private final class Cursor {
		private int pos;
		public Cursor(int pos) {
			this.pos = pos;
		}
	}

}