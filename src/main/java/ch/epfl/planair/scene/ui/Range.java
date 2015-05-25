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
	public Range(PGraphics screen, int x, int y, int sx, int sy, int width, int height, float c1, float c2) {
		super(screen, x, y, sx, sy, width, height);
		Utils.require(0, c1, 1, "invalid position for cursor 1");
		Utils.require(0, c2, 1, "invalid position for cursor 2");
		this.focus = null;
		this.cursor1 = new Cursor(c1);
		this.cursor2 = new Cursor(c2);
		this.weight = 10;
		this.stroke = Consts.COLOR1;
		this.fill = Consts.COLORBG;
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

	public void min(float c1) {
		cursor1.set(c1);
	}

	public void max(float c2) {
		cursor2.set(c2);
	}

	public float min() {
		return (PApplet.min(cursor1.pos, cursor2.pos) - weight) / (width - 2f * weight);
	}

	public float max() {
		return (PApplet.max(cursor1.pos, cursor2.pos) - weight) / (width - 2f * weight);
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
		public Cursor(float v) {
			set(v);
		}
		public void set(float v) {
			Utils.require(0, v, 1, "invalid position for cursor:"+v);
			pos = (int) Utils.trim(v * (width - 2 * weight) + weight, weight, width - weight);
		}
	}

}