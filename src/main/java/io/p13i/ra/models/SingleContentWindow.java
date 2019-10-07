package io.p13i.ra.models;

import java.util.List;

public class SingleContentWindow {
    private List<String> wordVector;
    public SingleContentWindow(List<String> wordVector) {
        this.wordVector = wordVector;
    }

    public List<String> getWordVector() {
        return wordVector;
    }
}
