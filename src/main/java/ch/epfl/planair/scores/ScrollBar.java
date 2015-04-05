package ch.epfl.planair.scores;

import ch.epfl.planair.Utils;
import ch.epfl.planair.specs.Drawable;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public final class ScrollBar extends Drawable {

    private final float barWidth; //Bar's width in pixels
    private final float barHeight; //Bar's height in pixels
    private final float xPosition; //Bar's x position in pixels
    private final float yPosition; //Bar's y position in pixels
    private float sliderPosition, newSliderPosition; //Position of slider
    private final float sliderPositionMin, sliderPositionMax; //Max and min values of slider
    private boolean mouseOver; //Is the mouse over the slider?
    private boolean locked; //Is the mouse clicking and dragging the slider now?
    private final PGraphics context;

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

    public void update() {
        mouseOver = isMouseOver();
        if (parent.mousePressed && mouseOver) {
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

    private boolean isMouseOver() {
        return parent.mouseX > xPosition && parent.mouseX < xPosition+barWidth
                && parent.mouseY > yPosition && parent.mouseY < yPosition+barHeight;
    }

    public void draw() {
        context.noStroke();
        context.fill(200);
        context.rect(0, 0, barWidth, barHeight);
        if (mouseOver || locked) {
            context.fill(0);
        } else {
            context.fill(220);
        }
        context.rect(sliderPosition, 0, barHeight, barHeight);
    }

    public float pos() {
        return Utils.trim(2 * sliderPosition / (barWidth - barHeight), 0.2f, 2);
    }
}
