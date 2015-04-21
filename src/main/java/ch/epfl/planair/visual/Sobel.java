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

	public void setup() {
		size(800, 600);
		img = loadImage("board/board1.jpg");
		noLoop();
		frameRate(60);
	}

	public void draw() {
		background(color(0, 0, 0));

		// kernel1 or kernel2 or gaussianKernel
		PImage result = convolute(img, gaussianKernel);

		image(result, 0, 0);
	}

	public int pixel(PImage source, int x, int y) {
		return y * source.width + x;
	}

	public PImage convolute(PImage source, float[][] kernel) {

		PImage result = createImage(source.width, source.height, ALPHA);
		float weight = 0;
		for (float[] x: kernel) {
			for (float f: x) {
				weight += f;
			}
		}
		int margin = kernel.length / 2;

		for (int x = margin; x + margin < result.width; ++x) {
			for (int y = margin; y + margin < result.height; ++y) {

				float sum = 0;
				for (int px = x - margin, sx = 0; px <= x + margin; ++px, ++sx) {
					for (int py = y - margin, sy = 0; py <= y + margin; ++py, ++sy) {

						sum += brightness(source.pixels[pixel(source, px, py)]) * kernel[sx][sy];
					}
				}
				result.pixels[pixel(result, x, y)] = color(sum / weight);
			}
		}
		return result;
	}

	/*public PImage sobel(PImage img) {
		float[][] hKernel = { { 0, 1, 0 },
				{ 0, 0, 0 },
				{ 0, -1, 0 } };
		float[][] vKernel = { { 0, 0, 0 },
				{ 1, 0, -1 },
				{ 0, 0, 0 } };

		PImage result = createImage(img.width, img.height, ALPHA);
		for (int i = 0; i < img.width * img.height; i++) {
			result.pixels[i] = color(0);
		}
		float max = 0;
		float[] buffer = new float[img.width * img.height];

		for (int i = 0; i < result.width * result.height; i++) {
			result.pixels[i];
		}

			for (int y = 2; y < img.height - 2; y++) {
			for (int x = 2; x < img.width - 2; x++) {
				if (buffer[y * img.width + x] > (int)(max * 0.3f)) {
					result.pixels[y * img.width + x] = color(255);
				} else {
					result.pixels[y * img.width + x] = color(0);
				}
			}
		}
		return result;
	}*/

}