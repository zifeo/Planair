package ch.epfl.planair.scene;

import processing.core.PApplet;
import processing.core.PShape;

public class Background {

    protected PApplet parent;

    private final float scale = 70;
    private PShape shape;

    public Background(PApplet parent) {
        this.parent = parent;
        shape = createBackgroundShape();
    }

    public void draw() {
        parent.pushMatrix();
        parent.translate(50, 100, -200);
        parent.shape(shape);
        parent.popMatrix();
    }

    private PShape createBackgroundShape() {
        PShape shape = parent.loadShape("background_scene.obj");
        shape.scale(scale);
        shape.rotate(PApplet.PI);
        shape.rotateY(-PApplet.PI / 4.0f);
        return shape;
    }

}
