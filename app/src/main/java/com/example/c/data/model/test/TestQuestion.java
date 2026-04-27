package com.example.c.data.model.test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TestQuestion {
    public String id;
    public String type;

    // Старый экран прохождения теста использует question.text.
    public String text;

    // В новом JSON вопрос может называться question.
    public String question;

    public List<TestOption> options;

    // Для radio/input старый код ожидает String, а не Integer.
    public String correctAnswer;

    // Для checkbox старый код ожидает List<String>, а не List<Integer>.
    public List<String> correctAnswers;

    // Для вопросов с ручным вводом.
    public List<String> answers;

    // Для matching.
    public List<String> left;
    public List<String> right;
    public Map<String, String> correctPairs;

    public String explanation;

    public TestQuestion() {
        options = new ArrayList<>();
        correctAnswers = new ArrayList<>();
        answers = new ArrayList<>();
        left = new ArrayList<>();
        right = new ArrayList<>();
        correctPairs = new LinkedHashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type != null ? type : "";
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        if (text != null && !text.trim().isEmpty()) {
            return text;
        }
        return question != null ? question : "";
    }

    public void setText(String text) {
        this.text = text;
        this.question = text;
    }

    public String getQuestion() {
        return getText();
    }

    public void setQuestion(String question) {
        this.question = question;
        if (text == null || text.trim().isEmpty()) {
            this.text = question;
        }
    }

    public List<TestOption> getOptions() {
        if (options == null) {
            options = new ArrayList<>();
        }
        return options;
    }

    public void setOptions(List<TestOption> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer != null ? correctAnswer : "";
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public List<String> getCorrectAnswers() {
        if (correctAnswers == null) {
            correctAnswers = new ArrayList<>();
        }
        return correctAnswers;
    }

    public void setCorrectAnswers(List<String> correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public List<String> getAnswers() {
        if (answers == null) {
            answers = new ArrayList<>();
        }
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public List<String> getLeft() {
        if (left == null) {
            left = new ArrayList<>();
        }
        return left;
    }

    public void setLeft(List<String> left) {
        this.left = left;
    }

    public List<String> getRight() {
        if (right == null) {
            right = new ArrayList<>();
        }
        return right;
    }

    public void setRight(List<String> right) {
        this.right = right;
    }

    public Map<String, String> getCorrectPairs() {
        if (correctPairs == null) {
            correctPairs = new LinkedHashMap<>();
        }
        return correctPairs;
    }

    public void setCorrectPairs(Map<String, String> correctPairs) {
        this.correctPairs = correctPairs;
    }

    public String getExplanation() {
        return explanation != null ? explanation : "";
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
