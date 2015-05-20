package ch.epfl.planair;

import ch.epfl.planair.config.Constants;
import ch.epfl.planair.config.Status;
import ch.epfl.planair.config.Utils;
import ch.epfl.planair.scores.Scoreboard;
import ch.epfl.planair.specs.Drawable;
import ch.epfl.planair.visual.WebcamProcessor;
import processing.core.*;
import ch.epfl.planair.scene.*;
import processing.event.MouseEvent;

import java.util.ArrayList;

public class Planair extends PApplet {

    private Status status = Status.PLAY;
    private PVector environmentRotation = new PVector(0, 0, 0);
    private float motionFactor = 1.5f;

    private WebcamProcessor cam;

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

	@Override
    public void setup() {
        size(Constants.WINDOWS_WIDTH, Constants.WINDOWS_HEIGHT, P3D);
        frameRate(Constants.FRAMERATE);

        PVector onPlate = new PVector(0, -Constants.PLATE_THICKNESS/2, 0);

        sphere = new Sphere(this, onPlate, Constants.SPHERE_RADIUS);
        sphere.setXBounds(
		        -Constants.PLATE_SIZE / 2 + Constants.SPHERE_RADIUS,
		        Constants.PLATE_SIZE / 2 - Constants.SPHERE_RADIUS
        );
        sphere.setZBounds(
		        -Constants.PLATE_SIZE / 2 + Constants.SPHERE_RADIUS,
		        Constants.PLATE_SIZE / 2 - Constants.SPHERE_RADIUS
        );
        sphere.enableGravity();

        plate = new Plate(this, new PVector(0, 0, 0), Constants.PLATE_SIZE, Constants.PLATE_THICKNESS);

        shiftCylinder = new Tree(this, onPlate);
        //shiftCylinder = new Cylinder(this, onPlate, Constants.CYLINDER_RADIUS, Constants.CYLINDER_HEIGHT, Constants.CYLINDER_RESOLUTION);
        shiftCylinder.setXBounds(
		        -Constants.PLATE_SIZE / 2 + Constants.CYLINDER_RADIUS,
		        Constants.PLATE_SIZE / 2 - Constants.CYLINDER_RADIUS
        );
        shiftCylinder.setZBounds(
                -Constants.PLATE_SIZE / 2 + Constants.CYLINDER_RADIUS,
                Constants.PLATE_SIZE / 2 - Constants.CYLINDER_RADIUS
        );

        scoreboard = new Scoreboard(this, width, Constants.SCOREBOARD_HEIGHT, sphere);
        scoreboard.addForProjection(plate);
        scoreboard.addForProjection(sphere);

        background = new Background(this);

        cam = new WebcamProcessor(this);
    }

    @Override
    public void draw() {
        pushMatrix();
        background(200);
        lights();

        switch (status) {
            case PLAY:
                camera(0, -Constants.EYE_HEIGHT, (height / 2.0f) / tan(PI * 30.0f / 180.0f), 0, 0, 0, 0, 1, 0);

                background.draw();

                PVector r = cam.getRotation();
                environmentRotation.x = r.x;
                environmentRotation.y = r.z;
                environmentRotation.z = -r.y;

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

                shiftCylinder.setLocation(new PVector(mouseX - width/2, -Constants.PLATE_THICKNESS/2, mouseY - height/2));
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

	    if (Constants.DEBUG) {
		    fill(color(0));
		    textSize(11f);
		    text(String.format("fps: %.1f   motion factor: %.1f", frameRate, motionFactor), 2, 13);
	    }
    }

    public void mouseWheel(MouseEvent e) {
        motionFactor -= e.getCount() / 15.0f;
        motionFactor = Utils.trim(motionFactor, 0.2f, 2);
    }

    public void mouseDragged() {
        if (mouseY < height - Constants.SCOREBOARD_HEIGHT) {
            environmentRotation.x = Utils.trim(environmentRotation.x - motionFactor * (mouseY - pmouseY) / 100.0f, Constants.PI_3);
            environmentRotation.z = Utils.trim(environmentRotation.z + motionFactor * (mouseX - pmouseX) / 100.0f, Constants.PI_3);
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
