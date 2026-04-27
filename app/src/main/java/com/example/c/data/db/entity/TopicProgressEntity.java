package com.example.c.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Прогресс чтения теоретической темы.
 *
 * Контент теории остаётся в JSON, а изменяемое состояние пользователя хранится в Room.
 */
@Entity(tableName = "topic_progress")
public class TopicProgressEntity {

    @PrimaryKey
    @NonNull
    public String topicId;

    public int readPercent;
    public int progressPercent;
    public int percent;
    public int scrollY;
    public boolean isRead;
    public long updatedAt;

    // Поля-алиасы оставлены для совместимости с уже существующим кодом статистики.
    public boolean isCompleted;
    public boolean completed;
    public long completedAt;
    public int userId;

    public TopicProgressEntity() {
        topicId = "";
        readPercent = 0;
        progressPercent = 0;
        percent = 0;
        scrollY = 0;
        isRead = false;
        isCompleted = false;
        completed = false;
        updatedAt = System.currentTimeMillis();
        completedAt = 0L;
        userId = 1;
    }

    public TopicProgressEntity(@NonNull String topicId, int readPercent, int scrollY, boolean isRead, long updatedAt) {
        this.topicId = topicId;
        this.readPercent = Math.max(0, Math.min(100, readPercent));
        this.progressPercent = this.readPercent;
        this.percent = this.readPercent;
        this.scrollY = Math.max(0, scrollY);
        this.isRead = isRead || this.readPercent >= 100;
        this.updatedAt = updatedAt;
        this.isCompleted = this.isRead;
        this.completed = this.isRead;
        this.completedAt = this.isRead ? updatedAt : 0L;
        this.userId = 1;
    }

    public boolean isActuallyRead() {
        return isRead || isCompleted || completed || readPercent >= 100 || progressPercent >= 100 || percent >= 100;
    }
}
