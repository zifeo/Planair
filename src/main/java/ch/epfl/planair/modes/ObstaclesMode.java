package ch.epfl.planair.modes;

import ch.epfl.planair.Planair;
import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.meta.Utils;
import ch.epfl.planair.scene.Sphere;
import ch.epfl.planair.scene.Tree;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

/**
 * The mode where a top-view of the plate is displayed,
 * where the user can add obstacles to the terrain.
 */
public final class ObstaclesMode extends Mode {

	private final Tree obstacleHolder;
	private final PlayMode playMode;

	public ObstaclesMode(PApplet parent, PlayMode playMode) {
		super(parent);

		this.obstacleHolder = new Tree(parent, new PVector(0, -Consts.PLATE_THICKNESS/2, 0), playMode);
		obstacleHolder.setXBounds(
				-Consts.PLATE_SIZE / 2 + Consts.HOLDER_RADIUS,
				Consts.PLATE_SIZE / 2 - Consts.HOLDER_RADIUS
		);
		this.obstacleHolder.setZBounds(
				-Consts.PLATE_SIZE / 2 + Consts.HOLDER_RADIUS,
				Consts.PLATE_SIZE / 2 - Consts.HOLDER_RADIUS
		);

		this.playMode = playMode;
	}

	@Override
	public void draw() {
		//p.camera(0, - Consts.EYE_HEIGHT, (p.height / 2f) / PApplet.tan(PConstants.PI * 30f / 180f), 0, 0, 0, 0, 1, 0);
		p.camera(0, -(p.height / 2f) / PApplet.tan(PConstants.PI * 30f / 180f), 0, 0, 0, 0, 0, 0, 1);
		playMode.drawMetaPlate(Utils.nullVector());
		obstacleHolder.draw();
	}

	@Override
	public void mousePressed() {
		Sphere sphere = playMode.sphere();
		PVector wantedLocation = obstacleHolder.location();
		PVector sphereLocation = sphere.location();
		float angle = PVector.angleBetween(wantedLocation, sphereLocation);
		float distance = PVector.dist(wantedLocation, sphereLocation);
		float borders = obstacleHolder.get2DDistanceFrom(angle) + sphere.get2DDistanceFrom(angle + PConstants.PI);

		if (distance > borders) {
			playMode.addObstacles(new Tree(obstacleHolder));
		}
	}

	@Override
	public void mouseMoved() {
		obstacleHolder.setLocation(new PVector(p.mouseX - p.width / 2, -Consts.PLATE_THICKNESS / 2, p.mouseY - p.height / 2));
	}

	@Override
	public void keyReleased() {
		switch (p.keyCode) {
			case 16: Planair.become(PlayMode.class); break; // SHIFT
		}
	}

}
