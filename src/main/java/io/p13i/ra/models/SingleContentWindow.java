package io.p13i.ra.models;

import java.util.List;
import java.util.stream.Stream;

public class SingleContentWindow {
    private List<String> wordVector;
    public SingleContentWindow(List<String> wordVector) {
        this.wordVector = wordVector;
    }

    public List<String> getWordVector() {
        return wordVector;
    }

    public Stream<String> stream() {
        return Stream.of(this.wordVector.toArray(new String[0]));
    }
}
