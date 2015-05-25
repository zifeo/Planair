package ch.epfl.planair.specs;

import ch.epfl.planair.meta.Utils;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.LinkedList;
import java.util.List;

/**
 * An object that can be moved, thus having a velocity.
 * Also, it's possible to specify boundaries that the object
 * can't cross.
 */
public abstract class Movable extends Drawable {

    private PVector velocity = Utils.nullVector();
    private PVector maxBounds = Utils.maxVector();
    private PVector minBounds = Utils.minVector();

    public Movable(PApplet parent, PVector location) {
        super(parent, location);
    }

    /**
     * @return the velocity of the object
     */
    public PVector velocity() {
        return velocity.get();
    }

    /**
     * Sets the velocity of the object
     * @param velocity the velocity to set
     */
    public void setVelocity(PVector velocity) {
        this.velocity.set(velocity);
    }

    @Override
    public void setLocation(PVector location) {
        checkBounds(location);
    }

    @Override
    public void update() {
        checkBounds(PVector.add(location(), velocity));
    }

    /**
     * Sets the boundaries in the x-axis
     * @param min the min boundary
     * @param max the max boundary
     */
    public void setXBounds(float min, float max) {
        this.minBounds.x = min;
        this.maxBounds.x = max;
    }

    /**
     * Sets the boundaries in the y-axis
     * @param min the min boundary
     * @param max the max boundary
     */
    public void setYBounds(float min, float max) {
        this.minBounds.y = min;
        this.maxBounds.y = max;
    }

    /**
     * Sets the boundaries in the z-axis
     * @param min the min boundary
     * @param max the max boundary
     */
    public void setZBounds(float min, float max) {
        this.minBounds.z = min;
        this.maxBounds.z = max;
    }

    public float xMinBound() {
        return minBounds.x;
    }

    public float yMinBound() {
        return minBounds.y;
    }

    public float zMinBound() {
        return minBounds.z;
    }

    public float xMaxBound() {
        return maxBounds.x;
    }

    public float yMaxBound() {
        return maxBounds.y;
    }

    public float zMaxBound() {
        return maxBounds.z;
    }

    /**
     * Checks if the objects collides with some obstacles and
     * bounce against them if necessary.
     * @param obstacles the list of obstacles
     * @return the count of the obstacles that were bounced
     * against
     */
    public int checkCollisions(List<Obstacle> obstacles) {
        int count = 0;
        PVector location = location();
        PVector correction = new PVector(0, 0, 0);
        List<Obstacle> removingList = new LinkedList<>();

        // Check collision for each obstacle
        for (Obstacle obstacle: obstacles) {

            PVector obstacleLocation = obstacle.location();
            float angle = PVector.angleBetween(location, obstacleLocation);
            PVector delta = PVector.sub(location, obstacleLocation);
            float borders = get2DDistanceFrom(angle) + obstacle.get2DDistanceFrom(angle + (float)Math.PI);

            // If there is a collision with that obstacle
            if (delta.mag() < borders) {
                PVector normal = PVector.sub(location, obstacleLocation);
                normal.normalize();
                normal.mult(2 * PVector.dot(velocity, normal));

                /* Simulate an elastic collision by substracting the normal vector
                of the positions to the velocity */
                velocity.sub(normal);

                delta.normalize();
                delta.setMag(borders);
                correction.add(PVector.add(obstacleLocation, delta));
                removingList.add(obstacle);
                ++count;
            }
        }

        removingList.forEach(Obstacle::remove);

        // If there was a collision
        if (count > 0) {
            correction.x /= count;
            correction.y = location.y;
            correction.z /= count;

            /* Put the object back at the position where it does not
              cross the other object anymore */
            setLocation(correction);
        }
        return count;
    }

    /**
     * If the object if going out of bounds, make it bounce against it.
     * @param location the location of the object
     * @return the number of bounds that were bounced on
     */
    protected int checkBounds(PVector location) {
        int count = 0;
        if (location.x < minBounds.x) {
            location.x = minBounds.x;
            velocity.x = Math.abs(velocity.x);
            ++count;
        } else if (location.x > maxBounds.x) {
            location.x = maxBounds.x;
            velocity.x = -Math.abs(velocity.x);
            ++count;
        }
        if (location.y < minBounds.y) {
            location.y = minBounds.y;
            velocity.y = Math.abs(velocity.y);
            ++count;
        } else if (location.y > maxBounds.y) {
            location.y = maxBounds.y;
            velocity.y = -Math.abs(velocity.y);
            ++count;
        }
        if (location.z < minBounds.z) {
            location.z = minBounds.z;
            velocity.z = Math.abs(velocity.z);
            ++count;
        } else if (location.z > maxBounds.z) {
            location.z = maxBounds.z;
            velocity.z = -Math.abs(velocity.z);
            ++count;
        }
        super.setLocation(location);
        return count;
    }
}