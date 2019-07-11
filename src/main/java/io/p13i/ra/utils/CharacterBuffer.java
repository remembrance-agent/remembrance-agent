package io.p13i.ra.utils;

import java.util.LinkedList;
import java.util.Queue;

public class CharacterBuffer {

    private Queue<Character> queue;
    private int maximumSize;

    public CharacterBuffer(int maximumSize) {
        this.maximumSize = maximumSize;
        queue = new LinkedList<Character>();
    }

    public void add(char character) {
        if (queue.size() >= maximumSize) {
            queue.poll();
        }
        queue.add(character);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Character c : queue) {
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }
}
