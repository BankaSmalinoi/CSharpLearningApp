package com.example.c.ui.practice;

public class PracticeTaskData {
    public String id = "";
    public String topicId = "";
    public String topicTitle = "";
    public String title = "Практическое задание";
    public String description = "";
    public String taskText = "";
    public String difficulty = "";
    public boolean requiresTheoryRead = true;
    public String inputExample = "";
    public String outputExample = "";
    public String hint = "";
    public String solutionExample = "";
    public String requirementsText = "";

    public String getTitle() {
        return isBlank(title) ? "Практическое задание" : title;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public String getTaskText() {
        return taskText == null ? "" : taskText;
    }

    public String getDifficulty() {
        return difficulty == null ? "" : difficulty;
    }

    public String getInputExample() {
        return inputExample == null ? "" : inputExample;
    }

    public String getOutputExample() {
        return outputExample == null ? "" : outputExample;
    }

    public String getHint() {
        return hint == null ? "" : hint;
    }

    public String getSolutionExample() {
        return solutionExample == null ? "" : solutionExample;
    }

    public String getRequirementsText() {
        return requirementsText == null ? "" : requirementsText;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
