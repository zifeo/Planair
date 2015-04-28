package ch.epfl.planair;

import ch.epfl.planair.config.Status;
import ch.epfl.planair.config.Utils;
import ch.epfl.planair.scores.Scoreboard;
import ch.epfl.planair.specs.Drawable;
import processing.core.*;
import ch.epfl.planair.scene.*;
import processing.event.MouseEvent;

import java.util.ArrayList;

public class Planair extends PApplet {

    // @todo : to put in JSON config loader
    public static final boolean DEBUG              = true;
    public static final int PLATE_SIZE             = 250;
    public static final int PLATE_THICKNESS        = 10;
    public static final int SPHERE_RADIUS          = 10;
    public static final int CYLINDER_HEIGHT        = 20;
    public static final int CYLINDER_RADIUS        = 15;
    public static final int CYLINDER_RESOLUTION    = 8;
    public static final int SCOREBOARD_HEIGHT      = 100;
    public static final int WINDOWS_WIDTH          = 500;
    public static final int WINDOWS_HEIGHT         = 600;
    public static final int FRAMERATE              = 60;
    public static final float PI_3                 = PI/3;
    public static final int EYE_HEIGHT             = 200;

    private Status status = Status.PLAY;
    private PVector environmentRotation = new PVector(0, 0, 0);
    private float motionFactor = 1.5f;

    private Sphere sphere;
    private Plate plate;
    private Tree shiftCylinder;
    //private Cylinder shiftCylinder;
    private ArrayList<Drawable> cylinders = new ArrayList<Drawable>();
    private Scoreboard scoreboard;
    private Background background;

    public static void main(String args[]) {
        String[] appletArgs = new String[] { "ch.epfl.planair.Planair" };
        if (args != null) {
            PApplet.main(concat(appletArgs, args));
        } else {
            PApplet.main(appletArgs);
        }
    }

    public void setup() {
        size(WINDOWS_WIDTH, WINDOWS_HEIGHT, P3D);
        frameRate(FRAMERATE);

        PVector onPlate = new PVector(0, -PLATE_THICKNESS/2, 0);

        sphere = new Sphere(this, onPlate, SPHERE_RADIUS);
        sphere.setXBounds(-PLATE_SIZE / 2 + SPHERE_RADIUS, PLATE_SIZE / 2 - SPHERE_RADIUS);
        sphere.setZBounds(-PLATE_SIZE / 2 + SPHERE_RADIUS, PLATE_SIZE / 2 - SPHERE_RADIUS);
        sphere.enableGravity();

        plate = new Plate(this, new PVector(0, 0, 0), PLATE_SIZE, PLATE_THICKNESS);

        shiftCylinder = new Tree(this, onPlate);
        //shiftCylinder = new Cylinder(this, onPlate, CYLINDER_RADIUS, CYLINDER_HEIGHT, CYLINDER_RESOLUTION);
        shiftCylinder.setXBounds(-PLATE_SIZE / 2 + CYLINDER_RADIUS, PLATE_SIZE / 2 - CYLINDER_RADIUS);
        shiftCylinder.setZBounds(-PLATE_SIZE / 2 + CYLINDER_RADIUS, PLATE_SIZE / 2 - CYLINDER_RADIUS);

        scoreboard = new Scoreboard(this, width, SCOREBOARD_HEIGHT, sphere);
        scoreboard.addForProjection(plate);
        scoreboard.addForProjection(sphere);

        background = new Background(this);
    }

    public void draw() {
        pushMatrix();
        background(200);
        lights();

        switch (status) {
            case PLAY:
                camera(0, -EYE_HEIGHT, (height/2.0f) / tan(PI*30.0f / 180.0f), 0, 0, 0, 0, 1, 0);

                background.draw();

                rotateX(environmentRotation.x);
                rotateY(environmentRotation.y);
                rotateZ(environmentRotation.z);
                sphere.setEnvironmentRotation(environmentRotation);

                sphere.update();
                sphere.checkCollisions(cylinders);
                plate.update();
                break;

            case ADD_CYLINDER:
                camera(0, -(height/2.0f) / tan(PI*30.0f / 180.0f), 0, 0, 0, 0, 0, 0, 1);

                shiftCylinder.setLocation(new PVector(mouseX - width/2, -PLATE_THICKNESS/2, mouseY - height/2));
                shiftCylinder.draw();
                break;
        }

        sphere.draw();
        plate.draw();
        for (Drawable cylinder : cylinders) {
            cylinder.draw();
        }
        popMatrix();

        scoreboard.update();
        scoreboard.draw();

        fill(color(0));
        textSize(11f);
        text(String.format("fps: %.1f   motion factor: %.1f", frameRate, motionFactor), 2, 13);
    }

    public void mouseWheel(MouseEvent e) {
        motionFactor -= e.getCount() / 15.0f;
        motionFactor = Utils.trim(motionFactor, 0.2f, 2);
    }

    public void mouseDragged() {
        if (mouseY < height - SCOREBOARD_HEIGHT) {
            environmentRotation.x = Utils.trim(environmentRotation.x - motionFactor * (mouseY - pmouseY) / 100.0f, PI_3);
            environmentRotation.z = Utils.trim(environmentRotation.z + motionFactor * (mouseX - pmouseX) / 100.0f, PI_3);
        }
    }

    public void mousePressed() {
        if (status == Status.ADD_CYLINDER) {
            PVector wantedLocation = shiftCylinder.location();
            PVector sphereLocation = sphere.location();
            float angle = PVector.angleBetween(wantedLocation, sphereLocation);
            float distance = PVector.dist(wantedLocation, sphereLocation);
            float borders = shiftCylinder.get2DDistanceFrom(angle) + sphere.get2DDistanceFrom(angle + PI);

            if (distance > borders) {
                //Cylinder obstacle = new Cylinder(shiftCylinder);
                Tree obstacle = new Tree(shiftCylinder);
                cylinders.add(obstacle);
                scoreboard.addForProjection(obstacle);
            }
        }
    }

    public void keyReleased() {
        switch (keyCode) {
            case SHIFT:
                status = Status.PLAY;
        }
    }

    public void keyPressed() {
        switch (keyCode) {
            case SHIFT:
                status = Status.ADD_CYLINDER;
        }
    }
}
