package ch.epfl.planair.specs;

import ch.epfl.planair.modes.PlayMode;
import ch.epfl.planair.scene.scores.Projectable;
import processing.core.PApplet;
import processing.core.PVector;

public abstract class Obstacle extends Movable implements Projectable{
    private final PlayMode playMode;

    public Obstacle(PApplet parent, PVector location, PlayMode playMode) {
        super(parent, location);
        this.playMode = playMode;
    }

    public Obstacle(Obstacle that) {
        this(that.p, that.location(), that.playMode);
    }

    public void remove(){
        this.playMode.removeObstacle(this);
    }
}
