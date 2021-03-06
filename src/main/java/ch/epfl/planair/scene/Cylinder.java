package ch.epfl.planair.scene;

import ch.epfl.planair.modes.PlayMode;
import ch.epfl.planair.specs.Obstacle;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.core.PShape;

public final class Cylinder extends Obstacle {

    private final PShape shape;
    private final float radius;
    private final float cylinderHeight;

    public Cylinder(PApplet parent, PVector location, float radius, float cylinderHeight, int cylinderResolution, PlayMode playMode) {
        super(parent, location, playMode);
        this.shape = createCylinder(radius, cylinderHeight, cylinderResolution);
        this.radius = radius;
        this.cylinderHeight = cylinderHeight;
    }

    public Cylinder(Cylinder that) {
        super(that);
        this.shape = that.shape;
        this.radius = that.radius;
        this.cylinderHeight = that.cylinderHeight;
        this.setXBounds(that.xMinBound(), that.xMaxBound());
        this.setYBounds(that.yMinBound(), that.yMaxBound());
        this.setZBounds(that.zMinBound(), that.zMaxBound());
    }

	@Override
	public void draw() {
        p.pushMatrix();
        PVector location = location();
        p.translate(location.x, location.y, location.z);
        p.shape(shape);
        drawAxes();
        p.popMatrix();
    }

    @Override
    public float get2DDistanceFrom(float angle) {
        return radius;
    }

	@Override
    public void projectOn(PGraphics graphic) {

        graphic.fill(220);
        graphic.noStroke();

        PVector location = location();
        float widthOrigin = xMaxBound() - xMinBound() + 2 * radius;
        float heightOrigin = zMaxBound() - zMinBound() + 2 * radius;

        float radiusScaled = radius / widthOrigin * graphic.width;
        float xScaled = (location.x - xMinBound() + 2 * radius) / widthOrigin * graphic.width;
        float yScaled = (location.z - zMinBound() + 2 * radius) / heightOrigin * graphic.height;

        graphic.ellipse(xScaled - radiusScaled, yScaled - radiusScaled, 2 * radiusScaled, 2 * radiusScaled);
    }

    private PShape createCylinder(float radius, float cylinderHeight, int cylinderResolution) {
        PShape cylinder = p.createShape(PApplet.GROUP);
        PShape floor = p.createShape();
        PShape tube = p.createShape();
        PShape ceiling = p.createShape();

        float angle;
        float[] x = new float[cylinderResolution + 1];
        float[] y = new float[cylinderResolution + 1];

        for (int i = 0; i < x.length; ++i) {
            angle = (float)(2*Math.PI) / cylinderResolution * i;
            x[i] = (float)Math.sin(angle) * radius;
            y[i] = (float)Math.cos(angle) * radius;
        }

        tube.beginShape(PApplet.QUAD_STRIP);
        tube.noFill();
        for (int i = 0; i < x.length; ++i) {
            tube.vertex(x[i], y[i] , 0);
            tube.vertex(x[i], y[i], cylinderHeight);
        }
        tube.endShape();

        floor.beginShape(PApplet.TRIANGLE_FAN);
        ceiling.beginShape(PApplet.TRIANGLE_FAN);
        floor.noFill();
        ceiling.noFill();
        floor.vertex(0, 0, 0);
        ceiling.vertex(0, 0, cylinderHeight);
        for (int i = 0; i < x.length; ++i) {
            floor.vertex(x[i], y[i] , 0);
            ceiling.vertex(x[i], y[i], cylinderHeight);
        }
        floor.endShape();
        ceiling.endShape();

        cylinder.addChild(floor);
        cylinder.addChild(tube);
        cylinder.addChild(ceiling);
        cylinder.rotateX((float)(0.5*Math.PI));

        return cylinder;
    }
}