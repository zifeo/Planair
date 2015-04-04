package drawableObjects;

import java.util.ArrayList;

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
        parent.pushMatrix();
        parent.stroke(0);
        parent.fill(127);
        PVector location = location();
        parent.translate(location.x, location.y - radius, location.z);
        parent.sphere(radius);
        drawAxes();
        parent.popMatrix();
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