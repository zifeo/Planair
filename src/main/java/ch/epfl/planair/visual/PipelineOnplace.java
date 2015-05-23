package ch.epfl.planair.visual;

import ch.epfl.planair.meta.Constants;
import ch.epfl.planair.meta.Utils;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * The image processing Pipeline, that works on-place on
 * image input.
 */
public class PipelineOnplace extends PApplet {

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

	public PipelineOnplace(PApplet parent) {
		this.parent = parent;

		/* Construct cos and sin constants */
		for (int i = 0; i < PI / Constants.PIPELINE_DISCRETIZATION_STEPS_PHI; i += 1) {
			COS[i] = (float) Math.cos(i * Constants.PIPELINE_DISCRETIZATION_STEPS_PHI);
			SIN[i] = (float) Math.sin(i * Constants.PIPELINE_DISCRETIZATION_STEPS_PHI);
		}
	}

	/**
	 * Applies the threshold operator to every pixel of a PImage
	 * @param source the image to modify
	 * @param op the operator to apply
	 */
	private void threshold(PImage source, IntUnaryOperator op) {
		for (int i = 0; i < source.width * source.height; ++i) {
			source.pixels[i] = op.applyAsInt(source.pixels[i]);
		}
	}

	/**
	 * A binary threshold based on brightness.
	 * If a pixel has a bigger brightness value than threshold,
	 * it is set to  maxColor, otherwise to minColor.
	 *
	 * @param source the image to modify
	 * @param threshold brighness limit (0-255)
	 * @param minColor greyscale (0-255)
	 * @param maxColor greyscale (0-255)
	 * @throws IllegalArgumentException when min or max color are invalid
	 */
	public void binaryBrightnessThreshold(PImage source, int threshold, int minColor, int maxColor) {
		Utils.require(0, minColor, 255, "invalid grey color");
		Utils.require(0, maxColor, 255, "invalid grey color");
		threshold(source, v -> parent.brightness(v) > threshold ? color(maxColor) : color(minColor));
	}

	/**
	 * Keeps a pixel's value if color's hue in within range [firstThreshold, secondThreshold].
	 * Else, puts that pixel's value to otherColor.
	 * @param source The image to modify
	 * @param firstThreshold the first threshold
	 * @param secondThreshold the second threshold
	 * @param otherColor the default color to set when pixel is out of range
	 */
	public void selectHueThreshold(PImage source, int firstThreshold, int secondThreshold, int otherColor) {
		Utils.require(0, otherColor, 255, "invalid grey color");
		threshold(source, v -> firstThreshold <= parent.hue(v) && parent.hue(v) <= secondThreshold ? v : color(otherColor));
	}

	/**
	 * Keeps a pixel's value if color's saturation in within range [firstThreshold, secondThreshold].
	 * Else, puts that pixel's value to otherColor.
	 * @param source The image to modify
	 * @param firstThreshold the first threshold
	 * @param secondThreshold the second threshold
	 * @param otherColor the default color to set when pixel is out of range
	 */
	public void selectSaturationThreshold(PImage source, int firstThreshold, int secondThreshold, int otherColor) {
		Utils.require(0, otherColor, 255, "invalid grey color");
		threshold(source, v -> firstThreshold <= parent.saturation(v) && parent.saturation(v) <= secondThreshold ? v : color(otherColor));
	}

	/**
	 * Keeps a pixel's value if brightness in within range [firstThreshold, secondThreshold].
	 * Else, puts that pixel's value to otherColor.
	 * @param source The image to modify
	 * @param firstThreshold the first threshold
	 * @param secondThreshold the second threshold
	 * @param otherColor the default color to set when pixel is out of range
	 */
	public void selectBrightnessThreshold(PImage source, int firstThreshold, int secondThreshold, int otherColor) {
		Utils.require(0, otherColor, 255, "invalid grey color");
		threshold(source, v -> firstThreshold <= parent.brightness(v) && parent.brightness(v) <= secondThreshold ? v : color(otherColor));
	}

	/**
	 * Applies a convolution on source image.
	 * @param source the image to modify
	 * @param kernel the kernel Matrix to convolute with
	 */
	public void convolute(PImage source, float[][] kernel) {
		float kernelSum = 0;

		// Compute the weight of the kernel
		for (float[] x: kernel) {
			for (float f: x) {
				kernelSum += abs(f);
			}
		}

		int margin = kernel.length / 2;

		// Compute the convolution
		for (int x = margin; x + margin < source.width; ++x) {
			for (int y = margin; y + margin < source.height; ++y) {

				// Apply the kernel on the surrounding pixels of the (x;y) pixel
				float localSum = 0;
				for (int px = x - margin, sx = 0; px <= x + margin; ++px, ++sx) {
					for (int py = y - margin, sy = 0; py <= y + margin; ++py, ++sy) {
						localSum += parent.brightness(source.pixels[toAccCoordinates(source, px, py)]) * kernel[sx][sy];
					}
				}

				// Set the (x;y) pixel to the normalised
				source.pixels[toAccCoordinates(source, x, y)] = color(localSum / kernelSum);
			}
		}
	}

	/**
	 * Applies the edge-detection "Sobel" algorithm on the source image.
	 * Puts pixels on an edge to white, and pixels outside edges to black.
	 * @param source the image to modify
	 * @param tolerance the tolerance factor
	 * @see <a href="http://en.wikipedia.org/wiki/Sobel_operator">Sobel Operator on Wikipedia</a>
	 */
	public void sobel(PImage source, float tolerance) {
		sobel(source, tolerance, 255, 0);
	}

	/**
	 * Applies the edge-detection "Sobel" algorithm on the source image.
	 * Puts pixels on an edge to edgeColor, and pixels outside edges to outColor.
	 * @param source the image to modify
	 * @param tolerance the tolerance factor
	 * @param  edgeColor the
	 */
	public void sobel(PImage source, float tolerance, int edgeColor, int outColor) {
		Utils.require(0, edgeColor, 255, "Invalid grey color");
		Utils.require(0, outColor, 255, "Invalid grey color");

		int margin = sobelKernelH.length / 2;
		float maxValue = 0;
		int size = source.width * source.height;
		float[] buffer = new float[size];

		// For each pixel, apply two different kernel
		for (int x = margin; x + margin < source.width; ++x) {
			for (int y = margin; y + margin < source.height; ++y) {


				// Apply the two kernels on the surrounding pixels
				float sumH = 0;
				float sumV = 0;
				for (int px = x - margin, sx = 0; px <= x + margin; ++px, ++sx) {
					for (int py = y - margin, sy = 0; py <= y + margin; ++py, ++sy) {

						int sid = toAccCoordinates(source, px, py);
						sumH += parent.brightness(source.pixels[sid]) * sobelKernelH[sx][sy];
						sumV += parent.brightness(source.pixels[sid]) * sobelKernelV[sx][sy];
					}
				}

				int bid = toAccCoordinates(source, x, y);
				buffer[bid] = sqrt(sumH * sumH + sumV * sumV);
				if (buffer[bid] > maxValue) {
					maxValue = buffer[bid];
				}
			}
		}

		for (int i = margin * source.width; i < size; ++i) {
			source.pixels[i] = buffer[i] / maxValue > tolerance ? color( edgeColor): color(outColor);
		}
	}

	/**
	 * Applies the feature extraction Hough Transform operation
	 * on the source image. Is typically used after Sobel to find
	 * the best lines corresponding to detected edges.
	 * Returns a set of lines that were recognised on the image.
	 * @param  source the source image
	 * @return the set of lines
	 * @see <a href="http://en.wikipedia.org/wiki/Hough_transform">Hough Transform on Wikipedia</a>
	 */
	public List<PVector> hough(PImage source) {
		// Dimensions of the accumulator
		int phiDim = (int) (Math.PI / Constants.PIPELINE_DISCRETIZATION_STEPS_PHI);
		int rDim = (int) ((( source.width +  source.height) * 2 + 1) / Constants.PIPELINE_DISCRETIZATION_STEPS_R);

		// An accumulator (with a 1 pix margin around). The accumulator is in
		// polar coordinates [radius, phi]
		int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];

		// Fill the accumulator: on edge points (ie, white pixels of the edge
		// image), store all possible (r, phi) pairs describing lines going
		// through the point.
		for (int y = 0; y <  source.height; ++y) {
			for (int x = 0; x <  source.width; ++x) {
				// Are we on an edge?
				if (parent.brightness( source.pixels[y *  source.width + x]) != 0) {

					// Determine all the lines (r, phi) passing through
					// align (x,y), convert (r,phi) to coordinates in the
					// accumulator, and increment the accumulator accordingly.
					for (int accPhi = 0; accPhi < PI / Constants.PIPELINE_DISCRETIZATION_STEPS_PHI; accPhi += 1) {
						double radius = x * COS[accPhi] + y * SIN[accPhi];
						float accR = (float) (radius / Constants.PIPELINE_DISCRETIZATION_STEPS_R) + (rDim - 1) * 0.5f;

						accumulator[(int) ((accPhi + 1) * (rDim + 2) + accR + 1)] += 1;
					}
				}
			}
		}

		// Size of the region we search for a local maximum
		int neighbourhood = 10;

		// Minimum number of votes a line needs in order to be considered
		int minVotes = 200;

		List<Integer> bestLinesId = new ArrayList<>();

		for (int accR = 0; accR < rDim; accR++) {
			for (int accPhi = 0; accPhi < phiDim; accPhi++) {

				// Compute current index in the accumulator
				int idx = (accPhi + 1) * (rDim + 2) + accR + 1;
				if (accumulator[idx] > minVotes) {
					boolean bestCandidate = true;

					// Iterate over the neighbourhood
					for (int dPhi = -neighbourhood / 2; dPhi < neighbourhood / 2 + 1; ++dPhi) {

						// Check that phi is not outside the image
						if ( accPhi + dPhi < 0 || accPhi + dPhi >= phiDim) {
							continue;
						}

						for (int dR = -neighbourhood / 2; dR < neighbourhood / 2 +1; ++dR) {

							// Check that R is not outside the image
							if (accR + dR < 0 || accR + dR >= rDim) {
								continue;
							}

							int neighbourIdx = (accPhi + dPhi + 1) * (rDim + 2) + accR + dR + 1;

							if (accumulator[idx] < accumulator[neighbourIdx]) {

								// The current idx is not a local maximum
								bestCandidate = false;
								break;
							}
						}

						if (!bestCandidate) {
							break;
						}
					}

					if (bestCandidate) {
						// the current idx is a local maximum
						bestLinesId.add(idx);
					}
				}
			}
		}

		// Select the best n lines (n = PIPELINE_LINES_COUNT)
		Collections.sort(bestLinesId, (a, b) -> -Integer.compare(accumulator[a], accumulator[b]));
		List<PVector> selected = new ArrayList<>();

		for (int i = 0; i < bestLinesId.size() && i < Constants.PIPELINE_LINES_COUNT; ++i) {

			int idx = bestLinesId.get(i);

			// First, compute back the (r, phi) polar coordinates:
			int accPhi = (idx / (rDim + 2)) - 1;

			float accR = idx - (accPhi + 1) * (rDim + 2) - 1;
			float r = (accR - (rDim - 1) * 0.5f) * Constants.PIPELINE_DISCRETIZATION_STEPS_R;
			float phi = accPhi * Constants.PIPELINE_DISCRETIZATION_STEPS_PHI;

			selected.add(new PVector(r, phi));
		}

		return selected;
	}

	/**
	 * Prints lines on the image, given their radius and phi in polar coordinates.
	 * Is used to debug the Hough algorithm.
	 * @param source the image on which to add the lines
	 * @param lines the lines in polar coordinates [r, phi]
	 */
	public void debugPlotLine(PImage source, List<PVector> lines) {

		for (PVector line: lines) {

			float r = line.x;
			float phi = line.y;
			int accPhi = (int) (phi / Constants.PIPELINE_DISCRETIZATION_STEPS_PHI);

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
			int x2 = source.width;
			int y2 = (int) (-COS[accPhi] / SIN[accPhi] * x2 + r / SIN[accPhi]);
			int y3 = source.width;
			int x3 = (int) (-(y3 - r / SIN[accPhi]) * (SIN[accPhi] / COS[accPhi]));

			// Finally, plot the lines
			parent.stroke(204, 102, 0);
			if (y0 > 0) {
				if (x1 > 0) parent.line(x0, y0, x1, y1);
				else if (y2 > 0) parent.line(x0, y0, x2, y2);
				else parent.line(x0, y0, x3, y3);
			} else {
				if (x1 > 0) {
					if (y2 > 0) parent.line(x1, y1, x2, y2);
					else parent.line(x1, y1, x3, y3);
				} else parent.line(x2, y2, x3, y3);
			}
		}

	}

	/**
	 * Given an image and x and y, computes the coordinates in a
	 * 1-dimension accumulator.
	 * @param source the source image
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return the correspondence in the accumulator coordinates
	 */
	private int toAccCoordinates(PImage source, int x, int y) {
		return y * source.width + x;
	}

	public List<PVector> getPlane(PImage image, List<PVector> lines) {

		QuadGraph quad = new QuadGraph();
		quad.build(lines, image.width, image.height);

		List<int[]> cycles = quad.findCycles();

		if (!cycles.isEmpty()) {
			for (int[] cycle : cycles) {

				PVector l1 = lines.get(cycle[0]);
				PVector l2 = lines.get(cycle[1]);
				PVector l3 = lines.get(cycle[2]);
				PVector l4 = lines.get(cycle[3]);
				// (intersection() is a simplified version of the
				// intersections() method you wrote last week, that simply
				// return the coordinates of the intersection between 2 lines)
				PVector c12 = intersection(l1, l2);
				PVector c23 = intersection(l2, l3);
				PVector c34 = intersection(l3, l4);
				PVector c41 = intersection(l4, l1);

				if (quad.isConvex(c12, c23, c34, c41) &&
						quad.validArea(c12, c23, c34, c41, 70000000, 50000) &&
						quad.nonFlatQuad(c12, c23, c34, c41)) {

					return Arrays.asList(c12, c23, c34, c41, l1, l2, l3, l4);
				}
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Computes the intersection of two lines
	 * @param line1 the first line
	 * @param line2 the second line
	 * @return the point of the intersection
	 */
	public static PVector intersection(PVector line1, PVector line2) {

		double sin_t1 = Math.sin(line1.y);
		double sin_t2 = Math.sin(line2.y);
		double cos_t1 = Math.cos(line1.y);
		double cos_t2 = Math.cos(line2.y);
		float r1 = line1.x;
		float r2 = line2.x;

		double denominator = cos_t2 * sin_t1 - cos_t1 * sin_t2;

		int x = (int) ((r2 * sin_t1 - r1 * sin_t2) / denominator);
		int y = (int) ((-r2 * cos_t1 + r1 * cos_t2) / denominator);

		return new PVector(x, y);
	}
}
