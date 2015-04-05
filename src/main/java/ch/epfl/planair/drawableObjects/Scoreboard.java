package ch.epfl.planair.drawableObjects;

import java.util.ArrayList;
import processing.core.*;

public final class Scoreboard extends Drawable implements Scorer {

    private final PGraphics overlay;
    private final ArrayList<Projectable> toProject;
    private final PGraphics projection;
    private final PGraphics facts;
    private final PGraphics barChart;
    private final PGraphics slider;
    private final Scorable scoreTrack;
    private final ScrollBar scrollbar;

    private final int PADDING = 5;
    private final int FONT_SIZE = 11;
    private final int FONT_HEIGHT = 15;
    private final int DT;
    private final int TIME_CHART_BASE = 45;
    private final int SCROLL_HEIGHT = 10;
    private final int MODULE_SIZE;

    private ArrayList<Float> scores;
    private int time;
    private int timeChart;
    private float totalScore;
    private float lastScore;
    private float maxScore;
    private float currentVelocity;

    public Scoreboard(PApplet parent, int overlayWidth, int overlayHeight, Scorable scoreTrack) {
        super(parent, new PVector(0, parent.height - overlayHeight, 0));
        this.DT = 2 * (int)parent.frameRate;
        this.MODULE_SIZE = overlayHeight - 2 * PADDING;
        this.overlay = parent.createGraphics(overlayWidth, overlayHeight, PApplet.P2D);
        this.projection = parent.createGraphics(MODULE_SIZE, MODULE_SIZE, PApplet.P2D);
        this.facts = parent.createGraphics(MODULE_SIZE, MODULE_SIZE, PApplet.P2D);
        this.barChart = parent.createGraphics(overlayWidth - 2 * MODULE_SIZE - 4 * PADDING, MODULE_SIZE - PADDING - SCROLL_HEIGHT, PApplet.P2D);
        this.slider = parent.createGraphics(this.barChart.width, SCROLL_HEIGHT, PApplet.P2D);
        this.toProject = new ArrayList<Projectable>();
        this.scores = new ArrayList<Float>();
        this.scores.add(0.0f);
        this.totalScore = 0;
        this.maxScore = 0;
        this.lastScore = 0;
        this.time = 0;
        this.currentVelocity = 0;
        this.scoreTrack = scoreTrack;
        this.scoreTrack.addScoreObserver(this);
        PVector location = location();
        this.scrollbar = new ScrollBar(parent,
                location.x + 2 * MODULE_SIZE + 3 * PADDING,
                location.y + 2 * PADDING + this.barChart.height,
                this.slider);
        this.timeChart = (int)Math.floor(scrollbar.pos() * TIME_CHART_BASE);
    }

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

    public void update() {

        if (parent.frameCount % 5 == 0) {
            currentVelocity = scoreTrack.velocity().mag();
        }

        if (parent.frameCount % DT == 0) {
            scores.add(0.0f);
            ++time;
        }

        scrollbar.update();
        timeChart = (int)Math.floor(scrollbar.pos() * TIME_CHART_BASE);

        prepareProjection();
        prepareFacts();
        prepareBarChart();
        prepareSlider();
    }

    public void addForProjection(Projectable item) {
        toProject.add(item);
    }

    public void prepareProjection() {
        projection.beginDraw();
        for (Projectable item : toProject) {
            item.projectOn(projection);
        }
        projection.endDraw();
    }

    public void prepareFacts() {
        facts.beginDraw();
        facts.textSize(FONT_SIZE);
        facts.textLeading(FONT_HEIGHT);
        facts.fill(50);
        facts.background(220);
        facts.text("Total Score\n"+Math.floor(totalScore * 10), 0, FONT_SIZE);
        facts.text("Velocity\n"+Math.floor(currentVelocity * 10), 0, MODULE_SIZE / 3 + FONT_SIZE);
        facts.text("Last Score\n"+Math.floor(lastScore * 10), 0, 2 * MODULE_SIZE / 3 + FONT_SIZE);
        facts.endDraw();
    }

    public void prepareBarChart() {
        barChart.beginDraw();
        barChart.noStroke();
        barChart.fill(220);
        barChart.background(200);
        scrollbar.draw();
        float size = barChart.width / timeChart;
        float max = (barChart.height - PADDING) / size;
        float scorePerBlock = maxScore / max;
        int delay = Math.max(scores.size() - timeChart, 0);
        for (int t = delay; t < scores.size(); ++t) {
            for (int s = 0; s < scores.get(t) / scorePerBlock; ++s) {
                barChart.rect((t - delay) * size, barChart.height - (s + 1) * size, size - 1, size - 1);
            }
        }
        barChart.endDraw();
    }

    public void prepareSlider() {
        slider.beginDraw();
        scrollbar.draw();
        slider.endDraw();
    }

    public void draw() {
        parent.pushMatrix();
        parent.noLights();

        overlay.beginDraw();
        overlay.background(220);
        overlay.image(projection, PADDING, PADDING);
        overlay.image(facts, MODULE_SIZE + 2 * PADDING, PADDING);
        overlay.image(barChart, 2 * MODULE_SIZE + 3 * PADDING, PADDING);
        overlay.image(slider, 2 * MODULE_SIZE + 3 * PADDING, 2 * PADDING + barChart.height);
        overlay.endDraw();

        parent.image(overlay, 0, parent.height - overlay.height);

        parent.lights();
        parent.popMatrix();
    }

}