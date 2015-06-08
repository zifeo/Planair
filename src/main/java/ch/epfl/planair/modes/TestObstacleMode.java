package ch.epfl.planair.modes;

import ch.epfl.planair.Planair;
import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.meta.Utils;
import ch.epfl.planair.scene.Sphere;
import ch.epfl.planair.scene.Tree;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

/**
 * The mode where a top-view of the plate is displayed,
 * where the user can add obstacles to the terrain.
 */
public final class TestObstacleMode extends ObstaclesMode {

    public TestObstacleMode(PApplet parent, PlayMode playMode) {
        super(parent, playMode);
    }

    @Override
    public void keyReleased() {
        switch (p.keyCode) {
            case 16: Planair.become(TestMode.class); break; // SHIFT
        }
    }

}
