package com.example.c.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(
        tableName = "test_attempts",
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("user_id"),
                @Index("test_id")
        }
)
public class TestAttemptEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @NonNull
    @ColumnInfo(name = "test_id")
    public String testId = "";

    @ColumnInfo(name = "score_percent")
    public int scorePercent;

    @ColumnInfo(name = "correct_answers")
    public int correctAnswers;

    @ColumnInfo(name = "total_questions")
    public int totalQuestions;

    @ColumnInfo(name = "passed_at")
    public long passedAt;
}