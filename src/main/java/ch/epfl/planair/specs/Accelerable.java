package ch.epfl.planair.specs;

import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.meta.Utils;
import processing.core.PVector;
import processing.core.PApplet;

public abstract class Accelerable extends Movable {

    private PVector force = Utils.nullVector();
    private PVector environmentRotation = Utils.nullVector();
    private boolean computeGravity = false;

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
            force.x = Consts.ACCELERABLE_G * (float)Math.sin(environmentRotation.z);
            // force.y = 0;
            force.z = - Consts.ACCELERABLE_G * (float)Math.sin(environmentRotation.x);
        }
    }

    public void update() {

        applyGravity();
        PVector friction = velocity();
        friction.mult(-1);
        friction.normalize();
        friction.setMag(Consts.ACCELERABLE_NORMAL_FORCE * Consts.ACCELERABLE_MU);

        PVector newVelocity = velocity();
        newVelocity.add(force);
        newVelocity.add(friction);
        setVelocity(newVelocity);

        super.update();
    }

}