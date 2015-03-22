final int WINDOWS_WIDTH = 1000;
final int WINDOWS_HEIGHT = 1000;
final int FRAMERATE = 60;
final float ROT_FACTOR = PI/16;

float mouseYOrigin = 500;
float scaleOrigin = 1;
float scale = 1;
float rotateX = 0;
float rotateY = 0;

void setup () {
  size(WINDOWS_WIDTH, WINDOWS_HEIGHT, P2D);
  frameRate(FRAMERATE);
}

void draw() {
  background(255, 255, 255);
  My3DPoint eye = new My3DPoint(0, 0, -5000);
  My3DPoint origin = new My3DPoint(0, 0, 0);
  My3DBox input3DBox = new My3DBox(origin, 100, 150, 300);
  
  input3DBox = transformBox(input3DBox, scaleMatrix(scale, scale, scale));
  input3DBox = transformBox(input3DBox, rotateXMatrix(rotateX));
  input3DBox = transformBox(input3DBox, rotateYMatrix(rotateY));
  
  //rotated around x
  float[][] transform1 = rotateXMatrix(PI/8);
  input3DBox = transformBox(input3DBox, transform1);
  
  //rotated and translated
  float[][] transform2 = translationMatrix(200, 200, 0);
  input3DBox = transformBox(input3DBox, transform2);
  
  //rotated, translated, and scaled
  float[][] transform3 = scaleMatrix(2, 2, 2);
  input3DBox = transformBox(input3DBox, transform3);
  projectBox(eye, input3DBox).render();
}

class My2DPoint {
  float x;
  float y;
  My2DPoint(float x, float y) {
    this.x = x;
    this.y = y;
  }
}

class My3DPoint {
  float x;
  float y;
  float z;
  My3DPoint(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
}

My2DPoint projectPoint(My3DPoint eye, My3DPoint p) {
  float xp, yp;
  float normalize = -eye.z/(p.z - eye.z);
  xp = (p.x - eye.x) * normalize;
  yp = (p.y - eye.y) * normalize;
  return new My2DPoint(xp, yp); 
}

class My2DBox {
  My2DPoint[] s;
  My2DBox(My2DPoint[] s) {
    this.s = s;
  } 
  void render(){
    int[] is = new int[]{0,0,0,1,1,3,3,4,4,6,6,6};
    int[] js = new int[]{1,3,4,2,5,2,7,5,7,2,7,5};
    for (int i = 0; i < 12; ++i) {
      My2DPoint pi = s[is[i]];
      My2DPoint pj = s[js[i]];
      line(pi.x, pi.y, pj.x, pj.y);
    }
  }
}

class My3DBox {
  My3DPoint[] p;
  My3DBox(My3DPoint origin, float dimX, float dimY, float dimZ){
    float x = origin.x;
    float y = origin.y;
    float z = origin.z;
    this.p = new My3DPoint[]{new My3DPoint(x, y+dimY, z+dimZ),
                             new My3DPoint(x, y,z+dimZ),
                             new My3DPoint(x+dimX, y, z+dimZ),
                             new My3DPoint(x+dimX, y+dimY, z+dimZ),
                             new My3DPoint(x, y+dimY, z),
                             origin,
                             new My3DPoint(x+dimX, y, z),
                             new My3DPoint(x+dimX, y+dimY, z)};
  }
  My3DBox(My3DPoint[] p) {
    this.p = p;
  }
}

My2DBox projectBox(My3DPoint eye, My3DBox box) {
    My2DPoint[] projected = new My2DPoint[box.p.length];
    for (int i = 0; i < box.p.length; ++i) {
      projected[i] = projectPoint(eye, box.p[i]);
    }
    return new My2DBox(projected);
}

float[] homogeneous3DPoint(My3DPoint p) {
  float[] result = {p.x, p.y, p.z , 1};
  return result;
}

float[][] rotateXMatrix(float angle) {
  return(new float[][] {{1, 0, 0, 0},
                        {0, cos(angle), sin(angle), 0},
                        {0, -sin(angle), cos(angle), 0},
                        {0, 0, 0, 1}});
}

float[][] rotateYMatrix(float angle) {
  return(new float[][] {{cos(angle), 0, sin(angle), 0},
                        {0, 1, 0, 0},
                        {-sin(angle), 0, cos(angle), 0},
                        {0, 0, 0, 1}});
}

float[][] rotateZMatrix(float angle) {
  return(new float[][] {{cos(angle), sin(angle), 0 , 0},
                        {-sin(angle), cos(angle), 0, 0},
                        {0, 0, 1, 0},
                        {0, 0, 0, 1}});
}

float[][] scaleMatrix(float x, float y, float z) {
  return(new float[][] {{x, 0, 0, 0},
                        {0, y, 0, 0},
                        {0, 0, z, 0},
                        {0, 0, 0, 1}});
}

float[][] translationMatrix(float x, float y, float z) {
  return(new float[][] {{1, 0, 0, x},
                        {0, 1, 0, y},
                        {0, 0, 1, z},
                        {0, 0, 0, 1}});
}

float[] matrixProduct(float[][] a, float[] b) {
  return(new float[] {a[0][0]*b[0] + a[0][1]*b[1] + a[0][2]*b[2] + a[0][3]*b[3],
                      a[1][0]*b[0] + a[1][1]*b[1] + a[1][2]*b[2] + a[1][3]*b[3],
                      a[2][0]*b[0] + a[2][1]*b[1] + a[2][2]*b[2] + a[2][3]*b[3],
                      a[3][0]*b[0] + a[3][1]*b[1] + a[3][2]*b[2] + a[3][3]*b[3]});
}

My3DPoint euclidian3DPoint (float[] a) {
  My3DPoint result = new My3DPoint(a[0]/a[3], a[1]/a[3], a[2]/a[3]);
  return result;
}

My3DBox transformBox(My3DBox box, float[][] transformMatrix) {
  My3DPoint[] transformed = new My3DPoint[box.p.length];
  for(int i = 0; i < box.p.length; ++i) {
    float[] converted = homogeneous3DPoint(box.p[i]);
    float[] transformation = matrixProduct(transformMatrix, converted);
    transformed[i] = euclidian3DPoint(transformation);
  }
  return new My3DBox(transformed);
}

/* EVENT HANDLERS */

void mouseDragged() 
{
  scale = scaleOrigin + (mouseYOrigin - mouseY) / 100;
}

void mousePressed() {
 mouseYOrigin = mouseY;
}

void mouseReleased() {
  scaleOrigin = scale;
}

void keyPressed() {
  switch (keyCode) {
    case UP:
        rotateX += ROT_FACTOR;
      break;
    case DOWN:
        rotateX -= ROT_FACTOR;
      break;
    case LEFT:
        rotateY += ROT_FACTOR;
      break;
    case RIGHT:
        rotateY -= ROT_FACTOR;
      break;    
  }
}
