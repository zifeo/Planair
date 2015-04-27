package ch.epfl.planair.visual;

import processing.core.PApplet;
import processing.core.PImage;

public class Hough extends PApplet{
    /* TODO remove or keep, depending on refactoring */
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
    float discretizationStepsPhi = 0.06f;
    float discretizationStepsR = 2.5f;

    public void setup() {
        size(800, 600);
        noLoop();
        frameRate(60);
    }

    public void draw() {
        hough(sobel(loadImage("board/board1.jpg")));
    }

    /* TODO refactor sobel + hough into a single class ?*/
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

                        int id = pixel(source, px, py);
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
                result.pixels[pixel(source, x, y)] = buffer[x][y] > 0.3 * max ? color(255): color(0);
            }
        }

        return result;
    }

    public PImage hough(PImage edgeImg) {
        // dimensions of the accumulator
        int phiDim = (int) (Math.PI / discretizationStepsPhi);
        int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);

        // Updated at each pass of the inner-most for-loop (for each value of phi for each pixel)
        double radius;
        float accPhi;
        float accR;

        // our accumulator (with a 1 pix margin around)
        int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];

        // Fill the accumulator: on edge points (ie, white pixels of the edge
        // image), store all possible (r, phi) pairs describing lines going
        // through the point.
        for (int y = 0; y < edgeImg.height; y++) {
            for (int x = 0; x < edgeImg.width; x++) {
                // Are we on an edge?
                if (brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
                    // ...determine here all the lines (r, phi) passing through
                    // pixel (x,y), convert (r,phi) to coordinates in the
                    // accumulator, and increment accordingly the accumulator.

                    for (float phi = 0; phi < PI; phi += discretizationStepsPhi) {
                        accPhi = phi / discretizationStepsPhi;
                        radius = (x * Math.cos(phi)) + (y * Math.sin(phi));
                        accR = (float) (radius / discretizationStepsR) + (rDim - 1) * 0.5f;

                        accumulator[(int) (accPhi * (rDim + 2) + accR)] += 1;
                    }
                }
            }
        }

        PImage houghImg = createImage(rDim + 2, phiDim + 2, ALPHA);

        for (int i = 0; i < accumulator.length; i++) {
            houghImg.pixels[i] = color(min(255, accumulator[i]));
        }
        houghImg.updatePixels();

        /* TODO remove debug lines */
        image(loadImage("board/board1.jpg"), 0, 0);
        for (int idx = 0; idx < accumulator.length; idx++) {
            if (accumulator[idx] > 200) {
                // first, compute back the (r, phi) polar coordinates:
                accPhi = (int) (idx / (rDim + 2)) - 1;
                accR = idx - (accPhi + 1) * (rDim + 2) - 1;
                float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
                float phi = accPhi * discretizationStepsPhi;
                // Cartesian equation of a line: y = ax + b
                // in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
                // => y = 0 : x = r / cos(phi)
                // => x = 0 : y = r / sin(phi)
                // compute the intersection of this line with the 4 borders of
                // the image
                int x0 = 0;
                int y0 = (int) (r / sin(phi));
                int x1 = (int) (r / cos(phi));
                int y1 = 0;
                int x2 = edgeImg.width;
                int y2 = (int) (-cos(phi) / sin(phi) * x2 + r / sin(phi));
                int y3 = edgeImg.width;
                int x3 = (int) (-(y3 - r / sin(phi)) * (sin(phi) / cos(phi)));

                // Finally, plot the lines
                stroke(204,102,0);
                if (y0 > 0) {
                    if (x1 > 0)
                        line(x0, y0, x1, y1);
                    else if (y2 > 0)
                        line(x0, y0, x2, y2);
                    else
                        line(x0, y0, x3, y3);
                }
                else {
                    if (x1 > 0) {
                        if (y2 > 0)
                            line(x1, y1, x2, y2);
                        else
                            line(x1, y1, x3, y3);
                    }
                    else
                        line(x2, y2, x3, y3);
                }
            }
        }

        return houghImg;
    }

    public int pixel(PImage source, int x, int y) {
        return y * source.width + x;
    }
}
