package ch.epfl.planair.mods;

import ch.epfl.planair.Planair;
import ch.epfl.planair.meta.Constants;
import ch.epfl.planair.meta.Utils;
import ch.epfl.planair.scene.Background;
import ch.epfl.planair.scene.Plate;
import ch.epfl.planair.scene.Sphere;
import ch.epfl.planair.scene.scores.Projectable;
import ch.epfl.planair.scene.scores.Scoreboard;
import ch.epfl.planair.specs.Drawable;
import ch.epfl.planair.visual.WebcamProcessor;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;

public final class PlayMode extends Mode {

	private final WebcamProcessor cam;

	private final Sphere sphere;
	private final List<Drawable> obstacles;
	private final Plate plate;
	private final Scoreboard scoreboard;
	private final Background background;

	private PVector environmentRotation = Utils.nullVector();
	private float motionFactor;


	public PlayMode(PApplet p) {
		super(p);
		this.motionFactor = Constants.MOTION_FACTOR;
		this.obstacles = new ArrayList<>();

		this.sphere = new Sphere(p, new PVector(0, -Constants.PLATE_THICKNESS/2, 0), Constants.SPHERE_RADIUS);
		this.sphere.setXBounds(
				-Constants.PLATE_SIZE / 2 + Constants.SPHERE_RADIUS,
				Constants.PLATE_SIZE / 2 - Constants.SPHERE_RADIUS
		);
		this.sphere.setZBounds(
				-Constants.PLATE_SIZE / 2 + Constants.SPHERE_RADIUS,
				Constants.PLATE_SIZE / 2 - Constants.SPHERE_RADIUS
		);
		this.sphere.enableGravity();

		this.plate = new Plate(p, new PVector(0, 0, 0), Constants.PLATE_SIZE, Constants.PLATE_THICKNESS);

		this.scoreboard = new Scoreboard(p, p.width, Constants.SCOREBOARD_HEIGHT, sphere);
		this.scoreboard.addForProjection(plate);
		this.scoreboard.addForProjection(sphere);

		this.background = new Background(p);

		this.cam = new WebcamProcessor(p);
	}

	public <T extends Drawable & Projectable> void addObstacles(T o) {
		obstacles.add(o);
		scoreboard.addForProjection(o);
	}

	@Override
	public void update() {
		environmentRotation.set(cam.getRotation());
		sphere.setEnvironmentRotation(environmentRotation);
		sphere.update();
		sphere.checkCollisions(obstacles);
		plate.update();
		scoreboard.update();
	}

	protected Sphere sphere() {
		return sphere;
	}

	protected void drawMetaPlate(PVector envRot) {
		p.pushMatrix();
		p.rotateX(envRot.x);
		p.rotateY(envRot.y);
		p.rotateZ(envRot.z);
		plate.draw();
		sphere.draw();
		obstacles.forEach(Drawable::draw);
		p.popMatrix();
	}

	@Override
	public void draw() {
		p.noCursor();
		p.camera(0, - Constants.EYE_HEIGHT, (p.height / 2f) / PApplet.tan(PConstants.PI * 30f / 180f), 0, 0, 0, 0, 1, 0);
		background.draw();
		drawMetaPlate(environmentRotation);
		p.camera();
		scoreboard.draw();
	}

	public void mouseWheel(MouseEvent e) {
		motionFactor -= e.getCount() / 15.0f;
		motionFactor = Utils.trim(motionFactor, 0.2f, 2);
	}

	public void mouseDragged() {
		if (p.mouseY < p.height - Constants.SCOREBOARD_HEIGHT) {
			environmentRotation.x = Utils.trim(environmentRotation.x - motionFactor * (p.mouseY - p.pmouseY) / 100.0f, PConstants.THIRD_PI);
			environmentRotation.z = Utils.trim(environmentRotation.z + motionFactor * (p.mouseX - p.pmouseX) / 100.0f, PConstants.THIRD_PI);
		}
	}

	public void keyPressed() {
		switch (p.keyCode) {
			case 16: Planair.become(ObstaclesMode.class); break; // SHIFT
			case 27: Planair.become(MenuMode.class); p.key = 0; break; // ESC
		}
	}

}
