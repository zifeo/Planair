package ch.epfl.planair.specs;

import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.meta.Utils;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * An object that can be displayed (drawn) on the screen.
 */
public abstract class Drawable {

    protected PApplet p;
    private PVector location;

    public Drawable(PApplet p){
        this.p = p;
        this.location = Utils.nullVector();
    }

    public Drawable(PApplet p, PVector location) {
        this.p = p;
        this.location = new PVector(location.x, location.y, location.z);
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
    public void update() {}

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
        if (Consts.DEBUG) {
            p.textSize(15);
            p.noStroke();
            p.fill(0, 200, 0);
            p.box(1, 250, 1);
            p.text("Y", -4, 140, 0);
            p.fill(200, 0, 0);
            p.box(250, 1, 1);
            p.text("X", 130, 6, 0);
            p.fill(0, 0, 200);
            p.box(1, 1, 250);
            p.text("Z", -4, 0, 130);
        }
    }
}