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
  
  // had to put "Cylinder" here because of Processing buggy behaviour, will change to Movable when on Eclipse
  public int checkCollisions(ArrayList<Cylinder> obstacles) {
    int count = 0;
    PVector location = location();
    for (Drawable obstacle: obstacles) {
      
       PVector obstacleLocation = obstacle.location();      
       float angle = PVector.angleBetween(location, obstacleLocation);
       PVector delta = PVector.sub(location, obstacleLocation);
       float borders = get2DDistanceFrom(angle) + obstacle.get2DDistanceFrom(angle + PI);
       
       if (delta.mag() < borders) {
         PVector normal = PVector.sub(location(), obstacle.location());
         normal.normalize();
         normal.mult(2 * PVector.dot(velocity, normal));
         velocity.sub(normal);
         
         delta.normalize();
         delta.setMag(borders);
         setLocation(PVector.add(obstacleLocation, delta));
         ++count;
       }
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
