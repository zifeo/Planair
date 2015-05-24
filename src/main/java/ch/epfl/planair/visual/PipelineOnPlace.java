package ch.epfl.planair.visual;

import ch.epfl.planair.meta.Consts;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntUnaryOperator;

public class PipelineOnPlace extends PApplet {

	private final PApplet parent;

	private final static ConvolveOp gaussX;
	private final static ConvolveOp gaussY;
	private final static ConvolveOp sobelX;
	private final static ConvolveOp sobelY;

	private final static float[] COS;
	private final static float[] SIN;

	static {
		COS = new float[(int) Math.ceil(PI / Consts.PIPELINE_DISCRETIZATION_STEPS_PHI)];
		SIN = new float[(int) Math.ceil(PI / Consts.PIPELINE_DISCRETIZATION_STEPS_PHI)];

		for (int i = 0; i < PI / Consts.PIPELINE_DISCRETIZATION_STEPS_PHI; i += 1) {
			COS[i] = (float) Math.cos(i * Consts.PIPELINE_DISCRETIZATION_STEPS_PHI);
			SIN[i] = (float) Math.sin(i * Consts.PIPELINE_DISCRETIZATION_STEPS_PHI);
		}

		gaussX = new ConvolveOp(new Kernel(3, 1, new float[]{12/39f, 15/39f, 12/39f}));
		gaussY = new ConvolveOp(new Kernel(1, 3, new float[]{12/39f, 15/39f, 12/39f}));
		sobelX = new ConvolveOp(new Kernel(3, 1, new float[]{1, 0, -1}));
		sobelY = new ConvolveOp(new Kernel(1, 3, new float[]{1, 0, -1}));
	}

	public PipelineOnPlace(PApplet parent) {
		this.parent = parent;
	}

	private void threshold(int[] source, IntUnaryOperator op) {
		for (int i = 0; i < source.length; ++i) {
			source[i] = op.applyAsInt(source[i]);
		}
	}

	public void binaryBrightnessThreshold(int[] source, int threshold, int minColor, int maxColor) {
		//Utils.require(0, minColor, 255, "invalid grey color");
		//Utils.require(0, maxColor, 255, "invalid grey color");
		threshold(source, v -> parent.brightness(v) > threshold ? color(maxColor) : color(minColor));
	}

	public void selectHueThreshold(int[] source, int firstThreshold, int secondThreshold, int otherColor) {
		//Utils.require(0, otherColor, 255, "invalid grey color");
		threshold(source, v -> firstThreshold <= parent.hue(v) && parent.hue(v) <= secondThreshold ? v : color(otherColor));
	}

	public void selectSaturationThreshold(int[] source, int firstThreshold, int secondThreshold, int otherColor) {
		//Utils.require(0, otherColor, 255, "invalid grey color");
		threshold(source, v -> firstThreshold <= parent.saturation(v) && parent.saturation(v) <= secondThreshold ? v : color(otherColor));
	}

	public void selectBrightnessThreshold(int[] source, int firstThreshold, int secondThreshold, int otherColor) {
		//Utils.require(0, otherColor, 255, "invalid grey color");
		threshold(source, v -> firstThreshold <= parent.brightness(v) && parent.brightness(v) <= secondThreshold ? v : color(otherColor));
	}

	public BufferedImage convolute(PImage source, int width, int height) {
		return gaussX.filter(gaussY.filter((BufferedImage) source.getNative(), null), null);
	}

	public int[] sobel(BufferedImage source, int width, int height, int threshold) {
		return sobel(source, width, height, threshold, 255, 0);
	}

	public int bri(int rgb) {
		int red   = (rgb >> 16) & 0xFF;
		int green = (rgb >>  8) & 0xFF;
		int blue  = rgb & 0xFF;

		return (int) (red * 0.2126f + green * 0.7152f + blue * 0.0722f);
	}

	public int[] sobel(BufferedImage b, int width2, int height2, int threshold, int minColor, int maxColor) {

		//BufferedImage bx = sobelX.filter(b, null);
		//b = sobelY.filter(b, null);

		int width = b.getWidth();
		int height = b.getHeight();
		int[] data = new int[width * height];

		for (int x = 0, i = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y, ++i) {

				/*int sum = (int) Math.hypot(bri(bx.getRGB(x, y)), bri(b.getRGB(x, y)));
				data[i] = sum > threshold ? Consts.WHITE: Consts.BLACK;*/

				data[i] = b.getRGB(x, y);

			}
		}

		return data;
	}

	public List<PVector> hough(int[] edgeImg, int width, int height) {
		// dimensions of the accumulator
		int phiDim = (int) (Math.PI / Consts.PIPELINE_DISCRETIZATION_STEPS_PHI);
		int rDim = (int) (((width + height) * 2 + 1) / Consts.PIPELINE_DISCRETIZATION_STEPS_R);

		// Updated at each pass of the inner-most for-loop (for each value of phi for each align)

		// our accumulator (with a 1 pix margin around)
		int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];

		// Fill the accumulator: on edge points (ie, white pixels of the edge
		// image), store all possible (r, phi) pairs describing lines going
		// through the point.
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				// Are we on an edge?
				if (parent.brightness(edgeImg[y * width + x]) != 0) {
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
			int accPhi = (idx / (rDim + 2)) - 1;
			float accR = idx - (accPhi + 1) * (rDim + 2) - 1;
			float r = (accR - (rDim - 1) * 0.5f) * Consts.PIPELINE_DISCRETIZATION_STEPS_R;
			float phi = accPhi * Consts.PIPELINE_DISCRETIZATION_STEPS_PHI;

			selected.add(new PVector(r, phi));
		}

		return selected;
	}

	private int align(int width, int x, int y) {
		return y * width + x;
	}

	public List<PVector> getPlane(int[] image, int width, int height, List<PVector> lines) {

		QuadGraph quad = new QuadGraph();
		quad.build(lines, width, height);

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