package ch.epfl.planair.specs;

import ch.epfl.planair.Planair;
import ch.epfl.planair.config.Utils;
import processing.core.PApplet;
import processing.core.PVector;

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

    public PVector location() {
        return location.get();
    }

    public void setLocation(PVector location) {
        this.location.set(location);
    }

    public void update() {}

    public abstract void draw();

    public float get2DDistanceFrom(float angle) {
        return 0;
    }

    protected void drawAxes() {
        if (Planair.DEBUG) {
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