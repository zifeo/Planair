package ch.epfl.planair.mods;

import ch.epfl.planair.Planair;
import ch.epfl.planair.scene.ui.Action;
import ch.epfl.planair.meta.Constants;
import ch.epfl.planair.scene.ui.Button;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

public final class MenuMode extends Mode {

	private final PFont logoFont;
	private final PFont menuFont;
	private final List<Button> menu;
	private final PGraphics screen;

	private final int menuX;
	private final int menuY;

	public MenuMode(PApplet parent) {
		super(parent);
		this.logoFont = p.createFont(Constants.FONT, 120);
		this.menuFont= p.createFont(Constants.FONT, 20);
		this.menu = new ArrayList<>();
		this.menuX = (p.width - Constants.MENU_WIDTH) / 2;
		this.menuY = (p.height - Constants.MENU_HEIGHT_CENTER) / 2;
		this.screen = p.createGraphics(Constants.MENU_WIDTH, p.height / 2, PApplet.P2D);
		constructMenu();
	}

	private void constructMenu() {
		int count = 0;
		menu.add(createMenuButton(count++, "Play!", () -> Planair.become(PlayMode.class)));
		menu.add(createMenuButton(count++, "Battle!", () -> PApplet.println("battle")));
		menu.add(createMenuButton(count++, "Cam Setup", () -> Planair.become(SetupMode.class)));
		menu.add(createMenuButton(count++, "Options", () -> PApplet.println("options")));
		menu.add(createMenuButton(count++, "Exit", p::exit));
	}

	private Button createMenuButton(int count, String text, Action action) {
		return new Button(screen, 0, count * (Constants.MENU_ITEM_HEIGHT + Constants.MENU_ITEM_MARGIN) + 180, Constants.MENU_WIDTH, Constants.MENU_ITEM_HEIGHT, text, action);
	}

	@Override
	public void draw() {
		p.camera(0, 0, (p.height / 2.0f) / PApplet.tan(PConstants.PI * 30.0f / 180.0f), 0, 0, 0, 0, 1, 0);

		screen.beginDraw();
		screen.background(Constants.COLORBG);
		screen.fill(Constants.COLOR1);
		screen.textFont(logoFont);
		screen.textAlign(PConstants.CENTER, PConstants.TOP);
		screen.text(Constants.LOGO, Constants.MENU_WIDTH / 2, 0);
		boolean hovered = false;
		for (Button b : menu) {
			hovered |= b.hover();
			b.draw();
		}
		screen.endDraw();

		p.cursor(hovered ? PConstants.HAND : PConstants.ARROW);
		p.image(screen, - Constants.MENU_WIDTH / 2, - p.height / 4);

	}

	@Override public void mousePressed() {
		menu.forEach(Button::mousePressed);
	}

	@Override public void mouseReleased() {
		menu.forEach(Button::mouseReleased);
	}

	@Override public void mouseMoved() {
		menu.forEach(Button::mouseMoved);
	}

}
