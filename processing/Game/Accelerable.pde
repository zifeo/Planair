abstract class Accelerable extends Movable {
    
  private PVector force = new PVector(0, 0, 0);
  private PVector environmentRotation = new PVector(0, 0, 0);
  private boolean computeGravity = false;

  private final float normalForce = 1;
  private final float G = 0.1;
  private final float MU = 0.03; 
    
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
