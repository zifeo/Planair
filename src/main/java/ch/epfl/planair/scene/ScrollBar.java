package ch.epfl.planair.scene;

import ch.epfl.planair.meta.Utils;
import ch.epfl.planair.specs.Drawable;
import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * A slider.
 */
public final class ScrollBar extends Drawable {

    private final float barWidth;
    private final float barHeight;
    private final float xPosition;
    private final float yPosition;
    private final float sliderPositionMin;
    private final float sliderPositionMax;
    private final PGraphics screen;
    private float sliderPosition;
    private float newSliderPosition;
    private boolean mouseover;
    private boolean locked;

    /**
     * Create a slider with coordinates and an output canvas.
     *
     * @param screen output canvas
     * @param x
     * @param y
     */
    public ScrollBar(PGraphics screen, float x, float y) {
        super(screen.parent, Utils.nullVector());
        this.barWidth = screen.width;
        this.barHeight = screen.height;
        this.xPosition = x;
        this.yPosition = y;
        this.sliderPosition = 0 + barWidth / 2 - barHeight / 2;
        this.newSliderPosition = sliderPosition;
        this.sliderPositionMin = 0;
        this.sliderPositionMax = barWidth - barHeight;
        this.screen = screen;
    }

    @Override
    public void update() {
	    mouseover = isMouseOver();
	    if (p.mousePressed && mouseover) {
		    locked = true;
	    }
	    if (!p.mousePressed) {
		    locked = false;
	    }
	    if (locked) {
		    newSliderPosition = Utils.trim(p.mouseX - xPosition - barHeight / 2, sliderPositionMin, sliderPositionMax);
	    }
	    if (PApplet.abs(newSliderPosition - sliderPosition) > 1) {
		    sliderPosition = sliderPosition + (newSliderPosition - sliderPosition);
	    }
    }

    @Override
    public void draw() {
	    screen.noStroke();
	    screen.fill(200);
	    screen.rect(0, 0, barWidth, barHeight);
	    screen.fill(mouseover || locked ? 0 : 220);
	    screen.rect(sliderPosition, 0, barHeight, barHeight);
    }

    /** Get the current position between 0.2 and 2. */
    public float pos() {
        return Utils.trim(2 * sliderPosition / (barWidth - barHeight), 0.2f, 2);
    }

    private boolean isMouseOver() {
        return p.mouseX > xPosition && p.mouseX < xPosition + barWidth
                && p.mouseY > yPosition && p.mouseY < yPosition + barHeight;
    }
}
