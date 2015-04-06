package ch.epfl.planair.scores;

import ch.epfl.planair.Utils;
import ch.epfl.planair.specs.Drawable;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

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
    private final PGraphics context;
    private float sliderPosition;
    private float newSliderPosition;
    private boolean mouseover;
    private boolean locked;

    /**
     * Create a slider with coordinates and an output canvas.
     *
     * @param parent
     * @param x
     * @param y
     * @param context output canvas
     */
    public ScrollBar(PApplet parent, float x, float y, PGraphics context) {
        super(parent, new PVector(0, 0, 0));
        this.barWidth = context.width;
        this.barHeight = context.height;
        this.xPosition = x;
        this.yPosition = y;
        this.sliderPosition = 0 + barWidth/2 - barHeight/2;
        this.newSliderPosition = sliderPosition;
        this.sliderPositionMin = 0;
        this.sliderPositionMax = barWidth - barHeight;
        this.context = context;
    }

    /** @inheritdoc */
    public void update() {
        mouseover = isMouseOver();
        if (parent.mousePressed && mouseover) {
            locked = true;
        }
        if (!parent.mousePressed) {
            locked = false;
        }
        if (locked) {
            newSliderPosition = Utils.trim(parent.mouseX - xPosition - barHeight / 2, sliderPositionMin, sliderPositionMax);
        }
        if (Math.abs(newSliderPosition - sliderPosition) > 1) {
            sliderPosition = sliderPosition + (newSliderPosition - sliderPosition);
        }
    }

    /** @inheritdoc */
    public void draw() {
        context.noStroke();
        context.fill(200);
        context.rect(0, 0, barWidth, barHeight);
        if (mouseover || locked) {
            context.fill(0);
        } else {
            context.fill(220);
        }
        context.rect(sliderPosition, 0, barHeight, barHeight);
    }

    /** Get the current position between 0.2 and 2. */
    public float pos() {
        return Utils.trim(2 * sliderPosition / (barWidth - barHeight), 0.2f, 2);
    }

    private boolean isMouseOver() {
        return parent.mouseX > xPosition && parent.mouseX < xPosition+barWidth
                && parent.mouseY > yPosition && parent.mouseY < yPosition+barHeight;
    }
}
