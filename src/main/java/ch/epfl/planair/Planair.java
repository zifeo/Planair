package ch.epfl.planair;

import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.meta.PipelineConfig;
import ch.epfl.planair.modes.*;
import ch.epfl.planair.music.MusicPlayer;
import processing.core.PApplet;
import processing.event.MouseEvent;
import processing.video.Capture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Planair main class.
 * Game loading, mode dependence setup, events forwarding and ticks.
 *
 * Introduction to Visual computing class 2015
 * Contact: firstname.surname@epfl.ch
 *
 * @author Nicolas Casademont
 * @author Timothée Lottaz
 * @author Teo Stocco
 *
 * @version 1.0
 */
public class Planair extends PApplet {

	private static Mode status;
	private static Planair self;
	private static MusicPlayer player;

	private final Map<Class<? extends Mode>, Mode> semantic;
	private final Capture webcam;

	static {
		status = null;
		self = null;
		player = null;
	}

	/** Switch mode. */
	public static void become(Class<? extends Mode> mode) {
		status.exited();
		status = self.semantic.get(mode);
		assert status != null;
		status.entered();
	}

	/** Music player */
	public static MusicPlayer music() {
		assert player != null;
		return player;
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
		if (Capture.list().length == 0) {
			println("No webcam available!");
			exit();
		}
		this.webcam = new Capture(this, Consts.CAMERA_WIDTH, Consts.CAMERA_HEIGHT, Consts.CAMERA_FPS);
		this.semantic = new HashMap<>();
	}

	/** Mode & Processing init. */
	@Override
	public void setup() {
		assert status == null;
		assert player == null;
		size(displayWidth, displayHeight, P3D);
		frameRate(Consts.FRAMERATE);

		// Fix for webcam (Nico)
		webcam.start();

		try {
			List<Mode> modes = new ArrayList<>();

			/* ADD MODES BELOW */
			PipelineConfig config = new PipelineConfig();
			PlayMode playMode = new PlayMode(this, webcam, config);
			modes.add(playMode);
			modes.add(new ObstaclesMode(this, playMode));
			modes.add(new MenuMode(this));
			modes.add(new SetupMode(this, webcam, config));

			/* DEFAULT MODE LOADED */
			Class<? extends Mode> defaultMode = MenuMode.class;

			modes.forEach(m -> semantic.put(m.getClass(), m));
			status = semantic.get(defaultMode);
			assert status != null;

			player = new MusicPlayer(this);
			player.playBackgroundMusic();
		} catch (Exception e) {
			println(e.getMessage());
			if (Consts.DEBUG) {
				e.printStackTrace();
			}
			exit();
		}
	}

	/** Draw and updates tick. */
	@Override
	public void draw() {
		background(Consts.COLORBG);
		lights();
		status.tick();

		if (Consts.DEBUG) {
			camera();
			fill(Consts.BLACK);
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
