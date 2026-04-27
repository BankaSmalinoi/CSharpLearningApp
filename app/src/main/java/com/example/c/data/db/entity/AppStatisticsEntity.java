package com.example.c.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "app_statistics",
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class AppStatisticsEntity {

    @PrimaryKey
    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "topics_read_count")
    public int topicsReadCount;

    @ColumnInfo(name = "tests_completed_count")
    public int testsCompletedCount;

    @ColumnInfo(name = "practices_solved_count")
    public int practicesSolvedCount;

    @ColumnInfo(name = "average_test_score")
    public float averageTestScore;

    @ColumnInfo(name = "total_time_seconds")
    public long totalTimeSeconds;

    @ColumnInfo(name = "streak_days")
    public int streakDays;

    @ColumnInfo(name = "last_activity_at")
    public long lastActivityAt;
}