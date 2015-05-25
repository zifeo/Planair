package ch.epfl.planair.specs;

import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.meta.Utils;
import processing.core.PVector;
import processing.core.PApplet;

/**
 * An object that can be affected by a force and thus be subject
 * to acceleration. Here the force is always the gravity force,
 * depending on the rotation of the environment.
 */
public abstract class Accelerable extends Movable {

    private PVector force;
    private PVector environmentRotation;
    private boolean computeGravity = false;

    public Accelerable(PApplet parent, PVector location) {
        super(parent, location);
        this.force = Utils.nullVector();
        this.environmentRotation = Utils.nullVector();
    }

    /**
     * Enables the effect of gravity on the object
     */
    public void enableGravity() {
        computeGravity = true;
    }

    /**
     * Disables the effect of gravity on the object
     */
    public void disableGravity() {
        computeGravity = false;
    }

    /**
     * Sets the internal environmentRotation to a new rotation vector
     * @param rotation the new rotation vector
     */
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

    @Override
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