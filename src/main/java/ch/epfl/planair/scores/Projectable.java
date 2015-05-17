package ch.epfl.planair.scores;

import processing.core.PGraphics;

/**
 * All objects that are projected on a PGraphic canvas such as a mini-map.
 */
public interface Projectable {

    /**
     * Project the object on the given PGraphics canvas.
     *
     * @param graphic the PGraphics canvas
     */
    void projectOn(PGraphics graphic);

}