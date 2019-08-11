package io.p13i.ra.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;


/**
 * Simple wrapper around a queue that limits the size of a queue and tracks the last element added to the queue.
 * @param <T> The type of elements in the buffer
 */
public class LimitedCapacityBuffer<T> implements Iterable<T> {

    private final LinkedList<T> queue;
    private int totalAddedElementsCount = 0;
    private T lastAddedElement;
    private final int maximumSize;

    public LimitedCapacityBuffer(int maximumSize) {
        this.maximumSize = maximumSize;
        queue = new LinkedList<>();
    }

    /**
     * Adds a new element removing the oldest-added element if there is not more space
     * @param element the element to add
     */
    public void add(T element) {
        if (queue.size() >= maximumSize) {
            queue.poll();
        }
        queue.add(element);
        totalAddedElementsCount++;
        lastAddedElement = element;
    }

    /**
     * Empties the buffer
     */
    public void clear() {
        while (!queue.isEmpty()) {
            queue.poll();
        }
    }

    @Override
    public String toString() {
        return LINQList.from(this.queue).toString();
    }

    /**
     * The number of elements currently in the buffer
     * @return a count
     */
    public int size() {
        return queue.size();
    }

    /**
     * The element last interested into the buffer
     * @return the last element added
     */
    T getLastAddedElement() {
        return lastAddedElement;
    }

    /**
     * Removes the last added element
     * @return the element removed
     */
    public T removeLast() {
        if (queue.isEmpty()) {
            return null;
        }

        return queue.removeLast();
    }

    /**
     * Gets the total number of characters added to the buffer
     * @return a count
     */
    int getTotalAddedElementsCount() {
        return totalAddedElementsCount;
    }

    /**
     * Indicates if the buffer is empty
     * @return whether the buffer is empty
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return queue.iterator();
    }
}
