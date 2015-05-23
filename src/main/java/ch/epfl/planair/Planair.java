package ch.epfl.planair;

import ch.epfl.planair.meta.Constants;
import ch.epfl.planair.modes.*;
import processing.core.PApplet;
import processing.event.MouseEvent;

import java.util.*;

public class Planair extends PApplet {

	private static Mode status = null;
	private static Planair self = null;
	private final Map<Class, Mode> logic;
	private final Timer clock;

	public static void main(String args[]) {
		String[] appletArgs = new String[] { "ch.epfl.planair.Planair" };
		PApplet.main(args != null ? concat(appletArgs, args) : appletArgs);
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
		/*this.clock.scheduleAtFixedRate(new TimerTask() {
			public void run() {  if (status != null) status.update(); }
		}, 0, 1000 / Constants.FRAMERATE);*/
	}

	@Override
	public void setup() {
		size(displayWidth, displayHeight, P3D);
		frameRate(Constants.FRAMERATE);

		List<Mode> modes = new ArrayList<>();
		PlayMode playMode = new PlayMode(this, width, height);
		modes.add(playMode);
		modes.add(new ObstaclesMode(this, playMode));
		modes.add(new MenuMode(this));
		modes.add(new SetupMode(this));

		assert status == null;
		for (Mode m : modes) {
			logic.put(m.getClass(), m);
		}
		status = this.logic.get(MenuMode.class);
		assert status != null;
	}

	@Override
	public void draw() {
		background(Constants.COLORBG);
		lights();

		status.update();
		status.draw();

		if (Constants.DEBUG) {
			fill(Constants.BLACK);
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
		status.mouseReleased();
	}
	@Override public void mousePressed() {
		status.mousePressed();
	}
	@Override public void keyReleased() {
		status.keyReleased();
	}
	@Override public void keyPressed() {
		status.keyPressed();
	}

}