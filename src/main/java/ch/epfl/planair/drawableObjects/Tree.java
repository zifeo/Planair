package ch.epfl.planair.drawableObjects;

import processing.core.PShape;
import processing.core.PVector;
import processing.core.PApplet;
import processing.core.PGraphics;

public final class Tree extends Movable implements Projectable {

    private PShape shape;
    private final float scale = 40;
    private final float radius = 15;

    public Tree(PApplet parent, PVector location) {
        super(parent, location);
        this.shape = createTree();
    }

    public Tree(Tree that) {
        this(that.parent, that.location());
        this.setXBounds(that.xMinBound(), that.xMaxBound());
        this.setYBounds(that.yMinBound(), that.yMaxBound());
        this.setZBounds(that.zMinBound(), that.zMaxBound());
    }

    public void draw() {
        parent.pushMatrix();
        PVector location = location();
        parent.translate(location.x, location.y, location.z);
        parent.shape(shape);
        drawAxes();
        parent.popMatrix();
    }

    public float get2DDistanceFrom(float angle) {
        return radius;
    }

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

    private PShape createTree() {
        PShape tree = parent.loadShape("simpleTree.obj");
        tree.scale(scale);
        tree.rotate((float)Math.PI);
        return tree;
    }
}

