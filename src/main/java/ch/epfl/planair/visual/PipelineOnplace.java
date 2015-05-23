package ch.epfl.planair.visual;

import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.meta.Utils;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntUnaryOperator;

public class PipelineOnPlace {

	private final PApplet p;

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
	private final static float[] COS;
	private final static float[] SIN;

	static {
		COS = new float[(int) Math.ceil(PConstants.PI / Consts.PIPELINE_DISCRETIZATION_STEPS_PHI)];
		SIN = new float[(int) Math.ceil(PConstants.PI / Consts.PIPELINE_DISCRETIZATION_STEPS_PHI)];

		/* Construct cos and sin constants */
		for (int i = 0; i < PConstants.PI / Consts.PIPELINE_DISCRETIZATION_STEPS_PHI; i += 1) {
			COS[i] = (float) Math.cos(i * Consts.PIPELINE_DISCRETIZATION_STEPS_PHI);
			SIN[i] = (float) Math.sin(i * Consts.PIPELINE_DISCRETIZATION_STEPS_PHI);
		}
	}

	public PipelineOnPlace(PApplet p) {
		this.p = p;
	}

	private void threshold(PImage source, IntUnaryOperator op) {
		for (int i = 0; i < source.width * source.height; ++i) {
			source.pixels[i] = op.applyAsInt(source.pixels[i]);
		}
	}

	private int[] thresholdBinary(PImage source, IntUnaryOperator op) {
		int[] pixels = new int[source.width * source.height];
		for (int i = 0; i < source.width * source.height; ++i) {
			pixels[i] = op.applyAsInt(source.pixels[i]);
		}
		return pixels;
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
	public int[] binaryBrightnessThresholdTab(PImage source, int threshold, int minColor, int maxColor) {
		Utils.require(0, minColor, 255, "invalid grey color");
		Utils.require(0, maxColor, 255, "invalid grey color");
		return thresholdBinary(source, v -> p.brightness(v) > threshold ? p.color(maxColor) : p.color(minColor));
	}

	public void binaryBrightnessThreshold(PImage source, int threshold, int minColor, int maxColor) {
		Utils.require(0, minColor, 255, "invalid grey color");
		Utils.require(0, maxColor, 255, "invalid grey color");
		threshold(source, v -> p.brightness(v) > threshold ? p.color(maxColor) : p.color(minColor));
	}

	public void inverseBinaryBrightnessThreshold(PImage source, int threshold, int minColor, int maxColor) {
		binaryBrightnessThreshold(source, threshold, maxColor, minColor);
	}

	public void truncateBrightnessThreshold(PImage source, int threshold) {
		Utils.require(0, threshold, 255, "invalid grey color");
		threshold(source, v -> p.brightness(v) > threshold ? p.color(threshold) : p.color(p.brightness(v)));
	}

	public void toZeroBrightnessThreshold(PImage source, int threshold, int minColor) {
		Utils.require(0, threshold, 255, "invalid grey color");
		Utils.require(0, minColor, 255, "invalid grey color");
		threshold(source, v -> p.brightness(v) > threshold ? p.color(p.brightness(v)): p.color(minColor));
	}

	public void inverseToZeroBrightnessThreshold(PImage source, int threshold) {
		Utils.require(0, threshold, 255, "invalid grey color");
		threshold(source, v -> p.brightness(v) > threshold ? p.color(p.brightness(v)): p.color(threshold));
	}

	public void selectHueThreshold(PImage source, int firstThreshold, int secondThreshold, int otherColor) {
		Utils.require(0, otherColor, 255, "invalid grey color");
		threshold(source, v -> firstThreshold <= p.hue(v) && p.hue(v) <= secondThreshold ? v : p.color(otherColor));
	}

	public void selectSaturationThreshold(PImage source, int firstThreshold, int secondThreshold, int otherColor) {
		Utils.require(0, otherColor, 255, "invalid grey color");
		threshold(source, v -> firstThreshold <= p.saturation(v) && p.saturation(v) <= secondThreshold ? v : p.color(otherColor));
	}

	public void selectBrightnessThreshold(PImage source, int firstThreshold, int secondThreshold, int otherColor) {
		Utils.require(0, otherColor, 255, "invalid grey color");
		threshold(source, v -> firstThreshold <= p.brightness(v) && p.brightness(v) <= secondThreshold ? v : p.color(otherColor));
	}

	public void convolute(PImage source, float[][] kernel) {
		float weight = 0;
		for (float[] x: kernel) {
			for (float f: x) {
				weight += PApplet.abs(f);
			}
		}
		int margin = kernel.length / 2;

		for (int x = margin; x + margin < source.width; ++x) {
			for (int y = margin; y + margin < source.height; ++y) {

				float sum = 0;
				for (int px = x - margin, sx = 0; px <= x + margin; ++px, ++sx) {
					for (int py = y - margin, sy = 0; py <= y + margin; ++py, ++sy) {
						sum += p.brightness(source.pixels[align(source, px, py)]) * kernel[sx][sy];
					}
				}
				source.pixels[align(source, x, y)] = p.color(sum / weight);
			}
		}
	}


	public void sobel(PImage source, float threshold) {
		sobel(source, threshold, 255, 0);
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
	public void sobel(PImage source, float threshold, int minColor, int maxColor) {
		Utils.require(0, minColor, 255, "invalid grey color");
		Utils.require(0, maxColor, 255, "invalid grey color");

		int margin = sobelKernelH.length / 2;
		float maxValue = 0;
		int size = source.width * source.height;
		float[] buffer = new float[size];

		for (int x = margin; x + margin < source.width; ++x) {
			for (int y = margin; y + margin < source.height; ++y) {

				float sumH = 0;
				float sumV = 0;
				for (int px = x - margin, sx = 0; px <= x + margin; ++px, ++sx) {
					for (int py = y - margin, sy = 0; py <= y + margin; ++py, ++sy) {

						int sid = align(source, px, py);
						sumH += p.brightness(source.pixels[sid]) * sobelKernelH[sx][sy];
						sumV += p.brightness(source.pixels[sid]) * sobelKernelV[sx][sy];
					}
				}

				int bid = align(source, x, y);
				buffer[bid] = PApplet.sqrt(sumH * sumH + sumV * sumV);
				if (buffer[bid] > maxValue) {
					maxValue = buffer[bid];
				}
			}
		}

		for (int i = margin * source.width; i < size; ++i) {
			source.pixels[i] = buffer[i] / maxValue > threshold ? p.color(minColor): p.color(maxColor);
		}
	}

	public List<PVector> hough(PImage edgeImg) {
		// dimensions of the accumulator
		int phiDim = (int) (Math.PI / Consts.PIPELINE_DISCRETIZATION_STEPS_PHI);
		int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / Consts.PIPELINE_DISCRETIZATION_STEPS_R);

		// Updated at each pass of the inner-most for-loop (for each value of phi for each align)

		// our accumulator (with a 1 pix margin around)
		int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];

		// Fill the accumulator: on edge points (ie, white pixels of the edge
		// image), store all possible (r, phi) pairs describing lines going
		// through the point.
		for (int y = 0; y < edgeImg.height; ++y) {
			for (int x = 0; x < edgeImg.width; ++x) {
				// Are we on an edge?
				if (p.brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
					// ...determine here all the lines (r, phi) passing through
					// align (x,y), convert (r,phi) to coordinates in the
					// accumulator, and increment accordingly the accumulator.

					for (int accPhi = 0; accPhi < PConstants.PI / Consts.PIPELINE_DISCRETIZATION_STEPS_PHI; accPhi += 1) {
						double radius = x * COS[accPhi] + y * SIN[accPhi];
						float accR = (float) (radius / Consts.PIPELINE_DISCRETIZATION_STEPS_R) + (rDim - 1) * 0.5f;

						accumulator[(int) ((accPhi + 1) * (rDim + 2) + accR + 1)] += 1;
					}
				}
			}
		}

		// size of the region we search for a local maximum
		int neighbourhood = 10;
		// only search around lines with more that this amount of votes
		// (to be adapted to your image)
		int minVotes = 200;
		List<Integer> best = new ArrayList<>();

		for (int accR = 0; accR < rDim; accR++) {
			for (int accPhi = 0; accPhi < phiDim; accPhi++) {

				// compute current index in the accumulator
				int idx = (accPhi + 1) * (rDim + 2) + accR + 1;
				if (accumulator[idx] > minVotes) {
					boolean bestCandidate = true;

					// iterate over the neighbourhood
					for (int dPhi = -neighbourhood / 2; dPhi < neighbourhood / 2 + 1; ++dPhi) {

						// check we are not outside the image
						if ( accPhi + dPhi < 0 || accPhi + dPhi >= phiDim) continue;
						for (int dR = -neighbourhood / 2; dR < neighbourhood / 2 +1; ++dR) {

							// check we are not outside the image
							if (accR+dR < 0 || accR+dR >= rDim) continue;

							int neighbourIdx = (accPhi + dPhi + 1) * (rDim + 2) + accR + dR + 1;

							if (accumulator[idx] < accumulator[neighbourIdx]) {

								// the current idx is not a local maximum!
								bestCandidate = false;
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

		Collections.sort(best, (a, b) -> - Integer.compare(accumulator[a], accumulator[b]));
		List<PVector> selected = new ArrayList<>();

		for (int i = 0; i < best.size() && i < Consts.PIPELINE_LINES_COUNT; ++i) {

			int idx = best.get(i);

			// first, compute back the (r, phi) polar coordinates:
			int accPhi = idx / (rDim + 2) - 1;
			float accR = idx - (accPhi + 1) * (rDim + 2) - 1;
			float r = (accR - (rDim - 1) * 0.5f) * Consts.PIPELINE_DISCRETIZATION_STEPS_R;
			float phi = accPhi * Consts.PIPELINE_DISCRETIZATION_STEPS_PHI;

			selected.add(new PVector(r, phi));
		}

		return selected;
	}

	public void debugPlotLine(PImage edgeImg, List<PVector> lines) {

		for (PVector line: lines) {

			float r = line.x;
			float phi = line.y;
			int accPhi = (int) (phi / Consts.PIPELINE_DISCRETIZATION_STEPS_PHI);

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
			p.stroke(204, 102, 0);
			if (y0 > 0) {
				if (x1 > 0) p.line(x0, y0, x1, y1);
				else if (y2 > 0) p.line(x0, y0, x2, y2);
				else p.line(x0, y0, x3, y3);
			} else {
				if (x1 > 0) {
					if (y2 > 0) p.line(x1, y1, x2, y2);
					else p.line(x1, y1, x3, y3);
				} else p.line(x2, y2, x3, y3);
			}
		}

	}

	private int align(PImage source, int x, int y) {
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

				if (QuadGraph.isConvex(c12, c23, c34, c41) &&
						QuadGraph.validArea(c12, c23, c34, c41, 70000000, 50000) &&
						QuadGraph.nonFlatQuad(c12, c23, c34, c41)) {

					return Arrays.asList(c12, c23, c34, c41, l1, l2, l3, l4);
				}
			}
		}
		return Collections.emptyList();
	}

	public static PVector intersection(PVector line1, PVector line2) {

		double sin_t1 = Math.sin(line1.y);
		double sin_t2 = Math.sin(line2.y);
		double cos_t1 = Math.cos(line1.y);
		double cos_t2 = Math.cos(line2.y);
		float r1 = line1.x;
		float r2 = line2.x;

		double denom = cos_t2 * sin_t1 - cos_t1 * sin_t2;

		int x = (int) ((r2 * sin_t1 - r1 * sin_t2) / denom);
		int y = (int) ((-r2 * cos_t1 + r1 * cos_t2) / denom);

		return new PVector(x, y);
	}
}
