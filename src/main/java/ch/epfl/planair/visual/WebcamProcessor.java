package ch.epfl.planair.visual;

import cs211.imageprocessing.PipelineM3;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Nicolas on 17.05.15.
 */
public class WebcamProcessor {

    PApplet parent;
    private Capture cam;
    private PImage image;
    private Pipeline pipeline;
    private QuadGraph quad;
    private TwoDThreeD twoDThreeD;

    public WebcamProcessor(PApplet parent){
        this.parent = parent;
        String[] cameras = Capture.list();
        if (cameras.length == 0) {
            parent.println("No camera :( !");
            parent.exit();
        } else {
            //println("Cameras:");
            for (int i = 0; i < cameras.length; i++) {
                //println(cameras[i]);
            }
            cam = new Capture(parent, 640, 480, 15);
            // cam = new Capture(this, cameras[4]);
            cam.start();
        }
        pipeline = new Pipeline(parent);
        quad = new QuadGraph();

        twoDThreeD = new TwoDThreeD(cam.width, cam.height);
    }

    public PVector getRotation(){
        if (cam.available()) {
            cam.read();
        }

        image = cam.get();
        //parent.image(image, 0, 0);
        PImage result = image;
        //result.resize(parent.width/3, parent.height/4);

        result = pipeline.selectHueThreshold(result, 80, 125, 0);
        //result = pipeline.selectHueThreshold(result, 95, 140, 0);
        result = pipeline.selectBrightnessThreshold(result, 30, 240, 0);
        result = pipeline.selectSaturationThreshold(result, 80, 255, 0);
        result = pipeline.convolute(result, Pipeline.gaussianKernel);
        result = pipeline.binaryBrightnessThreshold(result, 20, 0, 180);
        result = pipeline.sobel(result, 0.35f);

        // Partie QUAD a refactorer
        List<PVector> lines = pipeline.hough(result);
        List<PVector> corners = pipeline.getPlane(result, lines);

        result.resize(parent.width/3, parent.height/4);
        parent.image(result, parent.width/2, -parent.height/2);

        if(corners.size() < 8)
            return new PVector(0, 0, 0);
        else
            return twoDThreeD.get3DRotations(corners.subList(0, 4));
    }
}
