package ch.epfl.planair.visual;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import papaya.Mat;
import papaya.SVD;
import processing.core.PVector;

public final class TwoDThreeD {
	
	// Default focal length, well-suited for most webcams
	private static float f = 700;
	
	// Intrinsic camera matrix
	private static float [][] K = {{f,0,0},
			        	   {0,f,0},
			        	   {0,0,1}};

	// TODO change that to the smaller Lego bord, since it's what we're using
	// Real physical coordinates of the Lego board in millimeters
	private static float boardSize = 380.f; // Large Duplo board

	//static float boardSize = 255.f; // Smaller Lego board
	
	// The 3D coordinates of the physical board corners, clockwise
	private static float [][] physicalCorners = {
		// Store here the 3D coordinates of the corners of
		// the real Lego board, in homogenous coordinates
		// and clockwise.
			{-128, -128, 0, 1},
			{128, -128, 0, 1},
			{128, 128, 0, 1},
			{-128, 128, 0, 1}
		};

	/**
	 * Initiates TwoDThreeD with the width and height of the webcam image
	 * @param width the width of the image
	 * @param height the height of the image
	 */
	public TwoDThreeD(int width, int height) {
		
		// Set the offset to the center of the webcam image
		K[0][2] = 0.5f * width;
		K[1][2] = 0.5f * height;
			
	}

	/**
	 * Computes the 3D angles from the 4 2D vertex of the quadrilateral
	 * @param points2D the 4 vertex of the quadrilateral
	 * @return the corresponding 3D angles
	 */
	public PVector get3DRotations(List<PVector> points2D) {
		
		// Solve the extrinsic matrix from the projected 2D points
		double[][] E = solveExtrinsicMatrix(points2D);
		
		
        /* Re-build a proper 3x3 rotation matrix from the camera's
		extrinsic matrix E */
        float[] firstColumn = {(float)E[0][0],
        					   (float)E[1][0],
        					   (float)E[2][0]};

        firstColumn = Mat.multiply(firstColumn, 1 / Mat.norm2(firstColumn)); // normalize
        
        float[] secondColumn={(float)E[0][1],
        					  (float)E[1][1],
        					  (float)E[2][1]};

        secondColumn = Mat.multiply(secondColumn, 1/Mat.norm2(secondColumn)); // normalize
        
        float[] thirdColumn = Mat.cross(firstColumn, secondColumn);
        
        float[][] rotationMatrix = {
        		{firstColumn[0], secondColumn[0], thirdColumn[0]},
                {firstColumn[1], secondColumn[1], thirdColumn[1]},
                {firstColumn[2], secondColumn[2], thirdColumn[2]}
               };
        
        // Compute and return Euler angles (rx, ry, rz) from this matrix
        return rotationFromMatrix(rotationMatrix);
	
	}

	/**
	 * Solves the extrinsic matrix equation
	 * @param points2D the P vector
	 * @return the resulting matrix [R|t]
	 */
	private double[][] solveExtrinsicMatrix(List<PVector> points2D) {
	
		// p ~= K · [R|t] · P
		// with P the (3D) corners of the physical board, p the (2D) 
		// projected points onto the webcam image, K the intrinsic 
		// matrix and R and t the rotation and translation we want to 
		// compute.
		//
		// => We want to solve: (K^(-1) · p) X ([R|t] · P) = 0
		
		float[][] invK = Mat.inverse(K);

		float[][] projectedCorners = new float[4][3];

		assert(points2D.size() == 4);

		// Sort corners in the clockwise order
		points2D = sortCorners(points2D);
		
		for(int i=0;i<4;i++) {
		    // store in projectedCorners the result of (K^(-1) · p), for each 
		    // corner p found in the webcam image.
		    // You can use Mat.multiply to multiply a matrix with a vector.

			PVector point = points2D.get(i);

			projectedCorners[i] = Mat.multiply(invK, homegenizeVector(point).array());
		}
		
		// 'A' contains the cross-product (K^(-1) · p) X P
	    float[][] A = new float[12][9];
	    
	    for(int i=0;i<4;i++){
	      A[i*3][0] = 0;
	      A[i*3][1] = 0;
	      A[i*3][2] = 0;
	      
	      // note that we take physicalCorners[0,1,*3*]: we drop the Z
	      // coordinate and use the 2D homogenous coordinates of the physical
	      // corners
	      A[i*3][3]=-projectedCorners[i][2] * physicalCorners[i][0];
	      A[i*3][4]=-projectedCorners[i][2] * physicalCorners[i][1];
	      A[i*3][5]=-projectedCorners[i][2] * physicalCorners[i][3];

	      A[i*3][6]= projectedCorners[i][1] * physicalCorners[i][0];
	      A[i*3][7]= projectedCorners[i][1] * physicalCorners[i][1];
	      A[i*3][8]= projectedCorners[i][1] * physicalCorners[i][3];

	      A[i*3+1][0]= projectedCorners[i][2] * physicalCorners[i][0];
	      A[i*3+1][1]= projectedCorners[i][2] * physicalCorners[i][1];
	      A[i*3+1][2]= projectedCorners[i][2] * physicalCorners[i][3];
	      
	      A[i*3+1][3]=0;
	      A[i*3+1][4]=0;
	      A[i*3+1][5]=0;
	      
	      A[i*3+1][6]=-projectedCorners[i][0] * physicalCorners[i][0];
	      A[i*3+1][7]=-projectedCorners[i][0] * physicalCorners[i][1];
	      A[i*3+1][8]=-projectedCorners[i][0] * physicalCorners[i][3];

	      A[i*3+2][0]=-projectedCorners[i][1] * physicalCorners[i][0];
	      A[i*3+2][1]=-projectedCorners[i][1] * physicalCorners[i][1];
	      A[i*3+2][2]=-projectedCorners[i][1] * physicalCorners[i][3];
	      
	      A[i*3+2][3]= projectedCorners[i][0] * physicalCorners[i][0];
	      A[i*3+2][4]= projectedCorners[i][0] * physicalCorners[i][1];
	      A[i*3+2][5]= projectedCorners[i][0] * physicalCorners[i][3];
	      
	      A[i*3+2][6]=0;
	      A[i*3+2][7]=0;
	      A[i*3+2][8]=0;
	    }

	    SVD svd = new SVD(A);
	    
	    double[][] V = svd.getV();
	    
	    double[][] E = new double[3][3];
	    
	    //E is the last column of V
	    for(int i=0;i<9;i++){
	    	E[i/3][i%3] = V[i][V.length-1] / V[8][V.length-1];
	    }
	    
	    return E;

	}

	/**
	 * Computes the rotation angles from the extrinsic matrix
	 * @param mat the extrinsic matrix
	 * @return a vector of the 3D angles
	 */
	private PVector rotationFromMatrix(float[][]  mat) {

		// Assuming rotation order is around x,y,z
		PVector rot = new PVector();
		
		if(mat[1][0] > 0.998) { // singularity at north pole
			rot.z = 0;
			float delta = (float) Math.atan2(mat[0][1],mat[0][2]);
			rot.y = -(float) Math.PI/2;
			rot.x = -rot.z + delta;
			return rot;
		}

		if(mat[1][0] < -0.998) { // singularity at south pole
			rot.z = 0;
			float delta = (float) Math.atan2(mat[0][1],mat[0][2]);
			rot.y = (float) Math.PI/2;
			rot.x = rot.z + delta;
			return rot;
		}

		rot.y = -(float)Math.asin(mat[2][0]);
		rot.x = (float)Math.atan2(mat[2][1] / Math.cos(rot.y), mat[2][2] / Math.cos(rot.y));
		rot.z = (float)Math.atan2(mat[1][0] / Math.cos(rot.y), mat[0][0] / Math.cos(rot.y));

		return rot;
	}

	/**
	 * Takes a (x, y, 0) vector and returns (x, y, 1)
	 * @param v
	 * @return
	 */
	private PVector homegenizeVector(PVector v) {
		assert(v.z == 0);
		return new PVector(v.x, v.y, 1);
	}

	/**
	 * Compares vectors around their center, to sort them in the clockwise order
	 * By center we mean the average vector (v1 + v2 + v3 + v4) / 4
	 */
	static class CWComparator implements Comparator<PVector> {
		PVector center;
		public CWComparator(PVector center) {
			this.center = center;
		}
		@Override
		public int compare(PVector b, PVector d) {
			if(Math.atan2(b.y-center.y,b.x-center.x) < Math.atan2(d.y-center.y,d.x-center.x))
				return -1;
			else return 1;
		}
	}

	/**
	 * Sorts corners in clockwise order accordingly:
	 * {Top left, top right, bottom right, bottom left}
	 * @param quad the list of corners
	 * @return
	 */
	public static List<PVector> sortCorners(List<PVector> quad){
		// Sort corners so that they are ordered clockwise
		PVector a = quad.get(0);
		PVector b = quad.get(2);
		PVector center = new PVector((a.x+b.x)/2,(a.y+b.y)/2);
		Collections.sort(quad, new CWComparator(center));

		// Re-order the corners so that the first one is the closest to the
		// origin (0,0) of the image.
		//
		// You can use Collections.rotate to shift the corners inside the quad.

		PVector origin = new PVector(0, 0);

		// Find index of vector nearest to origin
		int nearestToOrigin = 0;
		double minDistToOrigin = Double.MAX_VALUE;

		for (int i = 0; i < 4; i++) {
			double dist = quad.get(i).dist(origin);
			if (dist <= minDistToOrigin) {
				nearestToOrigin = i;
				minDistToOrigin = dist;
			}
		}

		// Rotate vectors such that the nearest is at first position
		Collections.rotate(quad, nearestToOrigin);

		return quad;
	}
}
