package ch.epfl.planair.scene;

import ch.epfl.planair.specs.Movable;
import ch.epfl.planair.scene.scores.Projectable;
import processing.core.*;

public final class Tree extends Movable implements Projectable {

    private PShape shape;
    private final float scale = 5;
    private final float radius = 10;

    public Tree(PApplet parent, PVector location) {
        super(parent, location);
        this.shape = createTree();
    }

    public Tree(Tree that) {
        this(that.p, that.location());
        this.setXBounds(that.xMinBound(), that.xMaxBound());
        this.setYBounds(that.yMinBound(), that.yMaxBound());
        this.setZBounds(that.zMinBound(), that.zMaxBound());
    }

    public void draw() {
        p.pushMatrix();
        PVector location = location();
        p.translate(location.x, location.y, location.z);
        p.shape(shape);
        drawAxes();
        p.popMatrix();
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
        PShape tree = p.loadShape("3D/treeLight.obj");
        tree.scale(scale);
        tree.rotate(PConstants.PI);
        return tree;
    }
}

