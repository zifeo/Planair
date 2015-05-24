package ch.epfl.planair.specs;

import ch.epfl.planair.meta.Constants;
import ch.epfl.planair.meta.Utils;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * An object that can be displayed (drawn) on the screen.
 */
public abstract class Drawable {

    protected PApplet parent;
    private PVector location = Utils.nullVector();

    public Drawable(PApplet parent){
        this.parent = parent;
    }

    public Drawable(PApplet parent, PVector location) {
        this.parent = parent;
        this.location.set(location);
    }

    /**
     * Returns the location of the object
     * @return the location vector
     */
    public PVector location() {
        return location.get();
    }

    /**
     * Sets the current location to a new location
     * @param location the new location
     */
    public void setLocation(PVector location) {
        this.location.set(location);
    }

    /**
     * Updates the location and parameters of the object
     * over time. Does not necessarily need to do anything.
     */
    public void update() {

    }

    /**
     * Displays the object on the screen
     */
    public abstract void draw();

    /**
     *
     * @param angle
     * @return
     */
    public float get2DDistanceFrom(float angle) {
        return 0;
    }

    /**
     * Draws the x, y and z axes when DEBUG is true
     */
    protected void drawAxes() {
        if (Constants.DEBUG) {
            parent.textSize(15);
            parent.noStroke();
            parent.fill(0, 200, 0);
            parent.box(1, 250, 1);
            parent.text("Y", -4, 140, 0);
            parent.fill(200, 0, 0);
            parent.box(250, 1, 1);
            parent.text("X", 130, 6, 0);
            parent.fill(0, 0, 200);
            parent.box(1, 1, 250);
            parent.text("Z", -4, 0, 130);
        }
    }
}