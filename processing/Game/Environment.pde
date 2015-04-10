class Environment {
  private final float scale = 70;
  private PShape shape;
  
  public Environment() {
    shape = createEnvironment();
  }
  
  public void draw() {
      pushMatrix();
      
      translate(50, 100, -200);
      
      shape(shape);
      
      popMatrix();
  }
  
  
  private PShape createEnvironment() {
    PShape env = loadShape("environment.obj");
    env.scale(scale);
    env.rotate(PI);
    env.rotateY(-PI / 4.0);
    return env; 
  }
}


