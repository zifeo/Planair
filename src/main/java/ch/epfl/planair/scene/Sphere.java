package ch.epfl.planair.scene;

import java.util.ArrayList;

import ch.epfl.planair.specs.Drawable;
import ch.epfl.planair.scene.scores.Projectable;
import ch.epfl.planair.specs.Scorable;
import processing.core.PApplet;
import processing.core.PVector;
import processing.core.PGraphics;

public final class Sphere extends Scorable implements Projectable {

    private final float radius;

    public Sphere(PApplet parent, PVector location, float radius) {
        super(parent, location);
        this.radius = radius;
    }

    public void draw() {
        p.pushMatrix();
        p.stroke(0);
        p.fill(127);
        PVector location = location();
        p.translate(location.x, location.y - radius, location.z);
        p.sphere(radius);
        drawAxes();
        p.popMatrix();
    }

    public void projectOn(PGraphics graphic) {
        graphic.fill(150, 0, 0);
        graphic.noStroke();

        PVector location = location();
        float widthOrigin = xMaxBound() - xMinBound() + 2 * radius;
        float heightOrigin = zMaxBound() - zMinBound() + 2 * radius;

        float radiusScaled = radius / widthOrigin * graphic.width;
        float xScaled = (location.x - xMinBound() + 2 * radius) / widthOrigin * graphic.width;
        float yScaled = (location.z - zMinBound() + 2 * radius) / heightOrigin * graphic.height;

        graphic.ellipse(xScaled - radiusScaled, yScaled - radiusScaled, 2 * radiusScaled, 2 * radiusScaled);
    }

    @Override
    public float get2DDistanceFrom(float angle) {
        return radius;
    }

    public int checkCollisions(ArrayList<Drawable> obstacles) {
        int count = super.checkCollisions(obstacles);
        if (count != 0) {
            notifyScore(count);
        }
        return count;
    }

    protected int checkBounds(PVector location) {
        int count = super.checkBounds(location);
        if (count != 0) {
            notifyScore(-count);
        }
        return count;
    }

}