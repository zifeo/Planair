package ch.epfl.planair.specs;

import java.util.ArrayList;

import ch.epfl.planair.scores.Scorer;
import processing.core.PApplet;
import processing.core.PVector;

public abstract class Scorable extends Accelerable {

    private final ArrayList<Scorer> scoreObservers;

    public Scorable(PApplet parent, PVector location) {
        super(parent, location);
        this.scoreObservers = new ArrayList<Scorer>();
    }

    public void addScoreObserver(Scorer scorer) {
        scoreObservers.add(scorer);
    }

    protected void notifyScore(int delta) {
        for (Scorer scorer : scoreObservers) {
            scorer.notifiedScore(delta);
        }
    }

}