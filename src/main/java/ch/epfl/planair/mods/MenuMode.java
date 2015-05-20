package ch.epfl.planair.mods;

import ch.epfl.planair.Planair;
import ch.epfl.planair.scene.ui.Action;
import ch.epfl.planair.meta.Constants;
import ch.epfl.planair.scene.ui.Button;
import processing.core.PApplet;
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
		this.logoFont = p.createFont(Constants.FONT, 80);
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
		menu.add(createMenuButton(count++, "Setup", () -> Planair.become(SetupMode.class)));
		menu.add(createMenuButton(count++, "Exit", () -> p.exit()));
	}

	private Button createMenuButton(int count, String text, Action action) {
		return new Button(screen, 0, count * (Constants.MENU_ITEM_HEIGHT + Constants.MENU_ITEM_MARGIN) + 150, Constants.MENU_WIDTH, Constants.MENU_ITEM_HEIGHT, text, action);
	}

	@Override
	public void draw() {
		p.camera(0, 0, (p.height / 2.0f) / p.tan(p.PI * 30.0f / 180.0f), 0, 0, 0, 0, 1, 0);

		screen.beginDraw();
		screen.background(Constants.COLORBG);
		screen.fill(Constants.COLOR1);
		screen.textFont(logoFont);
		screen.textAlign(p.CENTER, p.TOP);
		screen.text(Constants.LOGO, Constants.MENU_WIDTH / 2, 0);

		boolean hovered = false;
		for (Button b : menu) {
			hovered |= b.hover();
			b.draw();
		}
		p.cursor(hovered ? p.HAND: p.ARROW);
		screen.endDraw();

		p.translate(- p.width/2, - p.height/2);

		p.image(screen, (p.width - Constants.MENU_WIDTH) / 2, p.height / 4);

	}

	@Override public void mousePressed() {
		for (Button b : menu) {
			b.mousePressed();
		}
	}

	@Override public void mouseReleased() {
		for (Button b : menu) {
			b.mouseReleased();
		}
	}

	@Override public void mouseMoved() {
		for (Button b : menu) {
			b.mouseMoved();
		}
	}

}
