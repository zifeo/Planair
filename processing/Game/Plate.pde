final class Plate extends Drawable {
  
  private final int size;
  private final int thickness;
  
  Plate(PVector location, int size, int thickness) {
    super(location);
    this.size = size;
    this.thickness = thickness;
  }
  
  public void draw() {
    pushMatrix();
    fill(146, 192, 220);
    PVector location = location();
    translate(location.x, location.y, location.z);
    box(size, thickness, size);
    drawAxes();
    popMatrix();
  }
  
  public void update() {}
  
  public float get2DDistanceFrom(float angle) {
    return size;
  }
}
