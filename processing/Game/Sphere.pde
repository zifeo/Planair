final class Sphere extends Accelerable implements Projectable {
  
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
  
 public void projectOn(PGraphics graphic) {
    graphic.fill(150, 0, 0);
    graphic.noStroke();
    
    PVector location = location();
    float widthOrigin = xMaxBound() - xMinBound() + 2 * radius;
    float heightOrigin = zMaxBound() - zMinBound() + 2 * radius;
    
    float radiusScaled = radius / widthOrigin * graphic.width;
    float xScaled = (location.x - xMinBound() + 2 * radius) / widthOrigin * graphic.width;
    float yScaled = (location.z - zMinBound() + 2 * radius) / heightOrigin * graphic.height;
    
    graphic.ellipse(xScaled - radiusScaled, yScaled - radiusScaled, 2 * radiusScaled, 2 * radiusScaled);
  }
  
  public float get2DDistanceFrom(float angle) {
    return radius;
  }
  
}
