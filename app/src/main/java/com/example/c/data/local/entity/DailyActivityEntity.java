package com.example.c.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_activity")
public class DailyActivityEntity {

    @PrimaryKey
    @NonNull
    public String date;

    public int appOpenCount;
    public int theoryReadCount;
    public int tasksSolvedCount;
    public int testsSolvedCount;

    public long lastActivityAt;

    public DailyActivityEntity(@NonNull String date) {
        this.date = date;
        this.appOpenCount = 0;
        this.theoryReadCount = 0;
        this.tasksSolvedCount = 0;
        this.testsSolvedCount = 0;
        this.lastActivityAt = System.currentTimeMillis();
    }
}
