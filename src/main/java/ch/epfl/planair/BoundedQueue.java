package ch.epfl.planair;


import processing.core.PVector;
import java.util.Arrays;

public class BoundedQueue {

    private int head;
    private final PVector[] items;
    private final int size;

    public BoundedQueue(int size) {
        this.head = 0;
        this.size = size;
        this.items = new PVector[size];
    }

    public void enqueue(PVector newNumber) {
        items[head++] = newNumber;
        head %= items.length;
    }

    public PVector get(int index) {
        return items[Math.floorMod(head - index, items.length)];
    }

    @Override
    public String toString() {
        return Arrays.toString(items);
    }
}
