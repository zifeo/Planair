package ch.epfl.planair.visual;

import ch.epfl.planair.meta.Consts;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class PipelineOnPlace extends PApplet {

	private final PApplet parent;

	public final static float[][] gaussianKernel = {
			{  9, 12,  9 },
			{ 12, 15, 12 },
			{  9, 12,  9 }
	};

	private static int gaussianKernelWeight;
	private static int gaussianKernelMargin;

	/* COS and SIN constants, to optimise Hough method */
	private final static float[] COS;
	private final static float[] SIN;

	static {
		COS = new float[(int) Math.ceil(PI / Consts.PIPELINE_DISCRETIZATION_STEPS_PHI)];
		SIN = new float[(int) Math.ceil(PI / Consts.PIPELINE_DISCRETIZATION_STEPS_PHI)];

		for (int i = 0; i < PI / Consts.PIPELINE_DISCRETIZATION_STEPS_PHI; i += 1) {
			COS[i] = (float) Math.cos(i * Consts.PIPELINE_DISCRETIZATION_STEPS_PHI);
			SIN[i] = (float) Math.sin(i * Consts.PIPELINE_DISCRETIZATION_STEPS_PHI);
		}

		gaussianKernelWeight = 0;
		for (float[] x: gaussianKernel) {
			for (float f: x) {
				gaussianKernelWeight += abs(f);
			}
		}
		gaussianKernelMargin = gaussianKernel.length / 2;
	}

	public PipelineOnPlace(PApplet parent) {
		this.parent = parent;
	}

	public void convolute(PImage source, float[][] kernel) {
		for (int x = gaussianKernelMargin; x + gaussianKernelMargin < source.width; ++x) {
			for (int y = gaussianKernelMargin; y + gaussianKernelMargin < source.height; ++y) {
				float sum = 0;
				for (int px = x - gaussianKernelMargin, sx = 0; px <= x + gaussianKernelMargin; ++px, ++sx) {
					for (int py = y - gaussianKernelMargin, sy = 0; py <= y + gaussianKernelMargin; ++py, ++sy) {
						sum += parent.brightness(source.pixels[align(source, px, py)]) * kernel[sx][sy];
					}
				}
				source.pixels[align(source, x, y)] = color(sum / gaussianKernelWeight);
			}
		}
	}

	public void sobel(PImage source, int threshold, int size) {
		int[] snapshot = new int[size];
		System.arraycopy(source.pixels, 0, snapshot, 0, size);
		for (int x = 1; x  < source.width - 1; ++x) {
			for (int y = 1; y < source.height - 1; ++y) {

				int id = y * source.width + x;

				float sumH = parent.brightness(snapshot[id - 1]) - parent.brightness(snapshot[id + 1]);
				float sumV = parent.brightness(snapshot[id - source.width]) - parent.brightness(snapshot[id + source.width]);

				if (sqrt(sumH * sumH + sumV * sumV) < threshold) {
					source.pixels[id] = Consts.BLACK;
				}
			}
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
				if (parent.brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
					// ...determine here all the lines (r, phi) passing through
					// align (x,y), convert (r,phi) to coordinates in the
					// accumulator, and increment accordingly the accumulator.

					for (int accPhi = 0; accPhi < PI / Consts.PIPELINE_DISCRETIZATION_STEPS_PHI; accPhi += 1) {
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
		int minVotes = Consts.PIPELINE_MIN_VOTES;
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
			int accPhi = (idx / (rDim + 2)) - 1;
			float accR = idx - (accPhi + 1) * (rDim + 2) - 1;
			float r = (accR - (rDim - 1) * 0.5f) * Consts.PIPELINE_DISCRETIZATION_STEPS_R;
			float phi = accPhi * Consts.PIPELINE_DISCRETIZATION_STEPS_PHI;

			selected.add(new PVector(r, phi));
		}

		return selected;
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
						QuadGraph.validArea(c12, c23, c34, c41, Consts.PIPELINE_MAX_PANCHE, Consts.PIPELINE_MIN_PANCHE) &&
						QuadGraph.nonFlatQuad(c12, c23, c34, c41)) {

					return Arrays.asList(c12, c23, c34, c41/*, l1, l2, l3, l4*/);
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