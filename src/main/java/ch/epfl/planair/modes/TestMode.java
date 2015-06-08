package ch.epfl.planair.modes;

import ch.epfl.planair.Planair;
import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.meta.PipelineConfig;
import ch.epfl.planair.meta.Utils;
import ch.epfl.planair.scene.Background;
import ch.epfl.planair.scene.Airplane;
import ch.epfl.planair.scene.Plate;
import ch.epfl.planair.scene.Sphere;
import ch.epfl.planair.scene.scores.Scoreboard;
import ch.epfl.planair.specs.Drawable;
import ch.epfl.planair.specs.Obstacle;
import ch.epfl.planair.visual.MovieCaptureAdaptor;
import ch.epfl.planair.visual.MovieProcessor;
import ch.epfl.planair.visual.WebcamProcessor;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import processing.event.MouseEvent;
import processing.video.Capture;
import processing.video.Movie;

import java.util.ArrayList;
import java.util.List;

/**
 * The main mode of the game. A 3D view of the plate
 * is presented. The user can control the angle of the plate
 * to move the ball and try to hit obstacles.
 */
public final class TestMode extends PlayMode {


    public TestMode(PApplet p, Movie webcam, PipelineConfig config) {
        super(p, new MovieCaptureAdaptor(p, webcam), config);
    }
}