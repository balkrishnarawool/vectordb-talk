package com.balarawool.vectordb.example5;

import java.util.List;

public class QuestionDocument {
    private String id;
    private String text;
    private List<Float> textVector;

    public QuestionDocument() {
    }

    public QuestionDocument(String id, String text, List<Float> textVector) {
        this.id = id;
        this.text = text;
        this.textVector = textVector;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public List<Float> getTextVector() {
        return textVector;
    }
}
