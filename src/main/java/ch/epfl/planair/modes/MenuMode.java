package ch.epfl.planair.modes;

import ch.epfl.planair.Planair;
import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.scene.ui.Action;
import ch.epfl.planair.scene.ui.ActionButton;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * The welcome mode, where the menu is displayed
 */
public final class MenuMode extends Mode {

	private final PFont logoFont;
	private final List<ActionButton> menu;
	private final PGraphics screen;

	public MenuMode(PApplet parent) {
		super(parent);
		this.logoFont = p.createFont(Consts.FONT, 120);
		this.menu = new ArrayList<>();
		this.screen = p.createGraphics(Consts.MENU_WIDTH, Consts.MENU_HEIGHT, PApplet.P2D);
		constructMenu();
	}

	private void constructMenu() {
		int count = 0;
		menu.add(createMenuButton(count++, "Play!", () -> Planair.become(PlayMode.class)));
		menu.add(createMenuButton(count++, "Test (milestone 4 with video)!", () -> Planair.become(TestMode.class)));
		menu.add(createMenuButton(count++, "Battle!", () -> Planair.music().triggerRampage()));
		menu.add(createMenuButton(count++, "Cam Setup", () -> Planair.become(SetupMode.class)));
		menu.add(createMenuButton(count++, "Exit", p::exit));
	}

	private ActionButton createMenuButton(int count, String text, Action action) {
		return new ActionButton(
				screen,
				0,
				count * (Consts.MENU_ITEM_HEIGHT + Consts.MENU_ITEM_MARGIN) + 180,
				- Consts.MENU_WIDTH / 2,
				- Consts.MENU_HEIGHT / 2,
				Consts.MENU_WIDTH,
				Consts.MENU_ITEM_HEIGHT,
				text,
				action
		);
	}

	@Override
	public void draw() {
		p.camera(0, 0, (p.height / 2f) / PApplet.tan(PConstants.PI * 30f / 180f), 0, 0, 0, 0, 1, 0);
		drawScreen();
		p.noLights();
		p.image(screen, - Consts.MENU_WIDTH / 2, - Consts.MENU_HEIGHT / 2);
		p.lights();
	}

	private void drawScreen() {
		screen.beginDraw();
		screen.background(Consts.COLORBG);
		screen.fill(Consts.COLOR1);
		screen.textFont(logoFont);
		screen.textAlign(PConstants.CENTER, PConstants.TOP);
		screen.text(Consts.LOGO, Consts.MENU_WIDTH / 2, 0);
		boolean hovered = false;
		for (ActionButton b : menu) {
			hovered |= b.hover();
			b.draw();
		}
		screen.endDraw();
		p.cursor(hovered ? PConstants.HAND : PConstants.ARROW);
	}

	@Override public void entered() {
		menu.forEach(ActionButton::mouseMoved);
	}

	@Override public void mousePressed() {
		menu.forEach(ActionButton::mousePressed);
	}
	@Override public void mouseReleased() {
		menu.forEach(ActionButton::mouseReleased);
	}
	@Override public void mouseMoved() {
		menu.forEach(ActionButton::mouseMoved);
	}

}