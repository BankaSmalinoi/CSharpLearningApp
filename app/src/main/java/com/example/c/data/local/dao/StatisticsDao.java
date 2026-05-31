package com.example.c.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.c.data.local.entity.DailyActivityEntity;
import com.example.c.data.local.entity.TaskSolutionHistoryEntity;
import com.example.c.data.local.entity.TestSolutionHistoryEntity;

import java.util.List;

@Dao
public interface StatisticsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDailyActivity(DailyActivityEntity entity);

    @Query("SELECT * FROM daily_activity WHERE date = :date LIMIT 1")
    DailyActivityEntity getDailyActivity(String date);

    @Query("UPDATE daily_activity SET appOpenCount = appOpenCount + 1, lastActivityAt = :time WHERE date = :date")
    void increaseAppOpen(String date, long time);

    @Query("UPDATE daily_activity SET theoryReadCount = theoryReadCount + 1, lastActivityAt = :time WHERE date = :date")
    void increaseTheoryRead(String date, long time);

    @Query("UPDATE daily_activity SET tasksSolvedCount = tasksSolvedCount + 1, lastActivityAt = :time WHERE date = :date")
    void increaseTaskSolved(String date, long time);

    @Query("UPDATE daily_activity SET testsSolvedCount = testsSolvedCount + 1, lastActivityAt = :time WHERE date = :date")
    void increaseTestSolved(String date, long time);

    @Query("SELECT * FROM daily_activity WHERE date LIKE :monthPrefix ORDER BY date ASC")
    List<DailyActivityEntity> getActivitiesForMonth(String monthPrefix);

    @Query("SELECT COUNT(*) FROM daily_activity WHERE appOpenCount > 0")
    int getActiveDaysCount();

    @Query("SELECT SUM(theoryReadCount) FROM daily_activity")
    Integer getTotalTheoryReadCount();

    @Query("SELECT SUM(tasksSolvedCount) FROM daily_activity")
    Integer getTotalTasksSolvedCount();

    @Query("SELECT SUM(testsSolvedCount) FROM daily_activity")
    Integer getTotalTestsSolvedCount();

    @Insert
    void insertTaskHistory(TaskSolutionHistoryEntity entity);

    @Insert
    void insertTestHistory(TestSolutionHistoryEntity entity);

    @Query("SELECT * FROM task_solution_history ORDER BY solvedAt DESC")
    List<TaskSolutionHistoryEntity> getTaskHistory();

    @Query("SELECT * FROM test_solution_history ORDER BY solvedAt DESC")
    List<TestSolutionHistoryEntity> getTestHistory();

    @Query("SELECT COUNT(*) FROM task_solution_history WHERE accepted = 1")
    int getAcceptedTasksCount();

    @Query("SELECT COUNT(DISTINCT taskId) FROM task_solution_history WHERE accepted = 1")
    int getUniqueAcceptedTasksCount();

    @Query("SELECT COUNT(*) FROM task_solution_history WHERE taskId = :taskId AND accepted = 1")
    int getAcceptedTaskHistoryCount(String taskId);

    @Query("SELECT COUNT(*) FROM task_solution_history")
    int getTaskAttemptsCount();

    @Query("SELECT COUNT(DISTINCT testId) FROM test_solution_history")
    int getUniqueTestsSolvedCount();

    @Query("SELECT AVG(percent) FROM test_solution_history")
    Double getAverageTestPercent();

    @Query("SELECT MAX(lastActivityAt) FROM daily_activity WHERE appOpenCount > 0")
    Long getLastAppOpenTime();
}
