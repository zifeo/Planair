package ch.epfl.planair.visual;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;

import java.util.List;

public class WebcamProcessor {

    PApplet parent;
    private Capture cam;
    private PImage image;
    private PipelineOnplace pipeline;
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
        pipeline = new PipelineOnplace(parent);
        quad = new QuadGraph();

		twoDThreeD = new TwoDThreeD(cam.width, cam.height);
	}

	public PVector getRotation(){

		if (cam.available()) {
			cam.read();
		}

        image = cam.get();
        //p.image(image, 0, 0);
        //result.resize(p.width/3, p.height/4);

        pipeline.selectHueThreshold(image, 80, 125, 0);
        //result = pipeline.selectHueThreshold(result, 95, 140, 0);
        pipeline.selectBrightnessThreshold(image, 30, 240, 0);
        pipeline.selectSaturationThreshold(image, 80, 255, 0);
        pipeline.binaryBrightnessThreshold(image, 20, 0, 180);
        pipeline.convolute(image, Pipeline.gaussianKernel);
        pipeline.sobel(image, 0.35f);

        // Partie QUAD a refactorer
        List<PVector> lines = pipeline.hough(image);
        List<PVector> corners = pipeline.getPlane(image, lines);

		if(corners.size() < 8)
			return new PVector(0, 0, 0);
		else
			return twoDThreeD.get3DRotations(corners.subList(0, 4));
	}
}
