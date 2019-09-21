package io.p13i.ra.models;

import java.util.List;

public class ContentWindow {
    private List<String> wordVector;
    public ContentWindow(List<String> wordVector) {
        this.wordVector = wordVector;
    }

    public List<String> getWordVector() {
        return wordVector;
    }
}
