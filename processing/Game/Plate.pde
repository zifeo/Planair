final class Plate extends Drawable implements Projectable {
  
  private final int size;
  private final int thickness;
  
  Plate(PVector location, int size, int thickness) {
    super(location);
    this.size = size;
    this.thickness = thickness;
  }
  
  public void draw() {
    pushMatrix();
    noStroke();
    fill(146, 192, 220);
    PVector location = location();
    translate(location.x, location.y, location.z);
    box(size, thickness, size);
    drawAxes();
    popMatrix();
  }
    
  public void projectOn(PGraphics graphic) {
    graphic.noStroke();
    graphic.fill(100, 50);
    graphic.rect(0, 0, graphic.width, graphic.height);
  }
}
