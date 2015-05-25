package ch.epfl.planair.scene;

import ch.epfl.planair.specs.Drawable;
import ch.epfl.planair.scene.scores.Projectable;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public final class Plate extends Drawable implements Projectable {

    private final int size;
    private final int thickness;

    public Plate(PApplet parent, PVector location, int size, int thickness) {
        super(parent, location);
        this.size = size;
        this.thickness = thickness;
    }

    @Override
    public void draw() {
        p.pushMatrix();
        p.noStroke();
        p.fill(153, 153, 102);
        PVector location = location();
        p.translate(location.x, location.y, location.z);
        p.box(size, thickness, size);
        drawAxes();
        p.popMatrix();
    }

    @Override
    public void projectOn(PGraphics graphic) {
        graphic.noStroke();
        graphic.fill(100, 50);
        graphic.rect(0, 0, graphic.width, graphic.height);
    }
}