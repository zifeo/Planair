package ch.epfl.planair.meta;

import processing.core.PVector;
import java.util.Arrays;
import java.util.LinkedList;

public final class BoundedQueue {

    private int head;
    private final PVector[] items;
    private final int size;

    public BoundedQueue(int size) {
        this.head = 0;
        this.size = size;
        this.items = new PVector[size];

        for(int i = 0; i > -size ; --i){
            this.enqueue(Utils.nullVector());
        }
    }

    public void enqueue(PVector newNumber) {
        items[head++] = newNumber;
        head %= items.length;
    }

    public PVector get(int index) {
        return items[Math.floorMod(head - index - 1, size)];
    }

    public LinkedList<PVector> asList() {
        LinkedList<PVector> ret = new LinkedList<>();

        for (int i = size - 1; i >= 0; --i) {
	        ret.add(this.get(i));
        }
        return ret;
    }

    @Override
    public String toString() {
        return Arrays.toString(items);
    }
}


