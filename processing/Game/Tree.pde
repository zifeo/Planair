final class Tree extends Movable implements Projectable {
    
  private PShape shape;
  private final float scale = 40;
  private final float radius = 15;
  
  Tree(PVector location) {
    super(location);
    this.shape = createTree();
  }
  
  Tree(Tree that) {
    this(that.location());
    this.setXBounds(that.xMinBound(), that.xMaxBound());
    this.setYBounds(that.yMinBound(), that.yMaxBound());
    this.setZBounds(that.zMinBound(), that.zMaxBound());
  }
  
  public void draw() {
    pushMatrix();
    PVector location = location();
    translate(location.x, location.y, location.z);
    shape(shape);
    drawAxes();
    popMatrix();
  }
  
  public float get2DDistanceFrom(float angle) {
    return radius; 
  }
  
  public void projectOn(PGraphics graphic) {
    
    graphic.fill(220);
    graphic.noStroke();

    PVector location = location();
    float widthOrigin = xMaxBound() - xMinBound() + 2 * radius;
    float heightOrigin = zMaxBound() - zMinBound() + 2 * radius;
    
    float radiusScaled = radius / widthOrigin * graphic.width;
    float xScaled = (location.x - xMinBound() + 2 * radius) / widthOrigin * graphic.width;
    float yScaled = (location.z - zMinBound() + 2 * radius) / heightOrigin * graphic.height;

    graphic.ellipse(xScaled - radiusScaled, yScaled - radiusScaled, 2 * radiusScaled, 2 * radiusScaled);
  }

  private PShape createTree() {
    PShape tree = loadShape("simpleTree.obj");
    tree.scale(scale);
    tree.rotate(PI);
    return tree;
  }
}

