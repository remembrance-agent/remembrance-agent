package io.p13i.ra.engine;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ConfusionMatrix {

    private final Set<String> allWords;
    private final Set<String> allDocumentNames;
    private final Map<Index<String, String>, Double> confusionMatrix;

    public ConfusionMatrix() {
        this.allWords = new LinkedHashSet<>();
        this.allDocumentNames = new LinkedHashSet<>();
        this.confusionMatrix = new HashMap<>();
    }

    public void add(String word, String documentName, double score) {
        this.allWords.add(word);
        this.allDocumentNames.add(documentName);
        this.confusionMatrix.put(new Index<>(word, documentName), score);
    }

    @Override
    public String toString() {
        AsciiTable at = new AsciiTable() {{
            setTextAlignment(TextAlignment.CENTER);
        }};

        at.addRule();

        List<String> header = new LinkedList<>();
        header.add("---");
        header.addAll(allWords);

        at.addRow(header)
                .setTextAlignment(TextAlignment.CENTER);

        at.addRule();

        for (String documentName : allDocumentNames) {
            List<String> documentNameAndScoresForEachWord = new LinkedList<>();
            documentNameAndScoresForEachWord.add(documentName);

            for (String word : allWords) {
                double score = this.confusionMatrix.get(new Index<>(word, documentName));
                documentNameAndScoresForEachWord.add(String.format("%03f", score));
            }

            at.addRow(documentNameAndScoresForEachWord)
                    .setTextAlignment(TextAlignment.CENTER);
            at.addRule();
        }

        at.addRule();

        return at.render(/* columns: */);
    }

    class Index<T1, T2> {
        private final T1 first;
        private final T2 second;

        Index(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public int hashCode() {
            return (first.hashCode() * second.hashCode()) % 31;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Index)) {
                return false;
            }

            Index otherIndex = (Index) obj;

            return first.equals(otherIndex.first) && second.equals(otherIndex.second);
        }
    }
}
