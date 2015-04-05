package drawableObjects;

import processing.core.PVector;
import processing.core.PApplet;

public abstract class Accelerable extends Movable {

    private PVector force = new PVector(0, 0, 0);
    private PVector environmentRotation = new PVector(0, 0, 0);
    private boolean computeGravity = false;

    // A déplacer dans une class properties
    private final float normalForce = 1;
    private final float G = 0.1f;
    private final float MU = 0.03f;

    public Accelerable(PApplet parent, PVector location) {
        super(parent, location);
    }

    public void enableGravity() {
        computeGravity = true;
    }

    public void disableGravity() {
        computeGravity = false;
    }

    public void setEnvironmentRotation(PVector rotation) {
        this.environmentRotation.set(rotation);
    }

    private void applyGravity() {
        if (computeGravity) {
            force.x = G * (float)Math.sin(environmentRotation.z);
            // force.y = 0;
            force.z = - G * (float)Math.sin(environmentRotation.x);
        }
    }

    public void update() {

        applyGravity();
        PVector friction = velocity();
        friction.mult(-1);
        friction.normalize();
        friction.setMag(normalForce * MU);

        PVector newVelocity = velocity();
        newVelocity.add(force);
        newVelocity.add(friction);
        setVelocity(newVelocity);

        super.update();
    }

}