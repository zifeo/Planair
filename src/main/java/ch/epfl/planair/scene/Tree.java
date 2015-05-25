package ch.epfl.planair.scene;

import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.modes.PlayMode;
import ch.epfl.planair.specs.Obstacle;
import processing.core.*;

public final class Tree extends Obstacle {

    private final PShape shape;
    private final float scale = 5;

    public Tree(PApplet parent, PVector location, PlayMode playMode) {
        super(parent, location, playMode);
        this.shape = createTree();
    }

    public Tree(Tree that) {
        super(that);
        this.shape = that.shape;
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
        return Consts.TREE_RADIUS;
    }

    @Override
    public void projectOn(PGraphics graphic) {

        graphic.fill(220);
        graphic.noStroke();

        PVector location = location();
        float widthOrigin = xMaxBound() - xMinBound() + 2 * Consts.TREE_RADIUS;
        float heightOrigin = zMaxBound() - zMinBound() + 2 * Consts.TREE_RADIUS;

        float radiusScaled = Consts.TREE_RADIUS / widthOrigin * graphic.width;
        float xScaled = (location.x - xMinBound() + 2 * Consts.TREE_RADIUS) / widthOrigin * graphic.width;
        float yScaled = (location.z - zMinBound() + 2 * Consts.TREE_RADIUS) / heightOrigin * graphic.height;

        graphic.ellipse(xScaled - radiusScaled, yScaled - radiusScaled, 12 * radiusScaled, 12 * radiusScaled);
    }

    private PShape createTree() {
        PShape tree = p.loadShape("3D/treeLight.obj");
        tree.scale(scale);
        tree.rotate(PConstants.PI);
        return tree;
    }
}

