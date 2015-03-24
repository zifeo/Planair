abstract class Drawable {
  
  private PVector location = new PVector(0, 0, 0);
  
  Drawable(PVector location) {
    this.location.set(location);
  }
  
  public PVector location() {
    return location.get();
  }
   
  public void setLocation(PVector location) {
    this.location.set(location);
  }
  
  public void update() {}
  
  public abstract void draw();
    
  public float get2DDistanceFrom(float angle) {
    return 0;
  }
  
  protected void drawAxes() {
    if (DEBUG) {
      textSize(15);
      noStroke();
      fill(0, 200, 0);
      box(1, 250, 1);
      text("Y", -4, 140, 0); 
      fill(200, 0, 0);
      box(250, 1, 1);
      text("X", 130, 6, 0); 
      fill(0, 0, 200);
      box(1, 1, 250);
      text("Z", -4, 0, 130); 
    }
  }
}
