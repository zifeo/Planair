package ch.epfl.planair.modes;

import ch.epfl.planair.Planair;
import ch.epfl.planair.meta.PipelineConfig;
import ch.epfl.planair.visual.MovieCaptureAdaptor;
import processing.core.PApplet;
import processing.video.Capture;
import processing.video.Movie;

/**
 * The main mode of the game. A 3D view of the plate
 * is presented. The user can control the angle of the plate
 * to move the ball and try to hit obstacles.
 */
public final class TestMode extends PlayMode {

    public TestMode(PApplet p, Capture webcam, PipelineConfig config) {
        super(p, webcam, config);
    }

	@Override
	public void keyPressed() {
		super.keyPressed();
		switch (p.keyCode) {
			case 16: Planair.become(TestObstacleMode.class); break; // SHIFT
		}
	}

}