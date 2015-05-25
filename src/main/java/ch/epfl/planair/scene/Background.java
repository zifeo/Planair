package ch.epfl.planair.scene;

import processing.core.PApplet;
import processing.core.PShape;

public final class Background {

    private PApplet parent;

    private final float scale = 180;
    private final PShape shape;

    public Background(PApplet parent) {
        this.parent = parent;
        this.shape = createBackgroundShape();
    }

    public void draw() {
        parent.pushMatrix();
        parent.translate(50, 100, -400);
        parent.shape(shape);
        parent.popMatrix();
    }

    private PShape createBackgroundShape() {
        PShape shape = parent.loadShape("3D/background_scene.obj");
        shape.scale(scale);
        shape.rotate(PApplet.PI);
        shape.rotateY(-PApplet.PI / 4.0f);
        return shape;
    }

}
