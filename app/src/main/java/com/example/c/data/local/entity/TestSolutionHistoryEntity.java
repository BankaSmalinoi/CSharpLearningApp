package com.example.c.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "test_solution_history")
public class TestSolutionHistoryEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String testId;
    public String testTitle;

    public int correctAnswers;
    public int totalQuestions;
    public int percent;

    public long solvedAt;

    public TestSolutionHistoryEntity(
            String testId,
            String testTitle,
            int correctAnswers,
            int totalQuestions,
            int percent,
            long solvedAt
    ) {
        this.testId = testId;
        this.testTitle = testTitle;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
        this.percent = percent;
        this.solvedAt = solvedAt;
    }
}