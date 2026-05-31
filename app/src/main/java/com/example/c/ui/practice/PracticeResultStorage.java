package com.example.c.ui.practice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashSet;
import java.util.Set;

public class PracticeResultStorage extends SQLiteOpenHelper {

    private static final String DB_NAME = "practice_checker_results.db";
    private static final int DB_VERSION = 1;

    public PracticeResultStorage(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS practice_solution_attempts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "task_id TEXT NOT NULL, " +
                "code TEXT NOT NULL, " +
                "success INTEGER NOT NULL, " +
                "status TEXT, " +
                "message TEXT, " +
                "expected_output TEXT, " +
                "actual_output TEXT, " +
                "error TEXT, " +
                "created_at INTEGER NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS practice_solution_progress (" +
                "task_id TEXT PRIMARY KEY, " +
                "solved INTEGER NOT NULL, " +
                "attempts_count INTEGER NOT NULL, " +
                "last_status TEXT, " +
                "last_message TEXT, " +
                "updated_at INTEGER NOT NULL" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public void saveAttempt(String taskId,
                            String code,
                            boolean success,
                            String status,
                            String message,
                            String expectedOutput,
                            String actualOutput,
                            String error) {
        long now = System.currentTimeMillis();
        SQLiteDatabase db = getWritableDatabase();

        ContentValues attempt = new ContentValues();
        attempt.put("task_id", taskId);
        attempt.put("code", code);
        attempt.put("success", success ? 1 : 0);
        attempt.put("status", status);
        attempt.put("message", message);
        attempt.put("expected_output", expectedOutput);
        attempt.put("actual_output", actualOutput);
        attempt.put("error", error);
        attempt.put("created_at", now);
        db.insert("practice_solution_attempts", null, attempt);

        int attempts = getAttemptsCountInternal(db, taskId) + 1;
        boolean solved = success || isTaskSolvedInternal(db, taskId);

        ContentValues progress = new ContentValues();
        progress.put("task_id", taskId);
        progress.put("solved", solved ? 1 : 0);
        progress.put("attempts_count", attempts);
        progress.put("last_status", status);
        progress.put("last_message", message);
        progress.put("updated_at", now);
        db.insertWithOnConflict("practice_solution_progress", null, progress, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean isTaskSolved(String taskId) {
        SQLiteDatabase db = getReadableDatabase();
        return isTaskSolvedInternal(db, taskId);
    }

    public Set<String> getSolvedTaskIds() {
        Set<String> result = new HashSet<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT task_id FROM practice_solution_progress WHERE solved = 1", null);
        try {
            while (cursor.moveToNext()) {
                result.add(cursor.getString(0));
            }
        } finally {
            cursor.close();
        }
        return result;
    }


    public String getLastAcceptedCode(String taskId) {
        if (taskId == null || taskId.trim().isEmpty()) {
            return "";
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT code FROM practice_solution_attempts " +
                        "WHERE task_id = ? AND success = 1 " +
                        "ORDER BY created_at DESC, id DESC LIMIT 1",
                new String[]{taskId}
        );

        try {
            if (cursor.moveToFirst()) {
                String code = cursor.getString(0);
                return code == null ? "" : code;
            }
            return "";
        } finally {
            cursor.close();
        }
    }

    private boolean isTaskSolvedInternal(SQLiteDatabase db, String taskId) {
        Cursor cursor = db.rawQuery(
                "SELECT solved FROM practice_solution_progress WHERE task_id = ? LIMIT 1",
                new String[]{taskId}
        );
        try {
            return cursor.moveToFirst() && cursor.getInt(0) == 1;
        } finally {
            cursor.close();
        }
    }

    private int getAttemptsCountInternal(SQLiteDatabase db, String taskId) {
        Cursor cursor = db.rawQuery(
                "SELECT attempts_count FROM practice_solution_progress WHERE task_id = ? LIMIT 1",
                new String[]{taskId}
        );
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }
}
