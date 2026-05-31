package com.example.c.data.statistics;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StatisticsStore {
    private static final String PREFS = "statistics_store_v1";
    private static final String KEY_THEORY_SET = "read_theory_ids";
    private static final String KEY_PRACTICE_SET = "solved_practice_ids";
    private static final String KEY_TEST_SET = "solved_test_ids";
    private static final String KEY_OPEN_DAYS = "open_days";
    private static final String KEY_DAILY = "daily_json";
    private static final String KEY_TASK_HISTORY = "task_history";
    private static final String KEY_TEST_HISTORY = "test_history";
    private static final String KEY_TOTAL_THEORY = "total_theory";
    private static final String KEY_TOTAL_PRACTICE = "total_practice";
    private static final String KEY_TOTAL_TESTS = "total_tests";

    private static volatile StatisticsStore instance;
    private final SharedPreferences prefs;

    private StatisticsStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static StatisticsStore getInstance(Context context) {
        if (instance == null) {
            synchronized (StatisticsStore.class) {
                if (instance == null) instance = new StatisticsStore(context);
            }
        }
        return instance;
    }

    public void setContentTotals(int theoryTotal, int practiceTotal, int testsTotal) {
        SharedPreferences.Editor e = prefs.edit();
        if (theoryTotal > 0) e.putInt(KEY_TOTAL_THEORY, theoryTotal);
        if (practiceTotal > 0) e.putInt(KEY_TOTAL_PRACTICE, practiceTotal);
        if (testsTotal > 0) e.putInt(KEY_TOTAL_TESTS, testsTotal);
        e.apply();
    }

    public void recordAppOpen() {
        String today = today();
        Set<String> days = new HashSet<>(prefs.getStringSet(KEY_OPEN_DAYS, new HashSet<>()));
        days.add(today);
        prefs.edit().putStringSet(KEY_OPEN_DAYS, days).apply();
        incrementDaily(today, "opens", 1);
    }

    public void recordTheoryRead(String topicId, int totalTopics) {
        if (topicId == null || topicId.trim().isEmpty()) topicId = "theory_" + System.currentTimeMillis();
        if (totalTopics > 0) prefs.edit().putInt(KEY_TOTAL_THEORY, totalTopics).apply();
        Set<String> ids = new HashSet<>(prefs.getStringSet(KEY_THEORY_SET, new HashSet<>()));
        boolean isNew = ids.add(topicId);
        prefs.edit().putStringSet(KEY_THEORY_SET, ids).apply();
        if (isNew) incrementDaily(today(), "theory", 1);
    }

    public void recordPracticeSolved(String taskId, int percent, int totalTasks) {
        if (taskId == null || taskId.trim().isEmpty()) taskId = "practice_" + System.currentTimeMillis();
        if (totalTasks > 0) prefs.edit().putInt(KEY_TOTAL_PRACTICE, totalTasks).apply();
        Set<String> ids = new HashSet<>(prefs.getStringSet(KEY_PRACTICE_SET, new HashSet<>()));
        boolean isNew = ids.add(taskId);
        prefs.edit().putStringSet(KEY_PRACTICE_SET, ids).apply();
        if (isNew) incrementDaily(today(), "practice", 1);
        appendHistory(KEY_TASK_HISTORY, taskId, percent);
    }

    public void recordTestSolved(String testId, int percent, int totalTests) {
        if (testId == null || testId.trim().isEmpty()) testId = "test_" + System.currentTimeMillis();
        if (totalTests > 0) prefs.edit().putInt(KEY_TOTAL_TESTS, totalTests).apply();
        Set<String> ids = new HashSet<>(prefs.getStringSet(KEY_TEST_SET, new HashSet<>()));
        boolean isNew = ids.add(testId);
        prefs.edit().putStringSet(KEY_TEST_SET, ids).apply();
        if (isNew) incrementDaily(today(), "tests", 1);
        appendHistory(KEY_TEST_HISTORY, testId, percent);
    }

    public void recordTheoryFromIntent(Activity activity) {
        recordTheoryRead(readBestId(activity.getIntent(), "topic", "theory", "id"), 0);
    }

    public void recordTestResultFromIntent(Activity activity) {
        Intent intent = activity.getIntent();
        int correct = readIntExtra(intent, "correct", "EXTRA_CORRECT", "correctAnswers", "score");
        int total = readIntExtra(intent, "total", "EXTRA_TOTAL", "questionsCount", "questionCount");
        int percent = total > 0 ? Math.round(correct * 100f / total) : readIntExtra(intent, "percent", "resultPercent");
        recordTestSolved(readBestId(intent, "test", "testId", "id"), percent, 0);
    }

    public Snapshot getSnapshot() {
        int theory = getSetSize(KEY_THEORY_SET);
        int practice = getSetSize(KEY_PRACTICE_SET);
        int tests = getSetSize(KEY_TEST_SET);
        int totalTheory = prefs.getInt(KEY_TOTAL_THEORY, 0);
        int totalPractice = prefs.getInt(KEY_TOTAL_PRACTICE, 0);
        int totalTests = prefs.getInt(KEY_TOTAL_TESTS, 0);
        return new Snapshot(
                theory,
                percent(theory, totalTheory),
                practice,
                percent(practice, totalPractice),
                tests,
                percent(tests, totalTests),
                getSetSize(KEY_OPEN_DAYS),
                getHistory(KEY_TASK_HISTORY),
                getHistory(KEY_TEST_HISTORY),
                getDailyMap()
        );
    }

    public DayInfo getDayInfo(String yyyyMmDd) {
        JSONObject root = readDailyRoot();
        JSONObject d = root.optJSONObject(yyyyMmDd);
        if (d == null) d = new JSONObject();
        return new DayInfo(
                yyyyMmDd,
                d.optInt("theory", 0),
                d.optInt("practice", 0),
                d.optInt("tests", 0),
                d.optInt("opens", 0)
        );
    }

    public Map<Integer, Integer> getMonthActivities(int year, int monthOneBased) {
        Map<Integer, Integer> result = new LinkedHashMap<>();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOneBased - 1);
        int max = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int day = 1; day <= max; day++) result.put(day, 0);

        String prefix = String.format(Locale.US, "%04d-%02d-", year, monthOneBased);
        JSONObject root = readDailyRoot();
        JSONArray names = root.names();
        if (names == null) return result;
        for (int i = 0; i < names.length(); i++) {
            String date = names.optString(i, "");
            if (!date.startsWith(prefix)) continue;
            int day = Integer.parseInt(date.substring(8, 10));
            DayInfo info = getDayInfo(date);
            result.put(day, info.total());
        }
        return result;
    }

    private void incrementDaily(String date, String key, int value) {
        try {
            JSONObject root = readDailyRoot();
            JSONObject day = root.optJSONObject(date);
            if (day == null) day = new JSONObject();
            day.put(key, day.optInt(key, 0) + value);
            root.put(date, day);
            prefs.edit().putString(KEY_DAILY, root.toString()).apply();
        } catch (JSONException ignored) { }
    }

    private JSONObject readDailyRoot() {
        try { return new JSONObject(prefs.getString(KEY_DAILY, "{}")); }
        catch (JSONException e) { return new JSONObject(); }
    }

    private Map<String, DayInfo> getDailyMap() {
        Map<String, DayInfo> map = new LinkedHashMap<>();
        JSONObject root = readDailyRoot();
        JSONArray names = root.names();
        if (names == null) return map;
        for (int i = 0; i < names.length(); i++) {
            String date = names.optString(i);
            map.put(date, getDayInfo(date));
        }
        return map;
    }

    private void appendHistory(String key, String id, int percent) {
        try {
            JSONArray arr = new JSONArray(prefs.getString(key, "[]"));
            JSONObject item = new JSONObject();
            item.put("date", today());
            item.put("id", id);
            item.put("percent", Math.max(0, Math.min(100, percent)));
            arr.put(item);
            while (arr.length() > 200) arr.remove(0);
            prefs.edit().putString(key, arr.toString()).apply();
        } catch (JSONException ignored) { }
    }

    private List<HistoryItem> getHistory(String key) {
        List<HistoryItem> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs.getString(key, "[]"));
            for (int i = arr.length() - 1; i >= 0; i--) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                list.add(new HistoryItem(o.optString("date"), o.optString("id"), o.optInt("percent")));
            }
        } catch (JSONException ignored) { }
        return list;
    }

    private int getSetSize(String key) {
        return prefs.getStringSet(key, new HashSet<>()).size();
    }

    private int percent(int current, int total) {
        if (total <= 0) return 0;
        return Math.min(100, Math.round(current * 100f / total));
    }

    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private static int readIntExtra(Intent intent, String... keys) {
        Bundle b = intent.getExtras();
        if (b == null) return 0;
        for (String key : keys) {
            if (!b.containsKey(key)) continue;
            Object v = b.get(key);
            if (v instanceof Integer) return (Integer) v;
            if (v instanceof Long) return ((Long) v).intValue();
            if (v instanceof Float) return Math.round((Float) v);
            if (v instanceof Double) return (int) Math.round((Double) v);
            try { return Integer.parseInt(String.valueOf(v)); } catch (Exception ignored) { }
        }
        return 0;
    }

    private static String readBestId(Intent intent, String... preferredParts) {
        Bundle b = intent.getExtras();
        if (b == null) return null;
        for (String part : preferredParts) {
            for (String key : b.keySet()) {
                if (key.toLowerCase(Locale.US).contains(part.toLowerCase(Locale.US))) {
                    Object v = b.get(key);
                    if (v != null) return String.valueOf(v);
                }
            }
        }
        return null;
    }

    public static class Snapshot {
        public final int readTheoryCount, readTheoryPercent, solvedPracticeCount, solvedPracticePercent;
        public final int solvedTestsCount, solvedTestsPercent, openDaysCount;
        public final List<HistoryItem> taskHistory, testHistory;
        public final Map<String, DayInfo> daily;

        Snapshot(int readTheoryCount, int readTheoryPercent, int solvedPracticeCount, int solvedPracticePercent,
                 int solvedTestsCount, int solvedTestsPercent, int openDaysCount,
                 List<HistoryItem> taskHistory, List<HistoryItem> testHistory, Map<String, DayInfo> daily) {
            this.readTheoryCount = readTheoryCount;
            this.readTheoryPercent = readTheoryPercent;
            this.solvedPracticeCount = solvedPracticeCount;
            this.solvedPracticePercent = solvedPracticePercent;
            this.solvedTestsCount = solvedTestsCount;
            this.solvedTestsPercent = solvedTestsPercent;
            this.openDaysCount = openDaysCount;
            this.taskHistory = taskHistory;
            this.testHistory = testHistory;
            this.daily = daily;
        }
    }

    public static class HistoryItem {
        public final String date, id;
        public final int percent;
        HistoryItem(String date, String id, int percent) {
            this.date = date;
            this.id = id;
            this.percent = percent;
        }
    }

    public static class DayInfo {
        public final String date;
        public final int theory, practice, tests, opens;
        DayInfo(String date, int theory, int practice, int tests, int opens) {
            this.date = date;
            this.theory = theory;
            this.practice = practice;
            this.tests = tests;
            this.opens = opens;
        }
        public int total() { return theory + practice + tests; }
    }
}
