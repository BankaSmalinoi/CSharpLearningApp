package com.example.c.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_solution_history")
public class TaskSolutionHistoryEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String taskId;
    public String taskTitle;

    public boolean accepted;
    public int percent;

    public String userCode;

    public long solvedAt;

    public TaskSolutionHistoryEntity(
            String taskId,
            String taskTitle,
            boolean accepted,
            int percent,
            String userCode,
            long solvedAt
    ) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.accepted = accepted;
        this.percent = percent;
        this.userCode = userCode;
        this.solvedAt = solvedAt;
    }
}