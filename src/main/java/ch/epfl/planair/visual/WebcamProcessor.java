package ch.epfl.planair.visual;

import ch.epfl.planair.BoundedQueue;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class WebcamProcessor {

    static private final int sizeInterp = 3;
    private final Capture cam;
    private final PipelineOnplace pipeline;
    private final TwoDThreeD twoDThreeD;
    private final PApplet parent;

    private final BoundedQueue yQueue;
    private final AtomicInteger rx;
    private final AtomicInteger ry;
    private final AtomicInteger rz;
    private final AtomicBoolean changed;

    private float discStep = 0;

    private boolean run;
    private int pos = 0;

    public WebcamProcessor(PApplet parent){
        this.yQueue = new BoundedQueue(sizeInterp);
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

        changed = new AtomicBoolean(false);

        Thread update = new Thread(new updater());
        update.start();
	}


	public PVector getRotation(){
        PVector r;

        if (!changed.get()) {
            discStep++;
            r = splineInterpolation();
            parent.println("interpolation : " + r);
            //yQueue.enqueue(r);
        } else {

            r = new PVector(Float.intBitsToFloat(rx.get()),
                    Float.intBitsToFloat(0),
                    Float.intBitsToFloat(rz.get()));

            yQueue.enqueue(r);
            changed.set(false);
            discStep = 0;
            parent.println("cam rotation : " + r);
        }

        //parent.println(r);

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

        PVector px = new PVector(0,0,0);

        List<PVector> list = yQueue.asList();

        px.add(list.get(0));

        for(int i = 1; i < sizeInterp; i++){
            PVector d = delta(list.subList(0, i+1));
            for (int j = 0; j < i; j++) {
                d.mult(sizeInterp - 1 + discStep*1/8.0f - j);
            }

            px.add(d);
        }

        //px.mult(0.8f);

        return px;
    }

    private PVector delta(List<PVector> list){
        if (list.size() == 1) {
            PVector ret = new PVector(0,0,0);
            ret.add(list.get(0));

            return ret;
        }
        else{
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

                changed.set(true);
            }
        }
    }

}
