package ch.epfl.planair.scores;

/**
 * All objects that implements a score counter.
 */
public interface Scorer {

    /**
     * Get a notification for scoring.
     *
     * @param delta amount
     */
    public abstract void notifiedScore(int delta);

}
