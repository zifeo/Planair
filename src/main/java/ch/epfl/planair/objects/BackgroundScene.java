package ch.epfl.planair.objects;

import processing.core.PApplet;
import processing.core.PShape;

public class BackgroundScene {
    protected PApplet parent;

    private final float scale = 70;
    private PShape shape;

    public BackgroundScene(PApplet parent) {
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
        PShape shape = parent.loadShape("environment.obj");
        shape.scale(scale);
        shape.rotate(parent.PI);
        shape.rotateY((float) (-parent.PI / 4.0));
        return shape;
    }


}
