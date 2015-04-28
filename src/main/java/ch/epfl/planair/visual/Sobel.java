package ch.epfl.planair.visual;

import processing.core.PApplet;
import processing.core.PImage;

public final class Sobel extends PApplet {

	private PImage img;

	private final static float[][] kernel1 = {
			{ 0, 0, 0 },
			{ 0, 1, 0 },
			{ 0, 0, 0 }
	};

	private final static float[][] kernel2 = {
			{ 0, 1, 0 },
			{ 1, 0, 1 },
			{ 0, 1, 0 }
	};

	private final static float[][] gaussianKernel = {
			{  9, 12,  9 },
			{ 12, 15, 12 },
			{  9, 12,  9 }
	};

	private final static float[][] kernelH = {
			{ 0,  1, 0 },
			{ 0,  0, 0 },
			{ 0, -1, 0 }
	};

	private final static float[][] kernelV = {
			{ 0, 0,  0 },
			{ 1, 0, -1 },
			{ 0, 0,  0 }
	};


	public void setup() {
		size(800, 600);
		img = loadImage("board/board1.jpg");
		noLoop();
		frameRate(60);
	}

	public void draw() {
		background(color(0, 0, 0));

		// kernel1 or kernel2 or gaussianKernel
		// PImage result = convolute(img, gaussianKernel);
		// sobel
		PImage result = sobel(img);

		image(result, 0, 0);
	}

	public int align(PImage source, int x, int y) {
		return y * source.width + x;
	}

	public PImage convolute(PImage source, float[][] kernel) {

		PImage result = createImage(source.width, source.height, ALPHA);
		float weight = 0;
		for (float[] x: kernel) {
			for (float f: x) {
				weight += abs(f);
			}
		}
		int margin = kernel.length / 2;

		for (int x = margin; x + margin < result.width; ++x) {
			for (int y = margin; y + margin < result.height; ++y) {

				float sum = 0;
				for (int px = x - margin, sx = 0; px <= x + margin; ++px, ++sx) {
					for (int py = y - margin, sy = 0; py <= y + margin; ++py, ++sy) {

						sum += brightness(source.pixels[align(source, px, py)]) * kernel[sx][sy];
					}
				}
				result.pixels[align(result, x, y)] = color(sum / weight);
			}
		}
		return result;
	}

	public PImage sobel(PImage source) {

		PImage result = createImage(source.width, source.height, ALPHA);

		int margin = kernelH.length / 2;
		float max = 0;
		float[][] buffer = new float[source.width][source.height];

		for (int x = margin; x + margin < result.width; ++x) {
			for (int y = margin; y + margin < result.height; ++y) {

				float sumH = 0;
				float sumV = 0;
				for (int px = x - margin, sx = 0; px <= x + margin; ++px, ++sx) {
					for (int py = y - margin, sy = 0; py <= y + margin; ++py, ++sy) {

						int id = align(source, px, py);
						sumH += brightness(source.pixels[id]) * kernelH[sx][sy];
						sumV += brightness(source.pixels[id]) * kernelV[sx][sy];
					}
				}
				buffer[x][y] = sqrt(sumH * sumH + sumV * sumV);
				if (buffer[x][y] > max) {
					max = buffer[x][y];
				}
			}
		}

		for (int x = margin; x + margin < result.width; ++x) {
			for (int y = margin; y + margin < result.height; ++y) {
				result.pixels[align(source, x, y)] = buffer[x][y] > 0.3 * max ? color(255): color(0);
			}
		}

		return result;
	}

}