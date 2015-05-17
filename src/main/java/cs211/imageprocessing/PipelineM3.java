package cs211.imageprocessing;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * Modified version of our pipeline class for assignment 3.
 * Java 8 required.
 */
public class PipelineM3 extends PApplet {

	private final PApplet parent;

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

	public PipelineM3(PApplet parent) {
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

	public PImage binaryBrightnessThreshold(PImage source, int threshold, int minColor, int maxColor) {
		Utils.require(0, minColor, 255, "invalid grey color");
		Utils.require(0, maxColor, 255, "invalid grey color");
		return threshold(source, v -> parent.brightness(v) > threshold ? color(maxColor) : color(minColor));
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

	public PImage sobel(PImage source, float threshold, int minColor, int maxColor) {
		Utils.require(0, threshold, 1, "invalid threshold");
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

	public int[] houghAccumulator(PImage edgeImg) {

		int phiDim = (int) (Math.PI / Constants.PIPELINE_DISCRETIZATION_STEPS_PHI);
		int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / Constants.PIPELINE_DISCRETIZATION_STEPS_R);

		int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];

		for (int y = 0; y < edgeImg.height; ++y) {
			for (int x = 0; x < edgeImg.width; ++x) {

				if (parent.brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {

					for (int accPhi = 0; accPhi < PI / Constants.PIPELINE_DISCRETIZATION_STEPS_PHI; accPhi += 1) {
						double radius = x * COS[accPhi] + y * SIN[accPhi];
						float accR = (float) (radius / Constants.PIPELINE_DISCRETIZATION_STEPS_R) + (rDim - 1) * 0.5f;

						accumulator[(int) ((accPhi + 1) * (rDim + 2) + accR + 1)] += 1;
					}
				}
			}
		}
		return accumulator;
	}

	public List<PVector> hough(PImage edgeImg, int[] accumulator) {
		// dimensions of the accumulator
		int phiDim = (int) (Math.PI / Constants.PIPELINE_DISCRETIZATION_STEPS_PHI);
		int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / Constants.PIPELINE_DISCRETIZATION_STEPS_R);

		// Updated at each pass of the inner-most for-loop (for each value of phi for each align)

		// our accumulator (with a 1 pix margin around)

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

		for (int i = 0; i < best.size() && i < Constants.PIPELINE_LINES; ++i) {

			int idx = best.get(i);

			// first, compute back the (r, phi) polar coordinates:
			int accPhi = (idx / (rDim + 2)) - 1;
			float accR = idx - (accPhi + 1) * (rDim + 2) - 1;
			float r = (accR - (rDim - 1) * 0.5f) * Constants.PIPELINE_DISCRETIZATION_STEPS_R;
			float phi = accPhi * Constants.PIPELINE_DISCRETIZATION_STEPS_PHI;

			selected.add(new PVector(r, phi, idx));
		}

		return selected;
	}

	public void debugPlotLine(PImage edgeImg, List<PVector> lines, int SCALE, int h) {

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
			int x2 = edgeImg.width;
			int y2 = (int) (-COS[accPhi] / SIN[accPhi] * x2 + r / SIN[accPhi]);
			int y3 = edgeImg.width;
			int x3 = (int) (-(y3 - r / SIN[accPhi]) * (SIN[accPhi] / COS[accPhi]));

			// Finally, plot the lines
			parent.stroke(min(255, line.z), 0, 0);
			if (y0 > 0) {
				if (x1 > 0) parent.line(x0 / SCALE, y0 / SCALE + h, x1 / SCALE, y1 / SCALE + h);
				else if (y2 > 0) parent.line(x0 / SCALE, y0 / SCALE + h, x2 / SCALE, y2 / SCALE + h);
				else parent.line(x0 / SCALE, y0 / SCALE + h, x3 / SCALE, y3 / SCALE + h);
				//parent.text(line.z, x0 / SCALE, y0 / SCALE + h);
			} else {
				if (x1 > 0) {
					if (y2 > 0) parent.line(x1 / SCALE, y1 / SCALE + h, x2 / SCALE, y2 / SCALE + h);
					else parent.line(x1 / SCALE, y1 / SCALE + h, x3 / SCALE, y3 / SCALE + h);
					//parent.text(line.z, x1 / SCALE, y1 / SCALE + h);
				} else {
					parent.line(x2 / SCALE, y2 / SCALE + h, x3 / SCALE, y3 / SCALE + h);
					//parent.text(line.z, x2 / SCALE, y2 / SCALE + h);
				}
			}
		}

	}

	public List<PVector> getPlane(PImage image, List<PVector> lines) {

		QuadGraph quad = new QuadGraph();
		quad.build(lines, image.width, image.height);

		List<int[]> cycles = quad.findCycles();
		if (!cycles.isEmpty()) {
			for (int[] cycle : quad.findCycles()) {

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
						quad.validArea(c12, c23, c34, c41, 600000, 60000) &&
						quad.nonFlatQuad(c12, c23, c34, c41)) {
					// Choose a random, semi-transparent colour

					return Arrays.asList(c12, c23, c34, c41, l1, l2, l3, l4);
				} else if (!quad.isConvex(c12, c23, c34, c41) ) println("convex");
				else if (!quad.validArea(c12, c23, c34, c41, 600000, 60000)) println("area");
				else if (!quad.nonFlatQuad(c12, c23, c34, c41)) println("nonFlat");
			}
		}
		return Collections.emptyList();
	}

	public PVector intersection(PVector line1, PVector line2) {

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

	private int align(PImage source, int x, int y) {
		return y * source.width + x;
	}
}
