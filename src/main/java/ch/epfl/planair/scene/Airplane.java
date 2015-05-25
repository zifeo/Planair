package ch.epfl.planair.scene;

import processing.core.PApplet;
import processing.core.PShape;

public final class Airplane {

    private final PApplet parent;

    private final float scale = 30;
    private final PShape shape;

    private final float flightHeight = 400;
    private final float flightRadius = 2000f;

    // The flight angle in radians
    private double flightAngle = 0.0;

    // The amount of radians to increment on each update
    private double deltaAngle = 0.03;

    public Airplane(PApplet parent) {
        this.parent = parent;
        this.shape = createBackgroundShape();
    }

    public void draw() {
        parent.pushMatrix();
        parent.translate((float) (flightRadius * Math.cos(flightAngle)), -flightHeight, (float) (flightRadius * Math.sin(flightAngle)));
        parent.shape(shape);
        parent.popMatrix();
    }

    public void update() {
        if (flightAngle > PApplet.TWO_PI) {
            flightAngle -= PApplet.TWO_PI;
        }
        flightAngle += deltaAngle;
        shape.rotateY((float) -deltaAngle);
    }

    private PShape createBackgroundShape() {
        PShape shape = parent.loadShape("airplane.obj");
        shape.scale(scale);
        shape.rotate(PApplet.PI);
        shape.rotateY(PApplet.HALF_PI);
        return shape;
    }

}