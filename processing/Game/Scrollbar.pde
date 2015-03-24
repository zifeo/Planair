final class ScrollBar extends Drawable {
  
  private final float barWidth; //Bar's width in pixels
  private final float barHeight; //Bar's height in pixels
  private final float xPosition; //Bar's x position in pixels
  private final float yPosition; //Bar's y position in pixels
  private float sliderPosition, newSliderPosition; //Position of slider
  private final float sliderPositionMin, sliderPositionMax; //Max and min values of slider
  private boolean mouseOver; //Is the mouse over the slider?
  private boolean locked; //Is the mouse clicking and dragging the slider now?
  private final PGraphics context;
  
  /**
  * @brief Creates a new horizontal scrollbar
  * @param x The x position of the top left corner of the bar in pixels
  * @param y The y position of the top left corner of the bar in pixels
  * @param w The width of the bar in pixels
  * @param h The height of the bar in pixels
  */
  ScrollBar(float x, float y, PGraphics context) {
    super(new PVector(0, 0, 0));
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
  
  /**
  * @brief Updates the state of the scrollbar according to the mouse movement
  */
  public void update() {
    mouseOver = isMouseOver();
    if (mousePressed && mouseOver) {
      locked = true;
    }
    if (!mousePressed) {
      locked = false;
    }
    if (locked) {
      newSliderPosition = trim(mouseX - xPosition - barHeight/2, sliderPositionMin, sliderPositionMax);
    }
    if (abs(newSliderPosition - sliderPosition) > 1) {
      sliderPosition = sliderPosition + (newSliderPosition - sliderPosition);
    }
  }
  
  /**
  * @brief Gets whether the mouse is hovering the scrollbar
  * @return Whether the mouse is hovering the scrollbar
  */
  private boolean isMouseOver() {
    return mouseX > xPosition && mouseX < xPosition+barWidth && mouseY > yPosition && mouseY < yPosition+barHeight;
  }
  
  /**
  * @brief Draws the scrollbar in its current state
  */
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
  
  /**
  * @brief Gets the slider position
  * @return The slider position in the interval [0,1]
  * corresponding to [leftmost position, rightmost position]
  */
  public float pos() {
    return trim(2 * sliderPosition / (barWidth - barHeight), 0.2, 2);
  }
}
