package com.example.c.data.model.test;

public class TestOption {
    public String id;
    public String text;

    // В старом коде TestPassingActivity обращается именно к полю option.isCorrect.
    public boolean isCorrect;

    // Оставлено как алиас для JSON/старых моделей, где поле могло называться "correct".
    public boolean correct;

    public TestOption() {
    }

    public TestOption(String id, String text, boolean isCorrect) {
        this.id = id;
        this.text = text;
        this.isCorrect = isCorrect;
        this.correct = isCorrect;
    }

    public TestOption(String text, boolean isCorrect) {
        this(null, text, isCorrect);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text != null ? text : "";
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCorrect() {
        return isCorrect || correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
        this.isCorrect = correct;
    }

    public boolean getCorrect() {
        return isCorrect();
    }

    public void setIsCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
        this.correct = isCorrect;
    }
}
