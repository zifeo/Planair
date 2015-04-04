package drawableObjects;

import processing.core.PApplet;
import processing.core.PVector;
import java.util.ArrayList;

/**
 * Created by Nicolas on 04.04.15.
 */
public abstract class Movable extends Drawable {

    private PVector velocity = new PVector(0, 0, 0);
    private PVector maxBounds = new PVector(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
    private PVector minBounds = new PVector(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

    Movable(PApplet parent, PVector location) {
        super(parent, location);
    }

    public PVector velocity() {
        return velocity.get();
    }

    public void setVelocity(PVector velocity) {
        this.velocity.set(velocity);
    }

    public void setLocation(PVector location) {
        checkBounds(location);
    }

    public void update() {
        checkBounds(PVector.add(location(), velocity));
    }

    public void setXBounds(float min, float max) {
        this.minBounds.x = min;
        this.maxBounds.x = max;
    }

    public void setYBounds(float min, float max) {
        this.minBounds.y = min;
        this.maxBounds.y = max;
    }

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

    public int checkCollisions(ArrayList<Drawable> obstacles) {
        int count = 0;
        PVector location = location();
        PVector correction = new PVector(0, 0, 0);
        for (Drawable obstacle: obstacles) {

            PVector obstacleLocation = obstacle.location();
            float angle = PVector.angleBetween(location, obstacleLocation);
            PVector delta = PVector.sub(location, obstacleLocation);
            float borders = get2DDistanceFrom(angle) + obstacle.get2DDistanceFrom(angle + (float)Math.PI);

            if (delta.mag() < borders) {
                PVector normal = PVector.sub(location, obstacleLocation);
                normal.normalize();
                normal.mult(2 * PVector.dot(velocity, normal));
                velocity.sub(normal);

                delta.normalize();
                delta.setMag(borders);
                correction.add(PVector.add(obstacleLocation, delta));
                ++count;
            }
        }
        if (count > 0) {
            correction.x /= count;
            correction.y = location.y;
            correction.z /= count;
            setLocation(correction);
        }
        return count;
    }

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
            System.out.println("inf " + minBounds.y);
            location.y = minBounds.y;
            velocity.y = Math.abs(velocity.y);
            ++count;
        } else if (location.y > maxBounds.y) {
            System.out.println("sup" + location.y);
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