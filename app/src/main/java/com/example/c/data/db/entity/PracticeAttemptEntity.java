package com.example.c.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(
        tableName = "practice_attempts",
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
                @Index("practice_id")
        }
)
public class PracticeAttemptEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @NonNull
    @ColumnInfo(name = "practice_id")
    public String practiceId = "";

    @ColumnInfo(name = "submitted_code")
    public String submittedCode;

    @ColumnInfo(name = "is_successful")
    public boolean isSuccessful;

    @ColumnInfo(name = "result_message")
    public String resultMessage;

    @ColumnInfo(name = "created_at")
    public long createdAt;
}