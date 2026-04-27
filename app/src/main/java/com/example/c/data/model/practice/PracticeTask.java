package com.example.c.data.model.practice;

import java.util.ArrayList;
import java.util.List;

public class PracticeTask {
    public String id;
    public String topicId;
    public String title;
    public String description;
    public String taskText;
    public String difficulty;
    public boolean requiresTheoryRead = true;
    public String inputExample;
    public String outputExample;
    public String hint;
    public String solutionExample;

    // Алиасы для совместимости с альтернативной структурой JSON.
    public String gameContext;
    public String task;
    public String starterCode;
    public String expectedResult;
    public List<String> requirements;
    public List<String> hints;

    // Пока БД решений не подключаем. Поле оставлено на будущее и для возможной отладки.
    public boolean solved;

    public PracticeTask() {
        requirements = new ArrayList<>();
        hints = new ArrayList<>();
    }

    public String getId() {
        return id != null ? id : "";
    }

    public String getTopicId() {
        return topicId != null ? topicId : "";
    }

    public String getTitle() {
        return title != null ? title : "Практическое задание";
    }

    public String getDescription() {
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        return gameContext != null ? gameContext : "";
    }

    public String getTaskText() {
        if (taskText != null && !taskText.trim().isEmpty()) {
            return taskText;
        }
        return task != null ? task : "";
    }

    public String getDifficulty() {
        return difficulty != null ? difficulty : "";
    }

    public String getInputExample() {
        return inputExample != null ? inputExample : "";
    }

    public String getOutputExample() {
        if (outputExample != null && !outputExample.trim().isEmpty()) {
            return outputExample;
        }
        return expectedResult != null ? expectedResult : "";
    }

    public String getHint() {
        if (hint != null && !hint.trim().isEmpty()) {
            return hint;
        }
        if (hints != null && !hints.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (String item : hints) {
                if (item != null && !item.trim().isEmpty()) {
                    if (builder.length() > 0) builder.append("\n");
                    builder.append("• ").append(item);
                }
            }
            return builder.toString();
        }
        return "";
    }

    public String getSolutionExample() {
        if (solutionExample != null && !solutionExample.trim().isEmpty()) {
            return solutionExample;
        }
        return starterCode != null ? starterCode : "";
    }

    public String getRequirementsText() {
        if (requirements == null || requirements.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (String item : requirements) {
            if (item != null && !item.trim().isEmpty()) {
                if (builder.length() > 0) builder.append("\n");
                builder.append("• ").append(item);
            }
        }
        return builder.toString();
    }
}
