package ch.epfl.planair.visual;

import processing.core.PApplet;
import processing.core.PImage;

public class Hough extends PApplet{
    float discretizationStepsPhi = 0.06f;
    float discretizationStepsR = 2.5f;

    public void setup() {
        size(800, 600);
        noLoop();
        frameRate(60);
    }

    public void draw() {
        hough(loadImage("board/board1.jpg"));
    }

    public void hough(PImage edgeImg) {
        // dimensions of the accumulator
        int phiDim = (int) (Math.PI / discretizationStepsPhi);
        int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);

        double r;
        int j;

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
                    j = 0;

                    for (float phi = 0; phi < PI; phi += discretizationStepsPhi) {
                        r = x * Math.cos(phi) + y * Math.sin(phi);

                        accumulator[(int) (j * (rDim + 2) + r)] += 1;

                        ++j;
                    }

                }
            }
        }

        println("Done.");

        PImage houghImg = createImage(rDim + 2, phiDim + 2, ALPHA);

        for (int i = 0; i < accumulator.length; i++) {
            houghImg.pixels[i] = color(min(255, accumulator[i]));
        }
        houghImg.updatePixels();

        image(houghImg, 0, 0);
    }
}
