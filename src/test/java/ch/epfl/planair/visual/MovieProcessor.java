package ch.epfl.planair.visual;

import processing.core.PApplet;
import processing.video.Movie;

public final class MovieProcessor extends PApplet {

	private Movie cam;

	@Override
	public void setup(){
		size(640, 480);
		cam = new Movie(this, getClass().getResource("/movie/testvideo.mp4").getPath());
		cam.loop();
	}

	@Override
	public void draw() {
		if (cam.available()) {
			cam.read();
		}
		image(cam, 0, 0);
	}

}