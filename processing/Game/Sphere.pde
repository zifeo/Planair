final class Sphere extends Accelerable {
  
  private final float radius;
  
  Sphere(PVector location, float radius) {
    super(location);
    this.radius = radius;
  }
  
  public void draw() {
    pushMatrix();
    stroke(0);
    fill(127);
    PVector location = location();
    translate(location.x, location.y - radius, location.z);
    sphere(radius);
    drawAxes();
    popMatrix();
  }
  
  public float get2DDistanceFrom(float angle) {
    return radius;
  }
  
}
