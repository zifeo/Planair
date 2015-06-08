package ch.epfl.planair.visual;

import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;
import processing.video.Movie;

/**
 * Created by Nicolas on 08.06.15.
 */
public class MovieCaptureAdaptor extends Capture{

    private final Movie movie;

    public MovieCaptureAdaptor(PApplet pApplet, Movie movie) {
        super(pApplet);
        this.movie = movie;
    }

    @Override
    public PImage get() {
        return movie;
    }

    public float width(){
        return movie.width;
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
