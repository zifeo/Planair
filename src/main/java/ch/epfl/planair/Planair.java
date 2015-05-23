package ch.epfl.planair;

import ch.epfl.planair.meta.Constants;
import ch.epfl.planair.mods.*;
import processing.core.PApplet;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Planair main class.
 * Game loading, mode dependence setup, events forwarding and ticks.
 *
 * Visual computing class 2015
 * Contact: firstname.surname@epfl.ch
 *
 * @author Nicolas Casademont
 * @author Timothée Lottaz
 * @author Teo Stocco
 *
 * @version 1.0
 */
public class Planair extends PApplet {

	private static Mode status = null;
	private static Planair self = null;

	private final Map<Class<? extends Mode>, Mode> semantic;

	/** Switch mode. */
	public static void become(Class<? extends Mode> mode) {
		status = self.semantic.get(mode);
		assert status != null;
	}

	/** Start game & Processing. */
	public static void main(String args[]) {
		String[] appletArgs = new String[] { "ch.epfl.planair.Planair" };
		PApplet.main(args != null ? concat(appletArgs, args): appletArgs);
	}

	/** Basic init. */
	public Planair() {
		assert self == null;
		self = this;
		this.semantic = new HashMap<>();
	}

	/** Mode & Processing init. */
	@Override
	public void setup() {
		size(displayWidth, displayHeight, P3D);
		frameRate(Constants.FRAMERATE);

		try {
			List<Mode> modes = new ArrayList<>();

			/* ADD MODES BELOW */
			PlayMode playMode = new PlayMode(this);
			modes.add(playMode);
			modes.add(new ObstaclesMode(this, playMode));
			modes.add(new MenuMode(this));
			modes.add(new SetupMode(this));
			/* ADD MODES ABOVE */

			assert status == null;
			for (Mode m : modes) {
				semantic.put(m.getClass(), m);
			}
			status = this.semantic.get(MenuMode.class);
			assert status != null;

		} catch (Exception e) {
			println(e.getMessage());
			if (Constants.DEBUG) {
				e.printStackTrace();
			}
		}
	}

	/** Draw and updates tick. */
	@Override
	public void draw() {
		background(Constants.COLORBG);
		lights();
		status.tick();

		if (Constants.DEBUG) {
			camera();
			fill(Constants.BLACK);
			textSize(11f);
			text(String.format("fps: %.1f", frameRate), 4, 13);
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
