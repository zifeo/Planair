package ch.epfl.planair.visual;

import ch.epfl.planair.meta.BoundedQueue;
import ch.epfl.planair.meta.PipelineConfig;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class WebcamProcessor {

	private final Capture webcam;
	private final PApplet p;

	private final BoundedQueue queue;

	private final AtomicInteger rx;
	private final AtomicInteger ry;
	private final AtomicInteger rz;
	private final PipelineConfig config;
	private final PipelineOnPlace pipeline;
	private final TwoDThreeD twoDThreeD;

	private float lastFrameTime;
	private float lastCalcTime;
	private float frameTimeLength;
	private float calculTimeLength;

	private Thread runner;

	public WebcamProcessor(PApplet p, Capture webcam/*, PipelineConfig config*/) {
		this.p = p;
		this.webcam = webcam;
		this.queue = new BoundedQueue(3);
		this.config = new PipelineConfig();
		this.rx = new AtomicInteger(0);
		this.ry = new AtomicInteger(0);
		this.rz = new AtomicInteger(0);
		this.runner = null;
		this.pipeline = new PipelineOnPlace(p);
		this.twoDThreeD = new TwoDThreeD(webcam.width, webcam.height);
	}

	public void start() {
		assert runner == null;
		webcam.start();
		runner = new Thread(new PipelineRunner(config));
		runner.start();
		p.println("Start");
	}

	public void stop() {
		runner.interrupt();
		runner = null;
		webcam.stop();
		p.println("end");
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

		private final PipelineConfig currentConfig;

		public PipelineRunner(PipelineConfig config) {
			this.currentConfig = config;
		}

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				if (webcam.available()) {
					webcam.read();
					PImage image = webcam.get();

					pipeline.selectHueThreshold(image, 50, 70, 0);

					pipeline.selectBrightnessThreshold(image, currentConfig.lower(PipelineConfig.Step.BRIGHTNESS), currentConfig.upper(PipelineConfig.Step.BRIGHTNESS), 0);
					pipeline.selectSaturationThreshold(image, currentConfig.lower(PipelineConfig.Step.SATURATION), currentConfig.upper(PipelineConfig.Step.SATURATION), 0);
					pipeline.binaryBrightnessThreshold(image, currentConfig.lower(PipelineConfig.Step.SOBEL), 0, 180);
					pipeline.convolute(image, PipelineOnPlace.gaussianKernel);
					pipeline.sobel(image, 0.35f);

					List<PVector> lines = pipeline.hough(image);
					List<PVector> corners = pipeline.getPlane(image, lines);

					if (corners.size() >= 8) {
						PVector r = twoDThreeD.get3DRotations(corners.subList(0, 4));

						rx.set(Float.floatToIntBits(r.x));
						ry.set(Float.floatToIntBits(r.z));
						rz.set(Float.floatToIntBits(-r.y));

					}
					p.println("d");
				}
			}
		}
	}

}