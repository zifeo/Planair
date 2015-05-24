package ch.epfl.planair.specs;

import java.util.ArrayList;

import ch.epfl.planair.scene.scores.Scorer;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * An object from which a score can be computed
 * Follows the Observer pattern
 */
public abstract class Scorable extends Accelerable {

    private final ArrayList<Scorer> scoreObservers;

    public Scorable(PApplet parent, PVector location) {
        super(parent, location);
        this.scoreObservers = new ArrayList<>();
    }

    /**
     * Add an observer of that object that computes a
     * score (scorer)
     * @param scorer the scorer to add
     */
    public void addScoreObserver(Scorer scorer) {
        scoreObservers.add(scorer);
    }

    /**
     * Notify to the scorers that the score may be
     * recomputed, by a certain factor.
     * @param delta the factor that influences the score
     */
    protected void notifyScore(int delta) {
        for (Scorer scorer : scoreObservers) {
            scorer.notifiedScore(delta);
        }
    }

}