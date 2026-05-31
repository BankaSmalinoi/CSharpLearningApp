package com.example.c.data.repository;

import android.content.Context;

import com.example.c.data.db.AppDatabase;
import com.example.c.data.local.dao.StatisticsDao;
import com.example.c.data.local.entity.DailyActivityEntity;
import com.example.c.data.local.entity.TaskSolutionHistoryEntity;
import com.example.c.data.local.entity.TestSolutionHistoryEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticsRepository {

    private final StatisticsDao dao;

    public StatisticsRepository(Context context) {
        dao = AppDatabase.getInstance(context.getApplicationContext()).statisticsDao();
    }

    private String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private void ensureDayExists(String date) {
        DailyActivityEntity existing = dao.getDailyActivity(date);
        if (existing == null) {
            dao.insertDailyActivity(new DailyActivityEntity(date));
        }
    }

    public void registerAppOpen() {
        String date = today();
        ensureDayExists(date);
        dao.increaseAppOpen(date, System.currentTimeMillis());
    }

    public void registerTheoryRead() {
        String date = today();
        ensureDayExists(date);
        dao.increaseTheoryRead(date, System.currentTimeMillis());
    }

    public void registerSolvedTask(String taskId, String taskTitle, boolean accepted, int percent, String userCode) {
        String date = today();
        ensureDayExists(date);

        boolean firstAcceptedForTask = accepted && dao.getAcceptedTaskHistoryCount(taskId) == 0;
        if (firstAcceptedForTask) {
            dao.increaseTaskSolved(date, System.currentTimeMillis());
        }

        dao.insertTaskHistory(new TaskSolutionHistoryEntity(
                taskId,
                taskTitle,
                accepted,
                Math.max(0, Math.min(100, percent)),
                userCode,
                System.currentTimeMillis()
        ));
    }

    public void registerSolvedTest(String testId, String testTitle, int correct, int total) {
        String date = today();
        ensureDayExists(date);

        int percent = total == 0 ? 0 : Math.round((correct * 100f) / total);
        dao.increaseTestSolved(date, System.currentTimeMillis());

        dao.insertTestHistory(new TestSolutionHistoryEntity(
                testId,
                testTitle,
                correct,
                total,
                percent,
                System.currentTimeMillis()
        ));
    }

    public int getTotalTheoryReadCount() {
        Integer value = dao.getTotalTheoryReadCount();
        return value == null ? 0 : value;
    }

    public int getTotalTasksSolvedCount() {
        return dao.getUniqueAcceptedTasksCount();
    }

    public int getTotalTestsSolvedCount() {
        return dao.getUniqueTestsSolvedCount();
    }

    public int getActiveDaysCount() {
        return dao.getActiveDaysCount();
    }

    public int getAcceptedTasksCount() {
        return dao.getAcceptedTasksCount();
    }

    public int getTaskAttemptsCount() {
        return dao.getTaskAttemptsCount();
    }

    public int getTaskSuccessPercent() {
        int attempts = dao.getTaskAttemptsCount();
        if (attempts == 0) return 0;
        int accepted = dao.getAcceptedTasksCount();
        return Math.round((accepted * 100f) / attempts);
    }

    public int getAverageTestPercent() {
        Double value = dao.getAverageTestPercent();
        return value == null ? 0 : value.intValue();
    }

    public List<TaskSolutionHistoryEntity> getTaskHistory() {
        return dao.getTaskHistory();
    }

    public List<TestSolutionHistoryEntity> getTestHistory() {
        return dao.getTestHistory();
    }

    public DailyActivityEntity getDayActivity(String date) {
        return dao.getDailyActivity(date);
    }

    public List<DailyActivityEntity> getActivitiesForMonth(String monthPrefix) {
        return dao.getActivitiesForMonth(monthPrefix);
    }

    public Long getLastAppOpenTime() {
        return dao.getLastAppOpenTime();
    }
}
