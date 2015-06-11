package ch.epfl.planair.visual;

import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;
import processing.video.Movie;

public class MovieCaptureAdaptor extends Capture{

    private final Movie movie;

    public MovieCaptureAdaptor(PApplet pApplet, Movie movie) {
        super(pApplet);
        this.movie = movie;
    }

    @Override
    public PImage get() {
        return movie.get();
    }

    @Override
    public boolean available(){
        return movie.available();
    }

    @Override
    public void start() {
        movie.loop();
    }

    @Override
    public void stop(){
        movie.stop();
    }

    @Override
    public synchronized void read() {
        movie.read();
    }

}
