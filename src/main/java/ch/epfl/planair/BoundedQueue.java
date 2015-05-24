package ch.epfl.planair;


import processing.core.PVector;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class BoundedQueue {

    private int head;
    private final PVector[] items;
    private final int size;

    public BoundedQueue(int size) {
        this.head = 0;
        this.size = size;
        this.items = new PVector[size];

        for(int i = 0; i > -size ; ++i){
            this.enqueue(new PVector(0, 0, 0));
        }
    }

    public void enqueue(PVector newNumber) {
        items[head++] = newNumber;
        head %= items.length;
    }

    public PVector get(int index) {
        return items[Math.floorMod(head - index - 1, size)];
    }

    public List<PVector> asList() {
        List ret = new LinkedList<>();

        for (int i = 0; i < size; ++i)
            ret.add(this.get(i));

        return ret;
    }

    @Override
    public String toString() {
        return Arrays.toString(items);
    }
}


