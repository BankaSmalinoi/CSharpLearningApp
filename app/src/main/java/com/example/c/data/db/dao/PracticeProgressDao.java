package com.example.c.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.c.data.db.entity.PracticeAttemptEntity;
import com.example.c.data.db.entity.PracticeProgressEntity;

import java.util.List;

@Dao
public interface PracticeProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertProgress(PracticeProgressEntity progress);

    @Insert
    long insertAttempt(PracticeAttemptEntity attempt);

    @Query("SELECT * FROM practice_progress WHERE user_id = :userId")
    LiveData<List<PracticeProgressEntity>> observeProgressByUser(int userId);

    @Query("SELECT * FROM practice_progress WHERE user_id = :userId AND practice_id = :practiceId LIMIT 1")
    PracticeProgressEntity getProgressSync(int userId, String practiceId);

    @Query("SELECT * FROM practice_attempts WHERE user_id = :userId ORDER BY created_at DESC")
    LiveData<List<PracticeAttemptEntity>> observeAllAttemptsByUser(int userId);

    @Query("SELECT * FROM practice_attempts WHERE user_id = :userId AND practice_id = :practiceId ORDER BY created_at DESC")
    LiveData<List<PracticeAttemptEntity>> observeAttemptsByPractice(int userId, String practiceId);

    @Query("SELECT COUNT(*) FROM practice_progress WHERE user_id = :userId AND is_solved = 1")
    LiveData<Integer> observeSolvedPracticesCount(int userId);

    @Query("SELECT COUNT(*) FROM practice_attempts WHERE user_id = :userId")
    LiveData<Integer> observeTotalPracticeAttempts(int userId);
}