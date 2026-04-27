package com.example.c.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.c.data.db.entity.TestAttemptEntity;
import com.example.c.data.db.entity.TestProgressEntity;

import java.util.List;

@Dao
public interface TestProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertProgress(TestProgressEntity progress);

    @Insert
    long insertAttempt(TestAttemptEntity attempt);

    @Query("SELECT * FROM test_progress WHERE user_id = :userId")
    LiveData<List<TestProgressEntity>> observeProgressByUser(int userId);

    @Query("SELECT * FROM test_progress WHERE user_id = :userId AND test_id = :testId LIMIT 1")
    TestProgressEntity getProgressSync(int userId, String testId);

    @Query("SELECT * FROM test_attempts WHERE user_id = :userId ORDER BY passed_at DESC")
    LiveData<List<TestAttemptEntity>> observeAllAttemptsByUser(int userId);

    @Query("SELECT * FROM test_attempts WHERE user_id = :userId AND test_id = :testId ORDER BY passed_at DESC")
    LiveData<List<TestAttemptEntity>> observeAttemptsByTest(int userId, String testId);
}