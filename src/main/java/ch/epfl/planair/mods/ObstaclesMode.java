package ch.epfl.planair.mods;

import ch.epfl.planair.Planair;
import ch.epfl.planair.meta.Constants;
import ch.epfl.planair.scene.Sphere;
import ch.epfl.planair.scene.Tree;
import processing.core.PApplet;
import processing.core.PVector;

public final class ObstaclesMode extends Mode {

	private final Tree shiftCylinder;
	private final PlayMode playMode;
	//private final Cylinder shiftCylinder;

	public ObstaclesMode(PApplet parent, PlayMode playMode) {
		super(parent);

		this.shiftCylinder = new Tree(parent, new PVector(0, -Constants.PLATE_THICKNESS/2, 0));
		//shiftCylinder = new Cylinder(this, onPlate, Constants.CYLINDER_RADIUS, Constants.CYLINDER_HEIGHT, Constants.CYLINDER_RESOLUTION);
		shiftCylinder.setXBounds(
				-Constants.PLATE_SIZE / 2 + Constants.CYLINDER_RADIUS,
				Constants.PLATE_SIZE / 2 - Constants.CYLINDER_RADIUS
		);
		this.shiftCylinder.setZBounds(
				-Constants.PLATE_SIZE / 2 + Constants.CYLINDER_RADIUS,
				Constants.PLATE_SIZE / 2 - Constants.CYLINDER_RADIUS
		);

		this.playMode = playMode;
	}

	@Override
	public void draw() {
		playMode.drawMetaPlate();
		p.camera(0, -(p.height / 2.0f) / p.tan(p.PI * 30.0f / 180.0f), 0, 0, 0, 0, 0, 0, 1);

		shiftCylinder.setLocation(new PVector(p.mouseX - p.width/2, -Constants.PLATE_THICKNESS/2, p.mouseY - p.height/2));
		shiftCylinder.draw();
	}

	@Override
	public void mousePressed() {
		Sphere sphere = playMode.sphere();
		PVector wantedLocation = shiftCylinder.location();
		PVector sphereLocation = sphere.location();
		float angle = PVector.angleBetween(wantedLocation, sphereLocation);
		float distance = PVector.dist(wantedLocation, sphereLocation);
		float borders = shiftCylinder.get2DDistanceFrom(angle) + sphere.get2DDistanceFrom(angle + p.PI);

		if (distance > borders) {
			// new Cylinder(shiftCylinder);
			playMode.addObstacles(new Tree(shiftCylinder));
		}
	}

	@Override
	public void keyReleased() {
		switch (p.keyCode) {
			case 16: Planair.become(PlayMode.class); break; // SHIFT
		}
	}

}
