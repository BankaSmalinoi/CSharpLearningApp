package com.example.c.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.c.data.db.entity.TopicProgressEntity;

import java.util.List;

@Dao
public interface TopicProgressDao {

    @Query("SELECT * FROM topic_progress ORDER BY updatedAt DESC")
    LiveData<List<TopicProgressEntity>> observeAllProgress();

    @Query("SELECT * FROM topic_progress ORDER BY updatedAt DESC")
    LiveData<List<TopicProgressEntity>> observeAll();

    @Query("SELECT * FROM topic_progress")
    List<TopicProgressEntity> getAllProgressSync();

    @Query("SELECT * FROM topic_progress")
    List<TopicProgressEntity> getAllSync();

    @Query("SELECT * FROM topic_progress WHERE topicId = :topicId LIMIT 1")
    LiveData<TopicProgressEntity> observeProgress(String topicId);

    @Query("SELECT * FROM topic_progress WHERE topicId = :topicId LIMIT 1")
    TopicProgressEntity getProgress(String topicId);

    @Query("SELECT * FROM topic_progress WHERE topicId = :topicId LIMIT 1")
    TopicProgressEntity getProgressSync(String topicId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveProgress(TopicProgressEntity progress);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TopicProgressEntity progress);

    @Update
    void update(TopicProgressEntity progress);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(TopicProgressEntity progress);

    @Query("UPDATE topic_progress SET readPercent = :readPercent, scrollY = :scrollY, isRead = :isRead, isCompleted = :isRead, completedAt = :completedAt, updatedAt = :updatedAt WHERE topicId = :topicId")
    void updateProgress(String topicId, int readPercent, int scrollY, boolean isRead, long completedAt, long updatedAt);

    @Query("SELECT COUNT(*) FROM topic_progress WHERE topicId = :topicId AND (isRead = 1 OR isCompleted = 1 OR completed = 1 OR readPercent >= 100 OR progressPercent >= 100 OR percent >= 100)")
    int isTheoryReadInt(String topicId);

    @Query("SELECT COUNT(*) FROM topic_progress WHERE topicId = :topicId AND (isRead = 1 OR isCompleted = 1 OR completed = 1 OR readPercent >= 100 OR progressPercent >= 100 OR percent >= 100)")
    int isTopicReadInt(String topicId);

    @Query("SELECT COUNT(*) FROM topic_progress WHERE isRead = 1 OR isCompleted = 1 OR readPercent >= 100")
    int countReadTopicsSync();
}
