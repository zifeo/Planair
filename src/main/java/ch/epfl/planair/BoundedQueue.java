package ch.epfl.planair;


import processing.core.PVector;
import java.util.Arrays;

public class BoundedQueue {

    private int head;
    private final xyPair[] items;
    private final int size;

    public BoundedQueue(int size) {
        this.head = 0;
        this.size = size;
        this.items = new xyPair[size];

        for(int i = 0; i < size; ++i){
            this.items[i] = new xyPair(new PVector(5,5,5), 0);
        }
    }

    public void enqueue(PVector newNumber, double t) {
        items[head++] = new xyPair(newNumber, t);
        head %= items.length;
    }

    public xyPair get(int index) {
        return items[Math.floorMod(head - index, size)];
    }

    @Override
    public String toString() {
        return Arrays.toString(items);
    }

    public class xyPair {
        public PVector r;
        public double t;

        xyPair(PVector r, double t){
            this.r = r;
            this.t = t;
        }

    }
}

