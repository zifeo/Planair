package ch.epfl.planair.modes;

import cs211.tangiblegame.TangibleGame;
import processing.core.PApplet;

/**
 * The mode where a top-view of the plate is displayed,
 * where the user can add obstacles to the terrain.
 */
public final class TestObstacleMode extends ObstaclesMode {

    public TestObstacleMode(PApplet parent, TestMode playMode) {
        super(parent, playMode);
    }

    @Override
    public void keyReleased() {
        switch (p.keyCode) {
            case 16: TangibleGame.become(TestMode.class); break; // SHIFT
        }
    }

}
