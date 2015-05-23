package ch.epfl.planair.visual;

import ch.epfl.planair.meta.BoundedQueue;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class WebcamProcessor {

    private final Capture cam;
    private final PipelineOnplace pipeline;
    private final TwoDThreeD twoDThreeD;
    private final PApplet parent;

    private final BoundedQueue queue;
    private final AtomicInteger rx;
    private final AtomicInteger ry;
    private final AtomicInteger rz;

    private float lastFrameTime;
    private float lastCalcTime;
    private float frameTimeLength;
    private float calculTimeLength;

    private boolean run;

    public WebcamProcessor(PApplet parent){
        this.queue = new BoundedQueue(3);
        this.parent = parent;

        String[] cameras = Capture.list();

        if (cameras.length == 0) {
            throw new IllegalStateException("No camera");
        }

        //println("Cameras:");
        for (int i = 0; i < cameras.length; i++) {
            //println(cameras[i]);
        }
        this.cam = new Capture(parent, 640, 480, 15);
        // cam = new Capture(this, cameras[4]);
        this.cam.start();

        this.rx = new AtomicInteger(0);
        this.ry = new AtomicInteger(0);
        this.rz = new AtomicInteger(0);

        this.pipeline = new PipelineOnplace(parent);
		this.twoDThreeD = new TwoDThreeD(cam.width, cam.height);

        Thread update = new Thread(new updater());
        update.start();
	}


	public PVector getRotation(){
        PVector r = new PVector(Float.intBitsToFloat(rx.get()),
                        Float.intBitsToFloat(0),
                        Float.intBitsToFloat(rz.get()));

        if (queue.get(0) != r) {
            queue.enqueue(r);
            float newTime = parent.millis();
            calculTimeLength = newTime - lastCalcTime;
            lastCalcTime = newTime;

            return r;
        }
        else {
            float newTime = parent.millis();
            frameTimeLength = newTime - lastFrameTime;
            lastFrameTime = newTime;
        }

        return r;
	}


    private final class updater implements Runnable {

        @Override
        public void run() {
            while (true){

                if (cam.available()) {
                    cam.read();
                }

                PImage image = cam.get();
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

                if (corners.size() >= 8) {
                    PVector r = twoDThreeD.get3DRotations(corners.subList(0, 4));

                    rx.set(Float.floatToIntBits(r.x));
                    ry.set(Float.floatToIntBits(r.z));
                    rz.set(Float.floatToIntBits(-r.y));

                    //parent.println(r.x + " " + r.y);
                }
            }
        }
    }

}
