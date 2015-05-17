package ch.epfl.planair.scores;

import java.util.ArrayList;

import ch.epfl.planair.config.Constants;
import ch.epfl.planair.specs.Drawable;
import ch.epfl.planair.specs.Scorable;
import processing.core.*;

/**
 * A scoreboard divided in three area:
 * - mini-map
 * - text-based facts
 * - score timelime
 */
public final class Scoreboard extends Drawable implements Scorer {

    private final PGraphics overlay;
    private final ArrayList<Projectable> toProject;
    private final PGraphics projection;
    private final PGraphics facts;
    private final PGraphics barChart;
    private final PGraphics slider;
    private final Scorable scoreTrack;
    private final ScrollBar scrollbar;
    private final ArrayList<Float> scores;
    private final int dt;
    private final int moduleSize;

    private int timeChart;
    private int time = 0;
    private float totalScore = 0;
    private float lastScore = 0;
    private float maxScore = 0;
    private float currentVelocity = 0;

    /**
     * Create a scoreboard with given size and score source.
     *
     * @param parent
     * @param overlayWidth scoreboard width
     * @param overlayHeight scoreboard height
     * @param scoreTrack score source
     */
    public Scoreboard(PApplet parent, int overlayWidth, int overlayHeight, Scorable scoreTrack) {
        super(parent, new PVector(0, parent.height - overlayHeight, 0));

        this.dt = 2 * (int) parent.frameRate;
        this.moduleSize = overlayHeight - 2 * Constants.SCOREBOARD_PADDING;

        this.overlay = parent.createGraphics(overlayWidth, overlayHeight, PApplet.P2D);
        this.projection = parent.createGraphics(moduleSize, moduleSize, PApplet.P2D);
        this.facts = parent.createGraphics(moduleSize, moduleSize, PApplet.P2D);
        this.barChart = parent.createGraphics(
		        overlayWidth - 2 * moduleSize - 4 * Constants.SCOREBOARD_PADDING,
		        moduleSize - Constants.SCOREBOARD_PADDING - Constants.SCOREBOARD_SCROLL_HEIGHT,
		        PApplet.P2D
        );
        this.slider = parent.createGraphics(this.barChart.width, Constants.SCOREBOARD_SCROLL_HEIGHT, PApplet.P2D);

        this.toProject = new ArrayList<>();
        this.scores = new ArrayList<>();
        this.scores.add(0f);
        this.scoreTrack = scoreTrack;
        this.scoreTrack.addScoreObserver(this);

        PVector location = location();
        this.scrollbar = new ScrollBar(parent,
                location.x + 2 * moduleSize + 3 * Constants.SCOREBOARD_PADDING,
                location.y + 2 * Constants.SCOREBOARD_PADDING + this.barChart.height,
                this.slider);
        this.timeChart = (int) Math.floor(scrollbar.pos() * Constants.SCOREBOARD_TIME_CHART_BASE);
    }

    /** @inheritdoc */
    public void notifiedScore(int delta) {
        lastScore = delta * scoreTrack.velocity().mag();
        totalScore += lastScore;
        scores.set(time, scores.get(time) + lastScore);

        if (totalScore < 0) {
            totalScore = 0;
        }
        if (scores.get(time) > maxScore) {
            maxScore = scores.get(time);
        }
    }

    /** @inheritdoc */
    public void update() {

        if (parent.frameCount % 5 == 0) {
            currentVelocity = scoreTrack.velocity().mag();
        }

        if (parent.frameCount % dt == 0) {
            scores.add(0.0f);
            ++time;
        }

        scrollbar.update();
        timeChart = (int) Math.floor(scrollbar.pos() * Constants.SCOREBOARD_TIME_CHART_BASE);

        drawMiniMap();
        drawFacts();
        drawBarChart();
        drawSlider();
    }

    /** Add object on the mini-map. */
    public void addForProjection(Projectable item) {
        toProject.add(item);
    }

    /** @inheritdoc */
    public void draw() {
        parent.pushMatrix();
        parent.noLights();

        overlay.beginDraw();
        overlay.background(220);
        overlay.image(projection, Constants.SCOREBOARD_PADDING, Constants.SCOREBOARD_PADDING);
        overlay.image(facts, moduleSize + 2 * Constants.SCOREBOARD_PADDING, Constants.SCOREBOARD_PADDING);
        overlay.image(barChart, 2 * moduleSize + 3 * Constants.SCOREBOARD_PADDING, Constants.SCOREBOARD_PADDING);
        overlay.image(slider, 2 * moduleSize + 3 * Constants.SCOREBOARD_PADDING, 2 * Constants.SCOREBOARD_PADDING + barChart.height);
        overlay.endDraw();

        parent.image(overlay, 0, parent.height - overlay.height);

        parent.lights();
        parent.popMatrix();
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
        facts.textSize(Constants.SCOREBOARD_FONT_SIZE);
        facts.textLeading(Constants.SCOREBOARD_FONT_HEIGHT);
        facts.fill(50);
        facts.background(220);
        facts.text("Total Score\n" + Math.floor(totalScore * 10), 0, Constants.SCOREBOARD_FONT_SIZE);
        facts.text("Velocity\n" + Math.floor(currentVelocity * 10), 0, moduleSize / 3 + Constants.SCOREBOARD_FONT_SIZE);
        facts.text("Last Score\n" + Math.floor(lastScore * 10), 0, 2 * moduleSize / 3 + Constants.SCOREBOARD_FONT_SIZE);
        facts.endDraw();
    }

    private void drawBarChart() {
        barChart.beginDraw();
        barChart.noStroke();
        barChart.fill(220);
        barChart.background(200);
        scrollbar.draw();

        float size = barChart.width / timeChart;
        float max = (barChart.height - Constants.SCOREBOARD_PADDING) / size;
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