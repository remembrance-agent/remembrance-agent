package io.p13i.ra.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MultipleContentWindows implements Iterable<SingleContentWindow> {
    private List<SingleContentWindow> singleContentWindowList;

    public MultipleContentWindows() {
        this(new ArrayList<SingleContentWindow>());
    }

    public MultipleContentWindows(List<SingleContentWindow> singleContentWindows) {
        this.singleContentWindowList = singleContentWindows;
    }

    public List<SingleContentWindow> getSingleWindows() {
        return singleContentWindowList;
    }

    public SingleContentWindow asSingleContentWindow() {
        List<String> words = new LinkedList<>();
        for (SingleContentWindow window : singleContentWindowList) {
            words.addAll(window.getWordVector());
        }
        return new SingleContentWindow(words);
    }

    @Override
    public Iterator<SingleContentWindow> iterator() {
        return this.singleContentWindowList.iterator();
    }
}
