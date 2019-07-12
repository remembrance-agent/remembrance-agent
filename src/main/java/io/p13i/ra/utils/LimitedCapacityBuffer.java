package io.p13i.ra.utils;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class LimitedCapacityBuffer<T> {

    private Queue<T> queue;
    private T lastAddedElement;
    private int maximumSize;

    public LimitedCapacityBuffer(int maximumSize) {
        this.maximumSize = maximumSize;
        queue = new LinkedList<T>();
    }

    public void add(T element) {
        if (queue.size() >= maximumSize) {
            queue.poll();
        }
        queue.add(element);
        lastAddedElement = element;
    }

    public T peek() {
        return queue.peek();
    }

    public T poll() {
        return queue.poll();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (T c : queue) {
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    public int size() {
        return queue.size();
    }

    public T getLastAddedElement() {
        return lastAddedElement;
    }
}
