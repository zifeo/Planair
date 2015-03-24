final class Scoreboard extends Drawable {
  
  private final PGraphics overlay;
  private final ArrayList<Projectable> toProject;
  private final PGraphics projection;

  private final int PADDING = 5;
  
  Scoreboard(int overlayWidth, int overlayHeight) {
    super(new PVector(0, 0, 0));
    this.overlay = createGraphics(overlayWidth, overlayHeight, P2D);
    this.projection = createGraphics(overlay.height - 2 * PADDING, overlay.height - 2 * PADDING, P2D);
    this.toProject = new ArrayList<Projectable>();
  }
  
  public void update() {
    projection.beginDraw();
    for (Projectable item : toProject) {
      item.projectOn(projection);
    }    
    projection.endDraw();
  }
  
  public void addForProjection(Projectable item) {
     toProject.add(item);
  }
  
  public void draw() {
    pushMatrix();
    noLights();
    
    overlay.beginDraw();
    overlay.background(220);
    overlay.image(projection, PADDING, PADDING);
    overlay.endDraw();
    
    image(overlay, 0, height - overlay.height);
    
    lights();
    popMatrix();
  }
  
  public float get2DDistanceFrom(float angle) {
    return 0; 
  }
  
  
}
