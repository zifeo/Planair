package ch.epfl.planair.visual;

import ch.epfl.planair.meta.BoundedQueue;
import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.meta.PipelineConfig;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class WebcamProcessor {

	private final Capture webcam;
    private final PApplet p;

    private final BoundedQueue queue;

    private final AtomicInteger rx;
    private final AtomicInteger ry;
    private final AtomicInteger rz;
	private final AtomicBoolean pipelining;
	public static final AtomicReference<PipelineConfig> config;

    private float lastFrameTime;
    private float lastCalcTime;
    private float frameTimeLength;
    private float calculTimeLength;

	static {
		config = new AtomicReference<>(new PipelineConfig());
	}

    public WebcamProcessor(PApplet p, Capture webcam){
	    this.p = p;
		this.webcam = webcam;
        this.queue = new BoundedQueue(3);
        this.rx = new AtomicInteger(0);
        this.ry = new AtomicInteger(0);
        this.rz = new AtomicInteger(0);
	    this.pipelining = new AtomicBoolean(false);

	    new Thread(new PipelineRunner()).start();
	}

	public void start() {
		pipelining.set(true);
		webcam.start();
	}

	public void stop() {
		pipelining.set(false);
		webcam.stop();
	}

	public PVector rotation() {
        PVector r = new PVector(Float.intBitsToFloat(rx.get()),
                        Float.intBitsToFloat(0),
                        Float.intBitsToFloat(rz.get()));

        if (queue.get(0) != r) {
            queue.enqueue(r);
            float newTime = p.millis();
            calculTimeLength = newTime - lastCalcTime;
            lastCalcTime = newTime;

        } else {
            float newTime = p.millis();
            frameTimeLength = newTime - lastFrameTime;
            lastFrameTime = newTime;
        }

        return r;
	}

    private final class PipelineRunner implements Runnable {

	    private final PipelineOnPlace pipeline;
	    private final TwoDThreeD twoDThreeD;

	    private PipelineRunner() {
		    this.pipeline = new PipelineOnPlace(p);
		    this.twoDThreeD = new TwoDThreeD(webcam.width, webcam.height);
	    }

	    @Override
        public void run() {
		    try {
                while (true) {
	                if (pipelining.get() && webcam.available()) {
		                webcam.read();
		                PImage image = webcam.get();
		                //p.image(image, 0, 0);
		                //result.resize(p.width/3, p.height/4);

		                pipeline.selectHueThreshold(image, 80, 125, 0);
		                //result = pipeline.selectHueThreshold(result, 95, 140, 0);
		                pipeline.selectBrightnessThreshold(image, 30, 240, 0);
		                pipeline.selectSaturationThreshold(image, 80, 255, 0);
		                pipeline.binaryBrightnessThreshold(image, 20, 0, 180);
		                pipeline.convolute(image, PipelineOnPlace.gaussianKernel);
		                pipeline.sobel(image, 0.35f);

		                // Partie QUAD a refactorer
		                List<PVector> lines = pipeline.hough(image);
		                List<PVector> corners = pipeline.getPlane(image, lines);

		                if (corners.size() >= 8) {
			                PVector r = twoDThreeD.get3DRotations(corners.subList(0, 4));

			                rx.set(Float.floatToIntBits(r.x));
			                ry.set(Float.floatToIntBits(r.z));
			                rz.set(Float.floatToIntBits(-r.y));

			                //p.println(r.x + " " + r.y);
		                }
	                } else {
		                Thread.sleep((int) (1000 / webcam.frameRate));
	                }
                }
		    } catch (InterruptedException e) {
			    if (Consts.DEBUG) {
				    p.println("PipelineRunner exited.");
			    }
		    }
        }
    }

}
