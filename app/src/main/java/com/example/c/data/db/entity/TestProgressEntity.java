package com.example.c.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "test_progress",
        primaryKeys = {"user_id", "test_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("user_id")
        }
)
public class TestProgressEntity {

    @ColumnInfo(name = "user_id")
    public int userId;

    @NonNull
    @ColumnInfo(name = "test_id")
    public String testId = "";

    @ColumnInfo(name = "best_score_percent")
    public int bestScorePercent;

    @ColumnInfo(name = "last_score_percent")
    public int lastScorePercent;

    @ColumnInfo(name = "attempts_count")
    public int attemptsCount;

    @ColumnInfo(name = "last_passed_at")
    public long lastPassedAt;
}