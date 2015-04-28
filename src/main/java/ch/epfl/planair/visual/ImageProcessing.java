package ch.epfl.planair.visual;


import processing.core.PApplet;
import processing.core.PImage;

public class ImageProcessing extends PApplet {

	@Override
	public void setup() {
		size(800, 600);
		noLoop();
	}

	@Override
	public void draw() {

		PImage image = loadImage("board/board1.jpg");
		Pipeline pipeline = new Pipeline(this);

		image = pipeline.sobel(image, 0.3f);

		image(image, 0, 0);

	}


}
