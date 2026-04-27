package com.example.c.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "practice_progress",
        primaryKeys = {"user_id", "practice_id"},
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
public class PracticeProgressEntity {

    @ColumnInfo(name = "user_id")
    public int userId;

    @NonNull
    @ColumnInfo(name = "practice_id")
    public String practiceId = "";

    @ColumnInfo(name = "is_solved")
    public boolean isSolved;

    @ColumnInfo(name = "attempts_count")
    public int attemptsCount;

    @ColumnInfo(name = "last_attempt_at")
    public long lastAttemptAt;
}