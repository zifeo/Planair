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
float motionFactor = 1.5;

Sphere sphere;
Plate plate;
Tree shiftCylinder;
ArrayList<Drawable> cylinders = new ArrayList<Drawable>();
Scoreboard scoreboard;

void setup() {
  size(WINDOWS_WIDTH, WINDOWS_HEIGHT, P3D);
  frameRate(FRAMERATE);

  PVector onPlate = new PVector(0, -PLATE_THICKNESS/2, 0);
  
  sphere = new Sphere(onPlate, SPHERE_RADIUS);
  sphere.setXBounds(-PLATE_SIZE/2 + SPHERE_RADIUS, PLATE_SIZE/2 - SPHERE_RADIUS);
  sphere.setZBounds(-PLATE_SIZE/2 + SPHERE_RADIUS, PLATE_SIZE/2 - SPHERE_RADIUS);
  sphere.enableGravity();

  plate = new Plate(new PVector(0, 0, 0), PLATE_SIZE, PLATE_THICKNESS);

  shiftCylinder = new Tree(onPlate);
  shiftCylinder.setXBounds(-PLATE_SIZE/2 + CYLINDER_RADIUS, PLATE_SIZE/2 - CYLINDER_RADIUS);
  shiftCylinder.setZBounds(-PLATE_SIZE/2 + CYLINDER_RADIUS, PLATE_SIZE/2 - CYLINDER_RADIUS);
  
  scoreboard = new Scoreboard(width, SCOREBOARD_HEIGHT, sphere);
  scoreboard.addForProjection(plate);
  scoreboard.addForProjection(sphere);
}

void draw() {
  pushMatrix();  
  background(200);
  lights();

  switch (status) {
    case PLAY:
      camera(0, 0, (height/2.0) / tan(PI*30.0 / 180.0), 0, 0, 0, 0, 1, 0);
  
      rotateX(environmentRotation.x);
      rotateY(environmentRotation.y);
      rotateZ(environmentRotation.z);
      sphere.setEnvironmentRotation(environmentRotation);
  
      sphere.update();
      sphere.checkCollisions(cylinders);
      plate.update();
      break;
  
    case ADD_CYLINDER:
      camera(0, -(height/2.0) / tan(PI*30.0 / 180.0), 0, 0, 0, 0, 0, 0, 1);
  
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

float trim(float value, float min, float max) {
  return value > max ? max : value < min ? min : value;
}

float trim(float value, float bound) {
  return trim(value, -bound, bound);
}

/* EVENT HANDLER */

void mouseWheel(MouseEvent e) {
  motionFactor -= e.getCount() / 5.0;
  motionFactor = trim(motionFactor, 0.2, 2);
}

void mouseDragged() {
  if (mouseY < height - SCOREBOARD_HEIGHT) {
    environmentRotation.x = trim(environmentRotation.x - motionFactor * (mouseY - pmouseY) / 100.0, PI_3);
    environmentRotation.z = trim(environmentRotation.z + motionFactor * (mouseX - pmouseX) / 100.0, PI_3);
  }
}

// don't allow the cylinder to be placed over the sphere
void mousePressed() {
  if (status == Status.ADD_CYLINDER) {
    PVector wantedLocation = shiftCylinder.location();
    PVector sphereLocation = sphere.location();      
    float angle = PVector.angleBetween(wantedLocation, sphereLocation);
    float distance = PVector.dist(wantedLocation, sphereLocation);
    float borders = shiftCylinder.get2DDistanceFrom(angle) + sphere.get2DDistanceFrom(angle + PI);
     
    if (distance > borders) {
      Tree obstacle = new Tree(shiftCylinder);
      cylinders.add(obstacle);
      scoreboard.addForProjection(obstacle);
    }
  }
}

void keyReleased() {
  switch (keyCode) {
  case SHIFT:
    status = Status.PLAY;
  }
}

void keyPressed() {
  switch (keyCode) {
  case SHIFT:
    status = Status.ADD_CYLINDER;
  }
}

/* GLOBAL VISIBILITY */
