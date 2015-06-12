package ch.epfl.planair.visual;

import ch.epfl.planair.meta.BoundedQueue;
import ch.epfl.planair.meta.Consts;
import ch.epfl.planair.meta.PipelineConfig;
import ch.epfl.planair.meta.Utils;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class WebcamProcessor {

	private final static int sizeInterp = 2;

	private final Capture webcam;
	private final PApplet p;

	private final AtomicInteger rx;
	private final AtomicInteger ry;
	private final AtomicInteger rz;
	private final PipelineConfig config;
	private final PipelineOnPlace pipeline;
	private final TwoDThreeD twoDThreeD;
	private final int size;
    private final BoundedQueue yQueue;
    private final AtomicBoolean changed;

	private Thread runner;
    private int discStep = 0;
	private int lastDiscStep = 6;

	public WebcamProcessor(PApplet p, Capture webcam, PipelineConfig config) {
		this.p = p;
		this.webcam = webcam;
		this.config = config;
		this.rx = new AtomicInteger(0);
		this.ry = new AtomicInteger(0);
		this.rz = new AtomicInteger(0);
		this.runner = null;
		this.pipeline = new PipelineOnPlace(p);
		this.twoDThreeD = new TwoDThreeD(Consts.CAMERA_WIDTH, Consts.CAMERA_HEIGHT);
        this.yQueue = new BoundedQueue(sizeInterp);
        this.changed = new AtomicBoolean(false);
		this.size = Consts.CAMERA_WIDTH * Consts.CAMERA_HEIGHT;
	}

	public void start() {
		assert runner == null;
		//webcam.start();
		runner = new Thread(new PipelineRunner(config.snapshot()));
		runner.start();
	}

	public void stop() {
		runner.interrupt();
		runner = null;
		//webcam.stop();
	}

	public PImage get(){
        webcam.read();
		return webcam.get();
	}

	public PVector rotation() {
		PVector r;

		/*if (!changed.get()) {
			discStep++;
			r = splineInterpolation();
			//yQueue.enqueue(r);
		} else {*/
			r = new PVector(Float.intBitsToFloat(rx.get()),
					Float.intBitsToFloat(ry.get()),
					Float.intBitsToFloat(rz.get()));


			/*if (discStep > 6)
				lastDiscStep = discStep;
			else
				lastDiscStep = 6;

			yQueue.enqueue(r);
			changed.set(false);
			discStep = 0;*/
		//}
		return r;
	}

    private PVector splineInterpolation(){

        PVector px = Utils.nullVector();

        LinkedList<PVector> list = yQueue.asList();

        px.add(list.get(0));

        for(int i = 1; i < sizeInterp; i++){
            PVector d = delta(list.subList(0, i+1));
            for (int j = 0; j < i; j++) {
                d.mult(sizeInterp - 1 + discStep*1.0f/lastDiscStep - j);
            }
			d.mult(0.8f);
            px.add(d);
        }

        //px.mult(0.8f);

        return px;
    }

    private PVector delta(List<PVector> list){
        if (list.size() == 1) {
            PVector ret = Utils.nullVector();
            ret.add(list.get(0));

            return ret;
        } else{
            //parent.println("size : " + list.size());
            PVector d1 = delta(list.subList(1, list.size()));
            d1.sub(delta(list.subList(0,list.size() - 1)));
            //parent.println("d1 before div" + d1);
            float div = (float) (list.size()-1);
            if (div != 0)
                d1.div(div);
            else
                d1.mult(0);

            //parent.println("d1 after div" + d1);
            return d1;
        }

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

					for (int i = 0; i < size; ++i) {
						float h = p.hue(image.pixels[i]);
						float b = p.brightness(image.pixels[i]);
						float s = p.saturation(image.pixels[i]);
						if (currentConfig.lower(PipelineConfig.Step.HUE) < h && h < currentConfig.upper(PipelineConfig.Step.HUE) &&
								currentConfig.lower(PipelineConfig.Step.BRIGHTNESS) < b && b < currentConfig.upper(PipelineConfig.Step.BRIGHTNESS) &&
								currentConfig.lower(PipelineConfig.Step.SATURATION) < s && s < currentConfig.upper(PipelineConfig.Step.SATURATION)) {
							image.pixels[i] = Consts.COLORBG;
						} else {
							image.pixels[i] = Consts.BLACK;
						}
					}
					image.updatePixels();

					/*
					pipeline.selectHueThreshold(image, currentConfig.lower(PipelineConfig.Step.HUE), currentConfig.upper(PipelineConfig.Step.HUE), 0);
					pipeline.selectBrightnessThreshold(image, currentConfig.lower(PipelineConfig.Step.BRIGHTNESS), currentConfig.upper(PipelineConfig.Step.BRIGHTNESS), 0);
					pipeline.selectSaturationThreshold(image, currentConfig.lower(PipelineConfig.Step.SATURATION), currentConfig.upper(PipelineConfig.Step.SATURATION), 0);
					pipeline.binaryBrightnessThreshold(image, currentConfig.lower(PipelineConfig.Step.SOBEL), 0, 180);
					*/

					//pipeline.convolute(image, PipelineOnPlace.gaussianKernel);
					image.filter(PConstants.BLUR, 2);
					pipeline.sobel(image, currentConfig.lower(PipelineConfig.Step.SOBEL), size);
					List<PVector> corners = pipeline.getPlane(image, pipeline.hough(image));

					if (!corners.isEmpty()) {
						PVector r = twoDThreeD.get3DRotations(corners.subList(0, 4));

						//if (PVector.sub(r, new PVector(Float.intBitsToFloat(rx.get()), Float.intBitsToFloat(rz.get()), -Float.intBitsToFloat(ry.get()))).mag() < 4) {

							rx.set(Float.floatToIntBits(r.x));
							ry.set(Float.floatToIntBits(r.z));
							rz.set(Float.floatToIntBits(-r.y));
						//}
					}

                }

                changed.set(true);
            }
        }
    }

}