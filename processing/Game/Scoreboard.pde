final class Scoreboard extends Drawable implements Scorer {

  private final PGraphics overlay;
  private final ArrayList<Projectable> toProject;
  private final PGraphics projection;
  private final PGraphics facts;
  private final Scorable scoreTrack;

  private final int PADDING = 5;
  private final int FONT_SIZE = 11;
  private final int FONT_HEIGHT = 15;
  private final int MODULE_SIZE;

  private float totalScore;
  private float lastScore;
  private float currentVelocity;

  Scoreboard(int overlayWidth, int overlayHeight, Scorable scoreTrack) {
    super(new PVector(0, 0, 0));
    this.MODULE_SIZE = overlayHeight - 2 * PADDING;
    this.overlay = createGraphics(overlayWidth, overlayHeight, P2D);
    this.projection = createGraphics(MODULE_SIZE, MODULE_SIZE, P2D);
    this.facts = createGraphics(MODULE_SIZE, MODULE_SIZE, P2D);
    this.toProject = new ArrayList<Projectable>();
    this.totalScore = 0;
    this.lastScore = 0;
    this.currentVelocity = 0;
    this.scoreTrack = scoreTrack;
    this.scoreTrack.addScoreObserver(this);
  }

  public void notifiedScore(int delta) {
    lastScore = delta * scoreTrack.velocity().mag();
    totalScore += lastScore;
    if (totalScore < 0) {
      totalScore = 0;
    }
  }

  public void update() {
    projection.beginDraw();
    for (Projectable item : toProject) {
      item.projectOn(projection);
    }    
    projection.endDraw();

    if (frameCount % 5 == 0) {
      currentVelocity = scoreTrack.velocity().mag();
    }

    facts.beginDraw();
    facts.textSize(FONT_SIZE);
    facts.textLeading(FONT_HEIGHT);
    facts.fill(50);
    facts.background(220);
    facts.text("Total Score\n"+floor(totalScore * 10), 0, FONT_SIZE);
    facts.text("Velocity\n"+floor(currentVelocity * 10), 0, MODULE_SIZE / 3 + FONT_SIZE);
    facts.text("Last Score\n"+floor(lastScore * 10), 0, 2 * MODULE_SIZE / 3 + FONT_SIZE);
    facts.endDraw();
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
    overlay.image(facts, MODULE_SIZE + 2 * PADDING, PADDING);
    overlay.endDraw();

    image(overlay, 0, height - overlay.height);

    lights();
    popMatrix();
  }

  public float get2DDistanceFrom(float angle) {
    return 0;
  }
}

