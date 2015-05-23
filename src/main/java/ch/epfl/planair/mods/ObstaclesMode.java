package ch.epfl.planair.mods;

import ch.epfl.planair.Planair;
import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.meta.Utils;
import ch.epfl.planair.scene.Sphere;
import ch.epfl.planair.scene.Tree;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public final class ObstaclesMode extends Mode {

	private final Tree obstacles;
	private final PlayMode playMode;

	public ObstaclesMode(PApplet parent, PlayMode playMode) {
		super(parent);

		this.obstacles = new Tree(parent, new PVector(0, -Consts.PLATE_THICKNESS/2, 0));
		obstacles.setXBounds(
				-Consts.PLATE_SIZE / 2 + Consts.CYLINDER_RADIUS,
				Consts.PLATE_SIZE / 2 - Consts.CYLINDER_RADIUS
		);
		this.obstacles.setZBounds(
				-Consts.PLATE_SIZE / 2 + Consts.CYLINDER_RADIUS,
				Consts.PLATE_SIZE / 2 - Consts.CYLINDER_RADIUS
		);

		this.playMode = playMode;
	}

	@Override
	public void draw() {
		playMode.drawMetaPlate(Utils.nullVector());
		p.camera(0, -(p.height / 2f) / PApplet.tan(PConstants.PI * 30f / 180f), 0, 0, 0, 0, 0, 0, 1);

		obstacles.setLocation(new PVector(p.mouseX - p.width / 2, -Consts.PLATE_THICKNESS / 2, p.mouseY - p.height / 2));
		obstacles.draw();
	}

	@Override
	public void mousePressed() {
		Sphere sphere = playMode.sphere();
		PVector wantedLocation = obstacles.location();
		PVector sphereLocation = sphere.location();
		float angle = PVector.angleBetween(wantedLocation, sphereLocation);
		float distance = PVector.dist(wantedLocation, sphereLocation);
		float borders = obstacles.get2DDistanceFrom(angle) + sphere.get2DDistanceFrom(angle + PConstants.PI);

		if (distance > borders) {
			playMode.addObstacles(new Tree(obstacles));
		}
	}

	@Override
	public void keyReleased() {
		switch (p.keyCode) {
			case 16: Planair.become(PlayMode.class); break; // SHIFT
		}
	}

}
