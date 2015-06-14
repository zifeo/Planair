package ch.epfl.planair.modes;

import cs211.tangiblegame.TangibleGame;
import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.meta.PipelineConfig;
import ch.epfl.planair.meta.Utils;
import ch.epfl.planair.scene.Background;
import ch.epfl.planair.scene.Airplane;
import ch.epfl.planair.scene.Plate;
import ch.epfl.planair.scene.Sphere;
import ch.epfl.planair.scene.scores.Scoreboard;
import ch.epfl.planair.specs.Drawable;
import ch.epfl.planair.specs.Obstacle;
import ch.epfl.planair.visual.WebcamProcessor;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;
import processing.event.MouseEvent;
import processing.video.Capture;

import java.util.ArrayList;
import java.util.List;

/**
 * The main mode of the game. A 3D view of the plate
 * is presented. The user can control the angle of the plate
 * to move the ball and try to hit obstacles.
 */
public class PlayMode extends Mode {

	private final WebcamProcessor daemon;

	private final Sphere sphere;
	private final List<Obstacle> obstacles;
	private final Plate plate;
	private final Scoreboard scoreboard;
	private final Background background;

	private final Airplane airplane;

	private PVector environmentRotation = Utils.nullVector();
	private float motionFactor;
	private int countObstacles;
	private int doubleKillTimeLeft;
	private boolean tangible;

	public PlayMode(PApplet p, Capture webcam, PipelineConfig config) {
		super(p);
		this.motionFactor = Consts.MOTION_FACTOR;
		this.obstacles = new ArrayList<>();

		this.sphere = new Sphere(p, new PVector(0, -Consts.PLATE_THICKNESS/2, 0), Consts.SPHERE_RADIUS);
		this.sphere.setXBounds(
				-Consts.PLATE_SIZE / 2 + Consts.SPHERE_RADIUS,
				Consts.PLATE_SIZE / 2 - Consts.SPHERE_RADIUS
		);
		this.sphere.setZBounds(
				-Consts.PLATE_SIZE / 2 + Consts.SPHERE_RADIUS,
				Consts.PLATE_SIZE / 2 - Consts.SPHERE_RADIUS
		);
		this.sphere.enableGravity();
		this.plate = new Plate(p, Utils.nullVector(), Consts.PLATE_SIZE, Consts.PLATE_THICKNESS);
		this.scoreboard = new Scoreboard(p, p.width, Consts.SCOREBOARD_HEIGHT, sphere);
		this.scoreboard.addForProjection(plate);
		this.scoreboard.addForProjection(sphere);
		this.background = new Background(p);
		this.airplane = new Airplane(p);
		this.daemon = new WebcamProcessor(p, webcam, config);
		this.countObstacles = 0;
		this.doubleKillTimeLeft = 0;
		this.tangible = true;
	}

	@Override
	public void update() {
		if (tangible) {
			environmentRotation.set(daemon.rotation());
		}
		sphere.setEnvironmentRotation(environmentRotation);
		sphere.update();
		sphere.checkCollisions(obstacles);
		plate.update();
		scoreboard.update();
		airplane.update();
		doubleKillTimeLeft = doubleKillTimeLeft <= 0 ? 0: doubleKillTimeLeft - 1;
	}

	@Override
	public void draw() {
		p.camera(0, -Consts.EYE_HEIGHT, (p.height / 2f) / PApplet.tan(PConstants.PI * 30f / 180f), 0, 0, 0, 0, 1, 0);
		background.draw();
		airplane.draw();
		drawMetaPlate(environmentRotation);
		p.camera();
		scoreboard.draw();
        PImage image = this.daemon.get();
		p.scale(0.5f);
        p.image(image,0,0);
	}

	public void addObstacles(Obstacle o) {
		obstacles.add(o);
		scoreboard.addForProjection(o);
	}

	public void removeObstacle(Obstacle o) {
		soundsEvent();
		obstacles.remove(o);
		scoreboard.removeProjection(o);
	}

	private void soundsEvent() {
		countObstacles += 1;

		if (countObstacles % 5 == 0) {
			TangibleGame.music().triggerRampage();
		} else if (doubleKillTimeLeft > 0){
			TangibleGame.music().triggerDoubleKill();
			doubleKillTimeLeft = 0;
		} else if (countObstacles == 1) {
			TangibleGame.music().triggerFirstBlood();
		} else {
			// Adds 180 frames (~ 3 secs) before which a double kill can be triggered
			doubleKillTimeLeft = 180;
		}
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

	private void toggleInteractionMode() {
		exited();
		tangible = !tangible;
		entered();
	}

	@Override
	public void entered() {
		mouseMoved();
		countObstacles = 0;
		doubleKillTimeLeft = 0;
		if (tangible) {
			daemon.start();
		}
	}

	@Override
	public void exited() {
		if (tangible) {
			daemon.stop();
		}
	}

	@Override
	public void mouseWheel(MouseEvent e) {
		motionFactor -= e.getCount() / 15.0f;
		motionFactor = Utils.trim(motionFactor, 0.2f, 2);
	}

	@Override
	public void mouseDragged() {
		if (p.mouseY < p.height - Consts.SCOREBOARD_HEIGHT) {
			environmentRotation.x = Utils.trim(environmentRotation.x - motionFactor * (p.mouseY - p.pmouseY) / 100.0f, PConstants.THIRD_PI);
			environmentRotation.z = Utils.trim(environmentRotation.z + motionFactor * (p.mouseX - p.pmouseX) / 100.0f, PConstants.THIRD_PI);
		}
	}

	@Override
	public void mouseMoved() {
		if (p.mouseY - p.height + Consts.SCOREBOARD_HEIGHT > 0) {
			p.cursor();
		} else {
			p.noCursor();
		}
	}

	@Override
	public void keyPressed() {
		switch (p.keyCode) {
			case 16: TangibleGame.become(ObstaclesMode.class); break; // SHIFT
			case 27: TangibleGame.become(MenuMode.class); p.key = 0; break; // ESC
			case 17: toggleInteractionMode(); break; // CTRL
		}
	}

}