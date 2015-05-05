package ch.epfl.planair.visual;

import ch.epfl.planair.config.Constants;
import ch.epfl.planair.config.Utils;
import processing.core.PApplet;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.IntUnaryOperator;

public class Pipeline extends PApplet {

	private final PApplet parent;

	public final static float[][] surroundKernel = {
			{ 0, 1, 0 },
			{ 1, 0, 1 },
			{ 0, 1, 0 }
	};

	public final static float[][] gaussianKernel = {
			{  9, 12,  9 },
			{ 12, 15, 12 },
			{  9, 12,  9 }
	};

	private final static float[][] sobelKernelH = {
			{ 0,  1, 0 },
			{ 0,  0, 0 },
			{ 0, -1, 0 }
	};
	private final static float[][] sobelKernelV = {
			{ 0, 0,  0 },
			{ 1, 0, -1 },
			{ 0, 0,  0 }
	};

	/* COS and SIN constants, to optimise Hough method */
	private final static float[] COS = new float[(int) Math.ceil(PI / Constants.PIPELINE_DISCRETIZATION_STEPS_PHI)];
	private final static float[] SIN = new float[(int) Math.ceil(PI / Constants.PIPELINE_DISCRETIZATION_STEPS_PHI)];

	public Pipeline(PApplet parent) {
		this.parent = parent;

		/* Construct cos and sin constants */
		for (int i = 0; i < PI / Constants.PIPELINE_DISCRETIZATION_STEPS_PHI; i += 1) {
			COS[i] = (float) Math.cos(i * Constants.PIPELINE_DISCRETIZATION_STEPS_PHI);
			SIN[i] = (float) Math.sin(i * Constants.PIPELINE_DISCRETIZATION_STEPS_PHI);
		}
	}

	private PImage threshold(PImage source, IntUnaryOperator op) {
		PImage result = createImage(source.width, source.height, ALPHA);
		for (int i = 0; i < result.width * result.height; ++i) {
			result.pixels[i] = op.applyAsInt(source.pixels[i]);
		}
		return result;
	}

	/**
	 * A binary threshold based on brightness.
	 * If input reaches the limit, max color is set, otherwise min color.
	 *
	 * @param threshold brighness limit (0-255)
	 * @param minColor greyscale (0-255)
	 * @param maxColor greyscale (0-255)
	 * @throws IllegalArgumentException when min or max color are invalid
	 * @return
	 */
	public PImage binaryBrightnessThreshold(PImage source, int threshold, int minColor, int maxColor) {
		Utils.require(0, minColor, 255, "invalid grey color");
		Utils.require(0, maxColor, 255, "invalid grey color");
		return threshold(source, v -> parent.brightness(v) > threshold ? color(maxColor): color(minColor));
	}

	public PImage inverseBinaryBrightnessThreshold(PImage source, int threshold, int minColor, int maxColor) {
		return binaryBrightnessThreshold(source, threshold, maxColor, minColor);
	}

	public PImage truncateBrightnessThreshold(PImage source, int threshold) {
		Utils.require(0, threshold, 255, "invalid grey color");
		return threshold(source, v -> parent.brightness(v) > threshold ? color(threshold) : color(brightness(v)));
	}

	public PImage toZeroBrightnessThreshold(PImage source, int threshold, int minColor) {
		Utils.require(0, threshold, 255, "invalid grey color");
		Utils.require(0, minColor, 255, "invalid grey color");
		return threshold(source, v -> parent.brightness(v) > threshold ? color(brightness(v)): color(minColor));
	}

	public PImage inverseToZeroBrightnessThreshold(PImage source, int threshold) {
		Utils.require(0, threshold, 255, "invalid grey color");
		return threshold(source, v -> parent.brightness(v) > threshold ? color(brightness(v)): color(threshold));
	}

	public PImage selectHueThreshold(PImage source, int firstThreshold, int secondThreshold, int otherColor) {
		Utils.require(0, otherColor, 255, "invalid grey color");
		return threshold(source, v -> firstThreshold <= parent.hue(v) && parent.hue(v) <= secondThreshold ? v : color(otherColor));
	}

	public PImage selectSaturationThreshold(PImage source, int firstThreshold, int secondThreshold, int otherColor) {
		Utils.require(0, otherColor, 255, "invalid grey color");
		return threshold(source, v -> firstThreshold <= parent.saturation(v) && parent.saturation(v) <= secondThreshold ? v : color(otherColor));
	}

	public PImage selectBrightnessThreshold(PImage source, int firstThreshold, int secondThreshold, int otherColor) {
		Utils.require(0, otherColor, 255, "invalid grey color");
		return threshold(source, v -> firstThreshold <= parent.brightness(v) && parent.brightness(v) <= secondThreshold ? v : color(otherColor));
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
						sum += parent.brightness(source.pixels[align(source, px, py)]) * kernel[sx][sy];
					}
				}
				result.pixels[align(result, x, y)] = color(sum / weight);
			}
		}
		return result;
	}


	public PImage sobel(PImage source, float threshold) {
		return sobel(source, threshold, 255, 0);
	}

	/**
	 *
	 * NB: default createImage background is black
	 *
	 * @param source
	 * @param threshold (0-1)
	 * @param minColor greyscale (0-255)
	 * @param maxColor greyscale (0-255)
	 * @return
	 */
	public PImage sobel(PImage source, float threshold, int minColor, int maxColor) {
		Utils.require(0, minColor, 255, "invalid grey color");
		Utils.require(0, maxColor, 255, "invalid grey color");
		PImage result = createImage(source.width, source.height, ALPHA);

		int margin = sobelKernelH.length / 2;
		float maxValue = 0;
		int size = source.width * source.height;
		float[] buffer = new float[size];

		for (int x = margin; x + margin < result.width; ++x) {
			for (int y = margin; y + margin < result.height; ++y) {

				float sumH = 0;
				float sumV = 0;
				for (int px = x - margin, sx = 0; px <= x + margin; ++px, ++sx) {
					for (int py = y - margin, sy = 0; py <= y + margin; ++py, ++sy) {

						int sid = align(source, px, py);
						sumH += parent.brightness(source.pixels[sid]) * sobelKernelH[sx][sy];
						sumV += parent.brightness(source.pixels[sid]) * sobelKernelV[sx][sy];
					}
				}

				int bid = align(result, x, y);
				buffer[bid] = sqrt(sumH * sumH + sumV * sumV);
				if (buffer[bid] > maxValue) {
					maxValue = buffer[bid];
				}
			}
		}

		for (int i = margin * result.width; i < size; ++i) {
			result.pixels[i] = buffer[i] / maxValue > threshold ? color(minColor): color(maxColor);
		}

		return result;
	}

	public PImage hough(PImage edgeImg) {
		// dimensions of the accumulator
		int phiDim = (int) (Math.PI / Constants.PIPELINE_DISCRETIZATION_STEPS_PHI);
		int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / Constants.PIPELINE_DISCRETIZATION_STEPS_R);

		// Updated at each pass of the inner-most for-loop (for each value of phi for each align)

		// our accumulator (with a 1 pix margin around)
		int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];

		// Fill the accumulator: on edge points (ie, white pixels of the edge
		// image), store all possible (r, phi) pairs describing lines going
		// through the point.
		for (int y = 0; y < edgeImg.height; ++y) {
			for (int x = 0; x < edgeImg.width; ++x) {
				// Are we on an edge?
				if (parent.brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
					// ...determine here all the lines (r, phi) passing through
					// align (x,y), convert (r,phi) to coordinates in the
					// accumulator, and increment accordingly the accumulator.

					for (int accPhi = 0; accPhi < PI / Constants.PIPELINE_DISCRETIZATION_STEPS_PHI; accPhi += 1) {
						double radius = x * COS[accPhi] + y * SIN[accPhi];
						float accR = (float) (radius / Constants.PIPELINE_DISCRETIZATION_STEPS_R) + (rDim - 1) * 0.5f;

						accumulator[(int) ((accPhi + 1) * (rDim + 2) + accR + 1)] += 1;
					}
				}
			}
		}

		PImage houghImg = createImage(rDim + 2, phiDim + 2, ALPHA);
		for (int i = 0; i < accumulator.length; i++) {
			houghImg.pixels[i] = color(min(255, accumulator[i]));
		}
		houghImg.updatePixels();
		//houghImg.resize(640, 480);

        /* TODO remove debug lines */

		// size of the region we search for a local maximum
		int neighbourhood = 10;
		// only search around lines with more that this amount of votes
		// (to be adapted to your image)
		int minVotes = 200;
		ArrayList<Integer> best = new ArrayList<>();

		for (int accR = 0; accR < rDim; accR++) {
			for (int accPhi = 0; accPhi < phiDim; accPhi++) {

				// compute current index in the accumulator
				int idx = (accPhi + 1) * (rDim + 2) + accR + 1;
				if (accumulator[idx] > minVotes) {
					boolean bestCandidate = true;

					// iterate over the neighbourhood
					for (int dPhi=-neighbourhood/2; dPhi < neighbourhood/2+1; ++dPhi) {

						// check we are not outside the image
						if ( accPhi+dPhi < 0 || accPhi+dPhi >= phiDim) continue;
						for (int dR=-neighbourhood/2; dR < neighbourhood/2 +1; ++dR) {

							// check we are not outside the image
							if (accR+dR < 0 || accR+dR >= rDim) continue;

							int neighbourIdx = (accPhi + dPhi + 1) * (rDim + 2) + accR + dR + 1;

							if (accumulator[idx] < accumulator[neighbourIdx]) {

								// the current idx is not a local maximum!
								bestCandidate=false;
								break;
							}
						}
						if (!bestCandidate) break;
					}
					if (bestCandidate) {
						// the current idx *is* a local maximum
						best.add(idx);
					}
				}
			}
		}

		/*for (int accR = 0; accR < rDim; accR++) {
			for (int accPhi = 0; accPhi < phiDim; accPhi++) {

				// compute current index in the accumulator

				int idx = (accPhi + 1) * (rDim + 2) + accR + 1;
				if (accumulator[idx] > minVotes) {
					best.add(idx);
				}
			}
		}*/

		Collections.sort(best, (a, b) -> Integer.compare(accumulator[a], accumulator[b]));

		for (int i = 0; i < best.size() && i < 4; ++i) {

			int idx = best.get(i);

			// first, compute back the (r, phi) polar coordinates:
			int accPhi = (int) (idx / (rDim + 2)) - 1;
			float accR = idx - (accPhi + 1) * (rDim + 2) - 1;
			float r = (accR - (rDim - 1) * 0.5f) * Constants.PIPELINE_DISCRETIZATION_STEPS_R;
			float phi = accPhi * Constants.PIPELINE_DISCRETIZATION_STEPS_PHI;
			// Cartesian equation of a line: y = ax + b
			// in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
			// => y = 0 : x = r / cos(phi)
			// => x = 0 : y = r / sin(phi)
			// compute the intersection of this line with the 4 borders of
			// the image
			int x0 = 0;
			int y0 = (int) (r / SIN[accPhi]);
			int x1 = (int) (r / COS[accPhi]);
			int y1 = 0;
			int x2 = edgeImg.width;
			int y2 = (int) (-COS[accPhi] / SIN[accPhi] * x2 + r / SIN[accPhi]);
			int y3 = edgeImg.width;
			int x3 = (int) (-(y3 - r / SIN[accPhi]) * (SIN[accPhi] / COS[accPhi]));

			// Finally, plot the lines
			parent.stroke(204, 102, 0);
			if (y0 > 0) {
				if (x1 > 0)
					parent.line(x0, y0, x1, y1);
				else if (y2 > 0)
					parent.line(x0, y0, x2, y2);
				else
					parent.line(x0, y0, x3, y3);
			}
			else {
				if (x1 > 0) {
					if (y2 > 0)
						parent.line(x1, y1, x2, y2);
					else
						parent.line(x1, y1, x3, y3);
				}
				else
					parent.line(x2, y2, x3, y3);
			}
		}

		return houghImg;
	}

	private int align(PImage source, int x, int y) {
		return y * source.width + x;
	}
}
