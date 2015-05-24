package ch.epfl.planair.specs;

import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.meta.Utils;
import processing.core.PApplet;
import processing.core.PVector;

public abstract class Drawable {

    protected PApplet p;
    private PVector location = Utils.nullVector();

    public Drawable(PApplet p){
        this.p = p;
    }

    public Drawable(PApplet p, PVector location) {
        this.p = p;
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