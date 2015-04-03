import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Game extends PApplet {

final boolean DEBUG = true;

final int PLATE_SIZE = 250;
final int PLATE_THICKNESS = 10;
final int SPHERE_RADIUS = 10;
final int CYLINDER_HEIGHT = 20;
final int CYLINDER_RADIUS = 15;
final int CYLINDER_RESOLUTION = 8;
final int SCOREBOARD_HEIGHT = 100;

final int WINDOWS_WIDTH = 500;
final int WINDOWS_HEIGHT = 600;
final int FRAMERATE = 60;
final float PI_3 = PI/3;

Status status = Status.PLAY;
PVector environmentRotation = new PVector(0, 0, 0);
float motionFactor = 1.5f;

Sphere sphere;
Plate plate;
Cylinder shiftCylinder;
ArrayList<Drawable> cylinders = new ArrayList<Drawable>();
Scoreboard scoreboard;

public void setup() {
  size(WINDOWS_WIDTH, WINDOWS_HEIGHT, P3D);
  frameRate(FRAMERATE);

  PVector onPlate = new PVector(0, -PLATE_THICKNESS/2, 0);
  
  sphere = new Sphere(onPlate, SPHERE_RADIUS);
  sphere.setXBounds(-PLATE_SIZE/2 + SPHERE_RADIUS, PLATE_SIZE/2 - SPHERE_RADIUS);
  sphere.setZBounds(-PLATE_SIZE/2 + SPHERE_RADIUS, PLATE_SIZE/2 - SPHERE_RADIUS);
  sphere.enableGravity();

  plate = new Plate(new PVector(0, 0, 0), PLATE_SIZE, PLATE_THICKNESS);

  shiftCylinder = new Cylinder(onPlate, CYLINDER_RADIUS, CYLINDER_HEIGHT, CYLINDER_RESOLUTION);
  shiftCylinder.setXBounds(-PLATE_SIZE/2 + CYLINDER_RADIUS, PLATE_SIZE/2 - CYLINDER_RADIUS);
  shiftCylinder.setZBounds(-PLATE_SIZE/2 + CYLINDER_RADIUS, PLATE_SIZE/2 - CYLINDER_RADIUS);
  
  scoreboard = new Scoreboard(width, SCOREBOARD_HEIGHT, sphere);
  scoreboard.addForProjection(plate);
  scoreboard.addForProjection(sphere);
}

public void draw() {
  pushMatrix();  
  background(200);
  lights();

  switch (status) {
    case PLAY:
      camera(0, 0, (height/2.0f) / tan(PI*30.0f / 180.0f), 0, 0, 0, 0, 1, 0);
  
      rotateX(environmentRotation.x);
      rotateY(environmentRotation.y);
      rotateZ(environmentRotation.z);
      sphere.setEnvironmentRotation(environmentRotation);
  
      sphere.update();
      sphere.checkCollisions(cylinders);
      plate.update();
      break;
  
    case ADD_CYLINDER:
      camera(0, -(height/2.0f) / tan(PI*30.0f / 180.0f), 0, 0, 0, 0, 0, 0, 1);
  
      shiftCylinder.setLocation(new PVector(mouseX - width/2, -PLATE_THICKNESS/2, mouseY - height/2));
      shiftCylinder.draw();
      break;
  }

  sphere.draw();
  plate.draw();
  for (Drawable cylinder : cylinders) {
    cylinder.draw();
  }
  popMatrix();
  
  scoreboard.update();
  scoreboard.draw();
}

public float trim(float value, float min, float max) {
  return value > max ? max : value < min ? min : value;
}

public float trim(float value, float bound) {
  return trim(value, -bound, bound);
}

/* EVENT HANDLER */

public void mouseWheel(MouseEvent e) {
  motionFactor -= e.getCount() / 5.0f;
  motionFactor = trim(motionFactor, 0.2f, 2);
}

public void mouseDragged() {
  if (mouseY < height - SCOREBOARD_HEIGHT) {
    environmentRotation.x = trim(environmentRotation.x - motionFactor * (mouseY - pmouseY) / 100.0f, PI_3);
    environmentRotation.z = trim(environmentRotation.z + motionFactor * (mouseX - pmouseX) / 100.0f, PI_3);
  }
}

// don't allow the cylinder to be placed over the sphere
public void mousePressed() {
  if (status == Status.ADD_CYLINDER) {
    PVector wantedLocation = shiftCylinder.location();
    PVector sphereLocation = sphere.location();      
    float angle = PVector.angleBetween(wantedLocation, sphereLocation);
    float distance = PVector.dist(wantedLocation, sphereLocation);
    float borders = shiftCylinder.get2DDistanceFrom(angle) + sphere.get2DDistanceFrom(angle + PI);
     
    if (distance > borders) {
      Cylinder obstacle = new Cylinder(shiftCylinder);
      cylinders.add(obstacle);
      scoreboard.addForProjection(obstacle);
    }
  }
}

public void keyReleased() {
  switch (keyCode) {
  case SHIFT:
    status = Status.PLAY;
  }
}

public void keyPressed() {
  switch (keyCode) {
  case SHIFT:
    status = Status.ADD_CYLINDER;
  }
}

/* GLOBAL VISIBILITY */
abstract class Accelerable extends Movable {
    
  private PVector force = new PVector(0, 0, 0);
  private PVector environmentRotation = new PVector(0, 0, 0);
  private boolean computeGravity = false;

  private final float normalForce = 1;
  private final float G = 0.1f;
  private final float MU = 0.03f; 
    
  Accelerable(PVector location) {
    super(location);
  }
  
  public void enableGravity() {
      computeGravity = true;
  }
  
  public void disableGravity() {
      computeGravity = false;
  }
  
  public void setEnvironmentRotation(PVector rotation) {
    this.environmentRotation.set(rotation);
  }
    
  private void applyGravity() {
    if (computeGravity) {
      force.x = G * sin(environmentRotation.z);
      // force.y = 0;
      force.z = - G * sin(environmentRotation.x);
    }
  }
  
  public void update() {
    
    applyGravity();
    PVector friction = velocity();
    friction.mult(-1);
    friction.normalize();
    friction.setMag(normalForce * MU);
    
    PVector newVelocity = velocity();
    newVelocity.add(force);
    newVelocity.add(friction);
    setVelocity(newVelocity);
    
    super.update();
  }

}
final class Cylinder extends Movable implements Projectable {
    
  private PShape shape;
  private final float radius;
  private final float cylinderHeight;
  
  Cylinder(PVector location, float radius, float cylinderHeight, int cylinderResolution) {
    super(location);
    this.shape = createCylinder(radius, cylinderHeight, cylinderResolution);
    this.radius = radius;
    this.cylinderHeight = cylinderHeight;
  }
  
  Cylinder(Cylinder that) {
    super(that.location());
    this.shape = that.shape;
    this.radius = that.radius;
    this.cylinderHeight = that.cylinderHeight;
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

  private PShape createCylinder(float radius, float cylinderHeight, int cylinderResolution) {
    PShape cylinder = createShape(GROUP);
    PShape floor = createShape();
    PShape tube = createShape();
    PShape ceiling = createShape();
    
    float angle;
    float[] x = new float[cylinderResolution + 1];
    float[] y = new float[cylinderResolution + 1];
  
    for (int i = 0; i < x.length; ++i) {
      angle = TWO_PI / cylinderResolution * i;
      x[i] = sin(angle) * radius;
      y[i] = cos(angle) * radius;
    }
    
    tube.beginShape(QUAD_STRIP);
    tube.noFill();
    for (int i = 0; i < x.length; ++i) {
      tube.vertex(x[i], y[i] , 0);
      tube.vertex(x[i], y[i], cylinderHeight);
    }
    tube.endShape();
  
    floor.beginShape(TRIANGLE_FAN);
    ceiling.beginShape(TRIANGLE_FAN);
    floor.noFill();
    ceiling.noFill();
    floor.vertex(0, 0, 0);
    ceiling.vertex(0, 0, cylinderHeight);
    for (int i = 0; i < x.length; ++i) {
      floor.vertex(x[i], y[i] , 0);
      ceiling.vertex(x[i], y[i], cylinderHeight);
    }
    floor.endShape();
    ceiling.endShape();
    
    cylinder.addChild(floor);
    cylinder.addChild(tube);
    cylinder.addChild(ceiling);
    cylinder.rotateX(HALF_PI);
    
    return cylinder;
  }
}

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
abstract class Movable extends Drawable {
  
  private PVector velocity = new PVector(0, 0, 0);
  private PVector maxBounds = new PVector(MAX_FLOAT, MAX_FLOAT, MAX_FLOAT);
  private PVector minBounds = new PVector(MIN_FLOAT, MIN_FLOAT, MIN_FLOAT);
    
  Movable(PVector location) {
    super(location);
  }
  
  public PVector velocity() {
    return velocity.get();
  }
   
  public void setVelocity(PVector velocity) {
    this.velocity.set(velocity);
  }
  
  public void setLocation(PVector location) {
    checkBounds(location);
  }
  
  public void update() {
    checkBounds(PVector.add(location(), velocity)); 
  }
  
  public void setXBounds(float min, float max) {
    this.minBounds.x = min;
    this.maxBounds.x = max;
  }
  
  public void setYBounds(float min, float max) {
    this.minBounds.y = min;
    this.maxBounds.y = max;
  }
  
  public void setZBounds(float min, float max) {
    this.minBounds.z = min;
    this.maxBounds.z = max;
  }
  
  public float xMinBound() {
    return minBounds.x;
  }
  
  public float yMinBound() {
    return minBounds.y;
  }
  
  public float zMinBound() {
    return minBounds.z;
  }
  
  public float xMaxBound() {
    return maxBounds.x;
  }
  
  public float yMaxBound() {
    return maxBounds.y;
  }
  
  public float zMaxBound() {
    return maxBounds.z;
  }
  
  public int checkCollisions(ArrayList<Drawable> obstacles) {
    int count = 0;
    PVector location = location();
    PVector correction = new PVector(0, 0, 0);
    for (Drawable obstacle: obstacles) {
      
       PVector obstacleLocation = obstacle.location();     
       float angle = PVector.angleBetween(location, obstacleLocation);
       PVector delta = PVector.sub(location, obstacleLocation);
       float borders = get2DDistanceFrom(angle) + obstacle.get2DDistanceFrom(angle + PI);
       
       if (delta.mag() < borders) {
         PVector normal = PVector.sub(location, obstacleLocation);
         normal.normalize();
         normal.mult(2 * PVector.dot(velocity, normal));
         velocity.sub(normal);
         
         delta.normalize();
         delta.setMag(borders);
         correction.add(PVector.add(obstacleLocation, delta));
         ++count;
       }
    }
    if (count > 0) {
      correction.x /= count;
      correction.y = location.y;
      correction.z /= count;
      setLocation(correction);
    }
    return count;
  }
  
  protected int checkBounds(PVector location) {
    int count = 0;
    if (location.x < minBounds.x) {
      location.x = minBounds.x;
      velocity.x = abs(velocity.x);
      ++count;
    } else if (location.x > maxBounds.x) {
      location.x = maxBounds.x;
      velocity.x = -abs(velocity.x);
      ++count;
    }
    if (location.y < minBounds.y) {
      location.y = minBounds.y;
      velocity.y = abs(velocity.y);
      ++count;
    } else if (location.y > maxBounds.y) {
      location.y = maxBounds.y;
      velocity.y = -abs(velocity.y);
      ++count;
    }
    if (location.z < minBounds.z) {
      location.z = minBounds.z;
      velocity.z = abs(velocity.z);
      ++count;
    } else if (location.z > maxBounds.z) {
      location.z = maxBounds.z;
      velocity.z = -abs(velocity.z);
      ++count;
    }
    super.setLocation(location);
    return count;
  }
}
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
interface Projectable {
  
  public abstract void projectOn(PGraphics graphic);
  
}
abstract class Scorable extends Accelerable {

  private final ArrayList<Scorer> scoreObservers;
  
  Scorable(PVector location) {
    super(location);
    this.scoreObservers = new ArrayList<Scorer>();
  }
    
  public void addScoreObserver(Scorer scorer) {
    scoreObservers.add(scorer);
  }
  
  protected void notifyScore(int delta) {
    for (Scorer scorer : scoreObservers) {
      scorer.notifiedScore(delta);
    }
  } 
  
}
final class Scoreboard extends Drawable implements Scorer {

  private final PGraphics overlay;
  private final ArrayList<Projectable> toProject;
  private final PGraphics projection;
  private final PGraphics facts;
  private final PGraphics barChart;
  private final PGraphics slider;
  private final Scorable scoreTrack;
  private final ScrollBar scrollbar;

  private final int PADDING = 5;
  private final int FONT_SIZE = 11;
  private final int FONT_HEIGHT = 15;
  private final int DT = 2 * FRAMERATE;
  private final int TIME_CHART_BASE = 45;
  private final int SCROLL_HEIGHT = 10;
  private final int MODULE_SIZE;

  private ArrayList<Float> scores;
  private int time;
  private int timeChart;
  private float totalScore;
  private float lastScore;
  private float maxScore;
  private float currentVelocity;

  Scoreboard(int overlayWidth, int overlayHeight, Scorable scoreTrack) {
    super(new PVector(0, height - overlayHeight, 0));
    this.MODULE_SIZE = overlayHeight - 2 * PADDING;
    this.overlay = createGraphics(overlayWidth, overlayHeight, P2D);
    this.projection = createGraphics(MODULE_SIZE, MODULE_SIZE, P2D);
    this.facts = createGraphics(MODULE_SIZE, MODULE_SIZE, P2D);
    this.barChart = createGraphics(overlayWidth - 2 * MODULE_SIZE - 4 * PADDING, MODULE_SIZE - PADDING - SCROLL_HEIGHT, P2D);
    this.slider = createGraphics(this.barChart.width, SCROLL_HEIGHT, P2D);
    this.toProject = new ArrayList<Projectable>();
    this.scores = new ArrayList<Float>();
    this.scores.add(0.0f);
    this.totalScore = 0;
    this.maxScore = 0;
    this.lastScore = 0;
    this.time = 0;
    this.currentVelocity = 0;
    this.scoreTrack = scoreTrack;
    this.scoreTrack.addScoreObserver(this);
    PVector location = location();
    this.scrollbar = new ScrollBar(location.x + 2 * MODULE_SIZE + 3 * PADDING, location.y + 2 * PADDING + this.barChart.height, this.slider);
    this.timeChart = floor(scrollbar.pos() * TIME_CHART_BASE); 
}

  public void notifiedScore(int delta) {
    lastScore = delta * scoreTrack.velocity().mag();
    totalScore += lastScore;
    scores.set(time, scores.get(time) + lastScore);

    if (totalScore < 0) {
      totalScore = 0;
    }
    if (scores.get(time) > maxScore) {
      maxScore = scores.get(time);
    }
  }
  
  public void update() {

    if (frameCount % 5 == 0) {
      currentVelocity = scoreTrack.velocity().mag();
    }
    
    if (frameCount % DT == 0) {
      scores.add(0.0f);
      ++time; 
    }
    
    scrollbar.update();
    timeChart = floor(scrollbar.pos() * TIME_CHART_BASE);

    prepareProjection();
    prepareFacts();
    prepareBarChart(); 
    prepareSlider();   
  }

  public void addForProjection(Projectable item) {
    toProject.add(item);
  }
  
  public void prepareProjection() {
    projection.beginDraw();
    for (Projectable item : toProject) {
      item.projectOn(projection);
    }    
    projection.endDraw();
  }
  
  public void prepareFacts() {
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
  
  public void prepareBarChart() {
    barChart.beginDraw();
    barChart.noStroke();
    barChart.fill(220);
    barChart.background(200);
    scrollbar.draw();
    float size = barChart.width / timeChart;
    float max = (barChart.height - PADDING) / size;
    float scorePerBlock = maxScore / max;
    int delay = max(scores.size() - timeChart, 0);
    for (int t = delay; t < scores.size(); ++t) {
      for (int s = 0; s < scores.get(t) / scorePerBlock; ++s) {
        barChart.rect((t - delay) * size, barChart.height - (s + 1) * size, size - 1, size - 1);
      }
    }
    barChart.endDraw();
  }
  
  public void prepareSlider() {
    slider.beginDraw();
    scrollbar.draw(); 
    slider.endDraw();
  }
  
  public void draw() {
    pushMatrix();
    noLights();

    overlay.beginDraw();
    overlay.background(220);
    overlay.image(projection, PADDING, PADDING);
    overlay.image(facts, MODULE_SIZE + 2 * PADDING, PADDING);
    overlay.image(barChart, 2 * MODULE_SIZE + 3 * PADDING, PADDING);
    overlay.image(slider, 2 * MODULE_SIZE + 3 * PADDING, 2 * PADDING + barChart.height);
    overlay.endDraw();

    image(overlay, 0, height - overlay.height);

    lights();
    popMatrix();
  }
  
}

interface Scorer {
  
  public abstract void notifiedScore(int delta);

}
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
    return trim(2 * sliderPosition / (barWidth - barHeight), 0.2f, 2);
  }
}
final class Sphere extends Scorable implements Projectable {
  
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
  
  public int checkCollisions(ArrayList<Drawable> obstacles) {
    int count = super.checkCollisions(obstacles);
    if (count != 0) {
      notifyScore(count);
    }
    return count;
  }
  
  protected int checkBounds(PVector location) {
    int count = super.checkBounds(location);
    if (count != 0) {
      notifyScore(-count);
    }
    return count; 
  }
  
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Game" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
