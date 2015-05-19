package ch.epfl.planair.mods;

import ch.epfl.planair.Planair;
import ch.epfl.planair.config.Constants;
import ch.epfl.planair.config.Status;
import ch.epfl.planair.config.Utils;
import ch.epfl.planair.scene.Background;
import ch.epfl.planair.scene.Plate;
import ch.epfl.planair.scene.Sphere;
import ch.epfl.planair.scene.Tree;
import ch.epfl.planair.scores.Scoreboard;
import ch.epfl.planair.specs.Drawable;
import ch.epfl.planair.visual.WebcamProcessor;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

import java.util.ArrayList;

public final class PlayMode extends Mode {

	private final Sphere sphere;
	private final Plate plate;
	private final Tree shiftCylinder;
	//private final Cylinder shiftCylinder;
	private final ArrayList<Drawable> cylinders = new ArrayList<>();
	private final Scoreboard scoreboard;
	private final Background background;
	private float motionFactor = 1.5f;


	private final int width;
	private final int height;

	private PVector environmentRotation = Utils.nullVector();

	private final WebcamProcessor cam;

	public PlayMode(PApplet parent, int width, int heigth) {
		super(parent);
		PVector onPlate = new PVector(0, -Constants.PLATE_THICKNESS/2, 0);
		this.width = width;
		this.height = heigth;

		this.sphere = new Sphere(parent, onPlate, Constants.SPHERE_RADIUS);
		sphere.setXBounds(
				-Constants.PLATE_SIZE / 2 + Constants.SPHERE_RADIUS,
				Constants.PLATE_SIZE / 2 - Constants.SPHERE_RADIUS
		);
		this.sphere.setZBounds(
				-Constants.PLATE_SIZE / 2 + Constants.SPHERE_RADIUS,
				Constants.PLATE_SIZE / 2 - Constants.SPHERE_RADIUS
		);
		this.sphere.enableGravity();

		this.plate = new Plate(parent, new PVector(0, 0, 0), Constants.PLATE_SIZE, Constants.PLATE_THICKNESS);

		this.shiftCylinder = new Tree(parent, onPlate);
		//shiftCylinder = new Cylinder(this, onPlate, Constants.CYLINDER_RADIUS, Constants.CYLINDER_HEIGHT, Constants.CYLINDER_RESOLUTION);
		shiftCylinder.setXBounds(
				-Constants.PLATE_SIZE / 2 + Constants.CYLINDER_RADIUS,
				Constants.PLATE_SIZE / 2 - Constants.CYLINDER_RADIUS
		);
		this.shiftCylinder.setZBounds(
				-Constants.PLATE_SIZE / 2 + Constants.CYLINDER_RADIUS,
				Constants.PLATE_SIZE / 2 - Constants.CYLINDER_RADIUS
		);

		this.scoreboard = new Scoreboard(parent, width, Constants.SCOREBOARD_HEIGHT, sphere);
		this.scoreboard.addForProjection(plate);
		this.scoreboard.addForProjection(sphere);

		this.background = new Background(parent);

		this.cam = new WebcamProcessor(parent);
	}


	@Override
	public void draw() {
		parent.pushMatrix();
		parent.background(200);
		parent.lights();
		parent.camera(0, -Constants.EYE_HEIGHT, (height / 2.0f) / parent.tan(parent.PI * 30.0f / 180.0f), 0, 0, 0, 0, 1, 0);

		background.draw();

		PVector r = cam.getRotation();
		environmentRotation = r;

		parent.rotateX(environmentRotation.x);
		parent.rotateY(environmentRotation.y);
		parent.rotateZ(environmentRotation.z);
		sphere.setEnvironmentRotation(environmentRotation);

		sphere.update();
		sphere.checkCollisions(cylinders);
		plate.update();

		sphere.draw();
		plate.draw();
		for (Drawable cylinder : cylinders) {
			cylinder.draw();
		}

		parent.popMatrix();

		scoreboard.update();
		scoreboard.draw();
		System.out.println("draw ?");

	}

	@Override
	public void update() {
		System.out.println("updated ?");
	}

	public void mouseWheel(MouseEvent e) {
		motionFactor -= e.getCount() / 15.0f;
		motionFactor = Utils.trim(motionFactor, 0.2f, 2);
	}

	public void mouseDragged() {
		if (parent.mouseY < height - Constants.SCOREBOARD_HEIGHT) {
			environmentRotation.x = Utils.trim(environmentRotation.x - motionFactor * (parent.mouseY - parent.pmouseY) / 100.0f, Constants.PI_3);
			environmentRotation.z = Utils.trim(environmentRotation.z + motionFactor * (parent.mouseX - parent.pmouseX) / 100.0f, Constants.PI_3);
		}
	}

	public void keyPressed() {
		switch (parent.keyCode) {
			case 16: Planair.become(ObstaclesMode.class); break; // SHIFT
		}
	}

}
