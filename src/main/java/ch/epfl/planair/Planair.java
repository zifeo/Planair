package ch.epfl.planair;

import ch.epfl.planair.config.Constants;
import ch.epfl.planair.mods.MenuMode;
import ch.epfl.planair.mods.ObstaclesMode;
import ch.epfl.planair.mods.PlayMode;
import processing.core.*;
import ch.epfl.planair.scene.*;
import processing.event.MouseEvent;
import ch.epfl.planair.mods.Mode;

import java.util.*;

public class Planair extends PApplet {

	private static Mode status = null;
	private static Planair self = null;

	private final Map<Class, Mode> logic;

	private final Timer clock;
	//private Status status = Status.MENU;

	private final List<Integer> colors = Arrays.asList(0xFF2C3E50, 0xFF34495E, 0xFF7F8C8D, 0xFF95A5A6);
	private Button playButton = new Button(this, 20, 30, 100, 40, "Play", () -> println("cliqued"));
	private PFont mainFont = createFont("fonts/SF-Archery-Black/SF_Archery_Black.ttf", 32);

	public static void main(String args[]) {
		String[] appletArgs = new String[] { "ch.epfl.planair.Planair" };
		PApplet.main(args != null ? concat(appletArgs, args): appletArgs);
	}

	public static void become(Class<? extends Mode> mode) {
		status = self.logic.get(mode);
		assert status != null;
	}

	public Planair() {

		assert self == null;
		self = this;

		this.logic = new HashMap<>();
		this.clock = new Timer();
		this.clock.scheduleAtFixedRate(new TimerTask() {
			public void run() {  if (status != null) status.update(); }
		}, 0, 1000 / Constants.FRAMERATE);
	}

	@Override
	public void setup() {
		size(Constants.WINDOWS_WIDTH, Constants.WINDOWS_HEIGHT, P3D);
		frameRate(Constants.FRAMERATE);

		assert status == null;
		this.logic.put(PlayMode.class, new PlayMode(this, width, height));
		this.logic.put(ObstaclesMode.class, new ObstaclesMode(this));
		this.logic.put(MenuMode.class, new MenuMode(this));
		status = this.logic.get(PlayMode.class);
		assert status != null;
	}

	@Override
	public void draw() {
		background(200);
		lights();

		status.draw();

		/*switch (status) {

			case MENU:
				//playButton.draw();
				fill(colors.get(0));
				textFont(mainFont);
				textAlign(CENTER, CENTER);
				text("Planair", width/2, height/2);

				break;

			case ADD_CYLINDER:
				camera(0, -(height/2.0f) / tan(PI*30.0f / 180.0f), 0, 0, 0, 0, 0, 0, 1);

				shiftCylinder.setLocation(new PVector(mouseX - width/2, -Constants.PLATE_THICKNESS/2, mouseY - height/2));
				shiftCylinder.draw();
				break;
		}*/

		if (Constants.DEBUG) {
			fill(color(0));
			textSize(11f);
			text(String.format("fps: %.1f", frameRate), 2, 13);
		}
	}

	@Override public boolean sketchFullScreen() {
		return true;
	}

	@Override public void mouseWheel(MouseEvent e) {
		status.mouseWheel(e);
	}
	@Override public void mouseDragged() {
		status.mouseDragged();
	}
	@Override public void mouseMoved() {
		status.mouseMoved();
	}
	@Override public void mouseReleased() {
		status.mousePressed();
	}
	@Override public void mousePressed() {
		status.mousePressed();

		/*switch (status) {

			case MENU:
				playButton.mousePressed();

			case ADD_CYLINDER:
				PVector wantedLocation = shiftCylinder.location();
				PVector sphereLocation = sphere.location();
				float angle = PVector.angleBetween(wantedLocation, sphereLocation);
				float distance = PVector.dist(wantedLocation, sphereLocation);
				float borders = shiftCylinder.get2DDistanceFrom(angle) + sphere.get2DDistanceFrom(angle + PI);

				if (distance > borders) {
					//Cylinder obstacle = new Cylinder(shiftCylinder);
					Tree obstacle = new Tree(shiftCylinder);
					cylinders.add(obstacle);
					scoreboard.addForProjection(obstacle);
				}
				break;
		}*/
	}
	@Override public void keyReleased() {
		status.keyReleased();
	}
	@Override public void keyPressed() {
		status.keyPressed();
	}
}
