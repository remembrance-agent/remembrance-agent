package io.p13i.ra.utils;

import java.util.LinkedList;
import java.util.Queue;


/**
 * Simple wrapper around a queue that limits the size of a queue and tracks the last element added to the queue.
 * @param <T> The type of elements in the buffer
 */
public class LimitedCapacityBuffer<T> {

    private Queue<T> queue;
    private int totalAddedElementsCount = 0;
    private T lastAddedElement;
    private int maximumSize;

    public LimitedCapacityBuffer(int maximumSize) {
        this.maximumSize = maximumSize;
        queue = new LinkedList<>();
    }

    public void add(T element) {
        if (queue.size() >= maximumSize) {
            queue.poll();
        }
        queue.add(element);
        totalAddedElementsCount++;
        lastAddedElement = element;
    }

    public void clear() {
        while (!queue.isEmpty()) {
            queue.poll();
        }
    }

    @Override
    public String toString() {
        return toString("");
    }

    public String toString(String withSeperator) {
        StringBuilder stringBuilder = new StringBuilder();
        for (T c : queue) {
            stringBuilder.append(c);
            stringBuilder.append(withSeperator);
        }
        return stringBuilder.toString();
    }

    int size() {
        return queue.size();
    }

    T getLastAddedElement() {
        return lastAddedElement;
    }

    int getTotalAddedElementsCount() {
        return totalAddedElementsCount;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
