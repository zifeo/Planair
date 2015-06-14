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
		this.twoDThreeD = new TwoDThreeD(webcam.width, webcam.height);
        this.yQueue = new BoundedQueue(sizeInterp);
        this.changed = new AtomicBoolean(false);
		this.size = webcam.width * webcam.height;
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

	public PVector rotation() {
		PVector r;

		if (!changed.get()) {
			discStep++;
			r = splineInterpolation();
			//yQueue.enqueue(r);
		} else {

			r = new PVector(Float.intBitsToFloat(rx.get()),
					Float.intBitsToFloat(0),
					Float.intBitsToFloat(rz.get()));


			if (discStep > 6)
				lastDiscStep = discStep;
			else
				lastDiscStep = 6;

			yQueue.enqueue(r);
			changed.set(false);
			discStep = 0;
		}
		return r;
	}

    private PVector splineInterpolation(){
        /*PVector a[] = new PVector[sizeInterp];
        PVector b[] = new PVector[sizeInterp];

        for(int i = 0; i <= sizeInterp/2; ++i){
            a[i] = new PVector(0,0,0);
            b[i] = new PVector(0,0,0);

            for(int j = 0; j < sizeInterp; ++j){
                PVector ai = new PVector(0,0,0);
                ai.add(yQueue.get(j).r);
                PVector bi = new PVector(0,0,0);
                bi.add(yQueue.get(j).r);

                ai.mult(((float) Math.cos(j * yQueue.get(j).t)));
                a[i].add(ai);

                bi.mult(((float) Math.sin(j * yQueue.get(j).t)));
                b[i].add(bi);

                parent.println("ai = " + a[i]);
                parent.println("bi = " + b[i]);

            }

            a[i].mult(2.0f / sizeInterp);
            b[i].mult(2.0f / sizeInterp);


            parent.println("ai = " + a[i]);
            parent.println("bi = " + b[i]);

        }

        PVector px = a[0];
        px.div(2);

        for(int i = 1; i < sizeInterp/2; ++i){
            a[i].mult((float)Math.cos(i * x));
            b[i].mult((float)Math.sin(i * x));

            px.add(a[i]);
            px.add(b[i]);
        }

        a[sizeInterp/2].mult(1/2.0f * (float) Math.cos(sizeInterp * x));
        b[sizeInterp/2].mult((float) Math.sin(sizeInterp * x));
        px.add(a[sizeInterp/2]);
        px.add(b[sizeInterp/2]);

        //a[sizeInterp / 2].mult((float) Math.cos(sizeInterp/2 * x));
        //px.add(a[sizeInterp / 2]);
        //px.mult(0.8f);*/

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

					pipeline.convolute(image, PipelineOnPlace.gaussianKernel);
					pipeline.sobel(image, currentConfig.lower(PipelineConfig.Step.SOBEL), size);
					List<PVector> corners = pipeline.getPlane(image, pipeline.hough(image));

					if (!corners.isEmpty()) {
						PVector r = twoDThreeD.get3DRotations(corners.subList(0, 4));
						PVector prev = new PVector(Float.intBitsToFloat(rx.get()), Float.intBitsToFloat(ry.get()), Float.intBitsToFloat(rz.get()));

						// smooths and gets rids of extravagant changes
						if (Math.abs(r.x - prev.x) < PConstants.THIRD_PI) {
							rx.set(Float.floatToIntBits((r.x + prev.x)/2));
						}
						if (Math.abs(r.z - prev.y) < PConstants.THIRD_PI) {
							ry.set(Float.floatToIntBits((r.z + prev.y)/2));
						}
						if (Math.abs(-r.y - prev.z) < PConstants.THIRD_PI) {
							rz.set(Float.floatToIntBits((-r.y + prev.z)/2));
						}
					}

                }

                changed.set(true);
            }
        }
    }

}