package ch.epfl.planair.modes;

import ch.epfl.planair.Planair;
import ch.epfl.planair.meta.Constants;
import ch.epfl.planair.meta.Utils;
import ch.epfl.planair.scene.Background;
import ch.epfl.planair.scene.Airplane;
import ch.epfl.planair.scene.Plate;
import ch.epfl.planair.scene.Sphere;
import ch.epfl.planair.scene.scores.Projectable;
import ch.epfl.planair.scene.scores.Scoreboard;
import ch.epfl.planair.specs.Drawable;
import ch.epfl.planair.visual.WebcamProcessor;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * The main mode of the game. A 3D view of the plate
 * is presented. The user can control the angle of the plate
 * to move the ball and try to hit obstacles.
 */
public final class PlayMode extends Mode {

	private final Sphere sphere;
	private final Plate plate;

	private final List<Drawable> obstacles;
	private final Scoreboard scoreboard;
	private final Background background;
	private final Airplane airplane;
	private float motionFactor = 1.5f;

	private final int width;
	private final int height;

	private PVector environmentRotation = Utils.nullVector();

	private final WebcamProcessor cam;

	public PlayMode(PApplet parent, int width, int heigth) {
		super(parent);
		this.width = width;
		this.height = heigth;
		this.obstacles = new ArrayList<>();
		this.sphere = new Sphere(parent, new PVector(0, -Constants.PLATE_THICKNESS/2, 0), Constants.SPHERE_RADIUS);
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

		this.scoreboard = new Scoreboard(parent, width, Constants.SCOREBOARD_HEIGHT, sphere);
		this.scoreboard.addForProjection(plate);
		this.scoreboard.addForProjection(sphere);

		this.background = new Background(parent);
		this.airplane = new Airplane(parent);

		this.cam = new WebcamProcessor(parent);
	}

	public <T extends Drawable & Projectable> void addObstacles(T o) {
		obstacles.add(o);
		scoreboard.addForProjection(o);
	}

	@Override
	public void update() {
		PVector r = cam.getRotation();
		environmentRotation.x = r.x;
		environmentRotation.y = r.z;
		environmentRotation.z = - r.z;
		sphere.setEnvironmentRotation(environmentRotation);
		sphere.update();
		sphere.checkCollisions(obstacles);
		plate.update();
		scoreboard.update();
		airplane.update();
	}

	protected void rotateEnvironment() {
		p.rotateX(environmentRotation.x);
		p.rotateY(environmentRotation.y);
		p.rotateZ(environmentRotation.z);
	}

	protected Sphere sphere() {
		return sphere;
	}

	protected void drawMetaPlate() {
		p.pushMatrix();
		rotateEnvironment();
		sphere.draw();
		plate.draw();

		for (Drawable cylinder : obstacles) {
			cylinder.draw();
		}
		p.popMatrix();
	}

	@Override
	public void draw() {
		p.noCursor();
		p.camera(0, -Constants.EYE_HEIGHT, (height / 2.0f) / p.tan(p.PI * 30.0f / 180.0f), 0, 0, 0, 0, 1, 0);
		background.draw();
		airplane.draw();
		drawMetaPlate();
		scoreboard.draw();
	}

	@Override
	public void mouseWheel(MouseEvent e) {
		motionFactor -= e.getCount() / 15.0f;
		motionFactor = Utils.trim(motionFactor, 0.2f, 2);
	}

	@Override
	public void mouseDragged() {
		if (p.mouseY < height - Constants.SCOREBOARD_HEIGHT) {
			environmentRotation.x = Utils.trim(environmentRotation.x - motionFactor * (p.mouseY - p.pmouseY) / 100.0f, Constants.PI_3);
			environmentRotation.z = Utils.trim(environmentRotation.z + motionFactor * (p.mouseX - p.pmouseX) / 100.0f, Constants.PI_3);
		}
	}

	@Override
	public void keyPressed() {
		switch (p.keyCode) {
			case 16: Planair.become(ObstaclesMode.class); break; // SHIFT
			case 27: Planair.become(MenuMode.class); p.key = 0; break; // ESC
		}
	}

}
