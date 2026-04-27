package com.example.c.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.c.data.db.entity.AppStatisticsEntity;

@Dao
public interface AppStatisticsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(AppStatisticsEntity statistics);

    @Query("SELECT * FROM app_statistics WHERE user_id = :userId LIMIT 1")
    LiveData<AppStatisticsEntity> observeStatistics(int userId);

    @Query("SELECT * FROM app_statistics WHERE user_id = :userId LIMIT 1")
    AppStatisticsEntity getStatisticsSync(int userId);
}