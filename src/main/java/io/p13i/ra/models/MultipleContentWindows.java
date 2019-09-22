package io.p13i.ra.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultipleContentWindows implements Iterable<SingleContentWindow> {
    private List<SingleContentWindow> singleContentWindowList;

    public MultipleContentWindows() {
        this(new ArrayList<>());
    }

    public MultipleContentWindows(List<SingleContentWindow> singleContentWindows) {
        this.singleContentWindowList = singleContentWindows;
    }

    public List<SingleContentWindow> getSingleWindows() {
        return singleContentWindowList;
    }

    public Stream<SingleContentWindow> stream() {
        return Stream.of(this.singleContentWindowList.toArray(new SingleContentWindow[0]));
    }

    public SingleContentWindow asSingleContentWindow() {
        return new SingleContentWindow(this.singleContentWindowList.stream()
                .flatMap(window -> window.getWordVector().stream())
                .collect(Collectors.toList()));
    }

    @Override
    public Iterator<SingleContentWindow> iterator() {
        return this.singleContentWindowList.iterator();
    }
}
