package ch.epfl.planair.scene.scores;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.planair.Planair;
import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.scene.ScrollBar;
import ch.epfl.planair.specs.Drawable;
import ch.epfl.planair.specs.Scorable;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * A scoreboard divided in three area:
 * - mini-map
 * - text-based facts
 * - score timelime
 */
public final class Scoreboard extends Drawable implements Scorer {

    private final PGraphics overlay;
    private final PGraphics projection;
    private final PGraphics facts;
    private final PGraphics barChart;
    private final PGraphics slider;
    private final Scorable scoreTrack;
    private final ScrollBar scrollbar;
    private final List<Projectable> toProject;
    private final List<Float> scores;

    private final int dt;
    private final int moduleSize;

    private int timeChart;
    private int time;
    private float totalScore;
    private float lastScore;
    private float maxScore;
    private float currentVelocity;

    /**
     * Create a scoreboard with given size and score source.
     *
     * @param p
     * @param overlayWidth scoreboard width
     * @param overlayHeight scoreboard height
     * @param scoreTrack score source
     */
    public Scoreboard(PApplet p, int overlayWidth, int overlayHeight, Scorable scoreTrack) {
        super(p, new PVector(0, p.height - overlayHeight, 0));
        this.dt = 2 * (int) p.frameRate;
        this.moduleSize = overlayHeight - 2 * Consts.SCOREBOARD_PADDING;
        this.overlay = p.createGraphics(overlayWidth, overlayHeight, PApplet.P2D);
        this.projection = p.createGraphics(moduleSize, moduleSize, PApplet.P2D);
        this.facts = p.createGraphics(moduleSize, moduleSize, PApplet.P2D);
        this.barChart = p.createGraphics(
		        overlayWidth - 2 * moduleSize - 4 * Consts.SCOREBOARD_PADDING,
		        moduleSize - Consts.SCOREBOARD_PADDING - Consts.SCROLL_HEIGHT,
		        PApplet.P2D
        );
        this.slider = p.createGraphics(this.barChart.width, Consts.SCROLL_HEIGHT, PApplet.P2D);
        this.toProject = new ArrayList<>();
        this.scores = new ArrayList<>();
        this.scores.add(0f);
        this.scoreTrack = scoreTrack;
        this.scoreTrack.addScoreObserver(this);
        PVector location = location();
        this.scrollbar = new ScrollBar(this.slider,
                location.x + 2 * moduleSize + 3 * Consts.SCOREBOARD_PADDING,
                location.y + 2 * Consts.SCOREBOARD_PADDING + this.barChart.height);
        this.timeChart = (int) Math.floor(scrollbar.pos() * Consts.SCOREBOARD_TIME_CHART_BASE);
        this.time = 0;
	    this.totalScore = 0;
	    this.lastScore = 0;
	    this.maxScore = 0;
	    this.currentVelocity = 0;
    }

	@Override
    public void update() {
        if (p.frameCount % 5 == 0) {
            currentVelocity = scoreTrack.velocity().mag();
        }
        if (p.frameCount % dt == 0) {
            scores.add(0f);
            ++time;
        }
        scrollbar.update();
        timeChart = (int) Math.floor(scrollbar.pos() * Consts.SCOREBOARD_TIME_CHART_BASE);
    }


	@Override
    public void draw() {
		drawMiniMap();
		drawFacts();
		drawBarChart();
		drawSlider();
		drawOverlay();
        p.noLights();
        p.image(overlay, 0, p.height - overlay.height);
        p.lights();
    }

    /** Add object on the mini-map. */
    public void addForProjection(Projectable item) {
        toProject.add(item);
    }

    public void removeProjection(Projectable item) {
        toProject.remove(item);
    }

    @Override
    public void notifiedScore(int delta) {
        lastScore = delta * scoreTrack.velocity().mag();
	    if (lastScore > 25) {
		    Planair.music().triggerHeadshot();
	    }
        totalScore += lastScore;
        scores.set(time, scores.get(time) + lastScore);

        if (totalScore < 0) {
            totalScore = 0;
        }
        if (scores.get(time) > maxScore) {
            maxScore = scores.get(time);
        }
    }

	private void drawOverlay() {
		overlay.beginDraw();
		overlay.background(220);
		overlay.image(projection, Consts.SCOREBOARD_PADDING, Consts.SCOREBOARD_PADDING);
		overlay.image(facts, moduleSize + 2 * Consts.SCOREBOARD_PADDING, Consts.SCOREBOARD_PADDING);
		overlay.image(barChart, 2 * moduleSize + 3 * Consts.SCOREBOARD_PADDING, Consts.SCOREBOARD_PADDING);
		overlay.image(slider, 2 * moduleSize + 3 * Consts.SCOREBOARD_PADDING, 2 * Consts.SCOREBOARD_PADDING + barChart.height);
		overlay.endDraw();
	}

    private void drawMiniMap() {
        projection.beginDraw();
        for (Projectable item : toProject) {
            item.projectOn(projection);
        }
        projection.endDraw();
    }

    private void drawFacts() {
        facts.beginDraw();
        facts.textSize(Consts.SCOREBOARD_FONT_SIZE);
        facts.textLeading(Consts.SCOREBOARD_FONT_HEIGHT);
        facts.fill(50);
        facts.background(220);
        facts.text("Total Score\n" + Math.floor(totalScore * 10), 0, Consts.SCOREBOARD_FONT_SIZE);
        facts.text("Velocity\n" + Math.floor(currentVelocity * 10), 0, moduleSize / 3 + Consts.SCOREBOARD_FONT_SIZE);
        facts.text("Last Score\n" + Math.floor(lastScore * 10), 0, 2 * moduleSize / 3 + Consts.SCOREBOARD_FONT_SIZE);
        facts.endDraw();
    }

    private void drawBarChart() {
        barChart.beginDraw();
        barChart.noStroke();
        barChart.fill(220);
        barChart.background(200);
        scrollbar.draw();

        float size = barChart.width / timeChart;
        float max = (barChart.height - Consts.SCOREBOARD_PADDING) / size;
        float scorePerBlock = maxScore / max;
        int delay = Math.max(scores.size() - timeChart, 0);

        for (int t = delay; t < scores.size(); ++t) {
            for (int s = 0; s < scores.get(t) / scorePerBlock; ++s) {
                barChart.rect((t - delay) * size, barChart.height - (s + 1) * size, size - 1, size - 1);
            }
        }
        barChart.endDraw();
    }

    private void drawSlider() {
        slider.beginDraw();
        scrollbar.draw();
        slider.endDraw();
    }

}