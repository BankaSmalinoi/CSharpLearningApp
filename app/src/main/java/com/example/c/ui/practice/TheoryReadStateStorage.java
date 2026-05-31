package com.example.c.ui.practice;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Проверяет, прочитана ли теория по topicId.
 *
 * Важно: практика использует этот класс для блокировки/разблокировки заданий.
 * Раньше логика могла вернуть false слишком рано:
 * если в одной из БД была таблица topic_progress, но нужной строки там не было,
 * проверка сразу завершалась и не доходила до настоящей Room-БД csharp_learning_db.
 *
 * Теперь проверка просматривает все подходящие БД и таблицы и возвращает true,
 * если хотя бы в одном месте найдено подтверждение прочтения темы.
 */
public class TheoryReadStateStorage {

    private static final int READ_THRESHOLD_PERCENT = 95;

    public static boolean isTheoryRead(Context context, String topicId) {
        if (topicId == null || topicId.trim().isEmpty()) {
            return true;
        }

        String normalizedTopicId = topicId.trim();

        // 1. Сначала проверяем Room/SQLite, потому что прогресс теории хранится там.
        if (isTheoryReadInAnyDatabase(context, normalizedTopicId)) {
            return true;
        }

        // 2. Дополнительный fallback: если где-то старый код сохранял состояние в SharedPreferences.
        return isTheoryReadInSharedPreferences(context, normalizedTopicId);
    }

    private static boolean isTheoryReadInAnyDatabase(Context context, String topicId) {
        String[] databaseNames = context.databaseList();
        if (databaseNames == null || databaseNames.length == 0) {
            return false;
        }

        // Room-БД проекта должна проверяться первой.
        String[] preferredDbNames = {
                "csharp_learning_db",
                "csharp_learning_db.db",
                "app_database",
                "app_database.db"
        };

        for (String preferred : preferredDbNames) {
            if (containsDatabase(databaseNames, preferred) && isTheoryReadInDatabase(context, preferred, topicId)) {
                return true;
            }
        }

        // Затем проверяем остальные БД. Важно: не возвращаем false при первой неудаче.
        for (String dbName : databaseNames) {
            if (dbName == null || dbName.trim().isEmpty()) {
                continue;
            }

            if (isInternalOrResultDatabase(dbName)) {
                // БД результатов практики не содержит прогресс теории.
                continue;
            }

            if (isTheoryReadInDatabase(context, dbName, topicId)) {
                return true;
            }
        }

        return false;
    }

    private static boolean containsDatabase(String[] databaseNames, String targetName) {
        for (String name : databaseNames) {
            if (targetName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInternalOrResultDatabase(String dbName) {
        String normalized = dbName == null ? "" : dbName.toLowerCase();
        return normalized.contains("practice_checker_results")
                || normalized.contains("webview")
                || normalized.contains("google")
                || normalized.contains("firebase")
                || normalized.contains("workdb")
                || normalized.contains("androidx");
    }

    private static boolean isTheoryReadInDatabase(Context context, String dbName, String topicId) {
        SQLiteDatabase db = null;
        try {
            File dbFile = context.getDatabasePath(dbName);
            if (dbFile == null || !dbFile.exists()) {
                return false;
            }

            db = SQLiteDatabase.openDatabase(
                    dbFile.getAbsolutePath(),
                    null,
                    SQLiteDatabase.OPEN_READONLY
            );

            String[] tableNames = {
                    "topic_progress",
                    "theory_progress",
                    "TopicProgressEntity",
                    "TheoryProgressEntity"
            };

            for (String tableName : tableNames) {
                if (isTheoryReadFromTable(db, tableName, topicId)) {
                    return true;
                }
            }

            return false;
        } catch (Exception ignored) {
            return false;
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static boolean isTheoryReadFromTable(SQLiteDatabase db, String tableName, String topicId) {
        if (!tableExists(db, tableName)) {
            return false;
        }

        List<String> columns = getColumns(db, tableName);
        String topicColumn = findColumn(columns,
                "topicId",
                "topic_id",
                "themeId",
                "theme_id",
                "lessonId",
                "lesson_id",
                "id"
        );

        if (topicColumn == null) {
            return false;
        }

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT * FROM " + tableName + " WHERE " + topicColumn + " = ?",
                    new String[]{topicId}
            );

            while (cursor.moveToNext()) {
                if (isReadRow(cursor, columns)) {
                    return true;
                }
            }

            return false;
        } catch (Exception ignored) {
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static boolean isReadRow(Cursor cursor, List<String> columns) {
        String[] booleanColumns = {
                "isRead",
                "is_read",
                "read",
                "isCompleted",
                "is_completed",
                "completed",
                "done",
                "isDone",
                "is_done"
        };

        for (String columnName : booleanColumns) {
            Integer value = getIntColumn(cursor, columns, columnName);
            if (value != null && value == 1) {
                return true;
            }
        }

        String[] percentColumns = {
                "readPercent",
                "read_percent",
                "progressPercent",
                "progress_percent",
                "percent",
                "progress"
        };

        for (String columnName : percentColumns) {
            Integer value = getIntColumn(cursor, columns, columnName);
            if (value != null && value >= READ_THRESHOLD_PERCENT) {
                return true;
            }
        }

        String[] statusColumns = {
                "status",
                "state",
                "readStatus",
                "read_status"
        };

        for (String columnName : statusColumns) {
            String value = getStringColumn(cursor, columns, columnName);
            if (value != null) {
                String normalized = value.trim().toLowerCase();
                if (normalized.equals("read")
                        || normalized.equals("completed")
                        || normalized.equals("done")
                        || normalized.equals("прочитано")
                        || normalized.equals("завершено")) {
                    return true;
                }
            }
        }

        return false;
    }

    private static Integer getIntColumn(Cursor cursor, List<String> columns, String columnName) {
        String realColumn = findColumn(columns, columnName);
        if (realColumn == null) {
            return null;
        }

        int index = cursor.getColumnIndex(realColumn);
        if (index < 0 || cursor.isNull(index)) {
            return null;
        }

        try {
            return cursor.getInt(index);
        } catch (Exception ignored) {
            try {
                return Integer.parseInt(cursor.getString(index));
            } catch (Exception ignoredAgain) {
                return null;
            }
        }
    }

    private static String getStringColumn(Cursor cursor, List<String> columns, String columnName) {
        String realColumn = findColumn(columns, columnName);
        if (realColumn == null) {
            return null;
        }

        int index = cursor.getColumnIndex(realColumn);
        if (index < 0 || cursor.isNull(index)) {
            return null;
        }

        try {
            return cursor.getString(index);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean isTheoryReadInSharedPreferences(Context context, String topicId) {
        String[] prefNames = {
                "theory_progress",
                "topic_progress",
                "progress",
                "app_progress"
        };

        String[] keyTemplates = {
                "%s",
                "read_%s",
                "is_read_%s",
                "theory_read_%s",
                "topic_read_%s",
                "progress_%s",
                "read_percent_%s"
        };

        for (String prefName : prefNames) {
            try {
                SharedPreferences preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);

                for (String template : keyTemplates) {
                    String key = String.format(template, topicId);

                    if (preferences.contains(key)) {
                        if (preferences.getBoolean(key, false)) {
                            return true;
                        }

                        int percent = preferences.getInt(key, 0);
                        if (percent >= READ_THRESHOLD_PERCENT) {
                            return true;
                        }

                        String value = preferences.getString(key, "");
                        if (value != null) {
                            String normalized = value.trim().toLowerCase();
                            if (normalized.equals("true")
                                    || normalized.equals("read")
                                    || normalized.equals("completed")
                                    || normalized.equals("done")
                                    || normalized.equals("прочитано")) {
                                return true;
                            }

                            try {
                                if (Integer.parseInt(normalized) >= READ_THRESHOLD_PERCENT) {
                                    return true;
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    private static boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{tableName}
            );
            return cursor.moveToFirst();
        } catch (Exception ignored) {
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static List<String> getColumns(SQLiteDatabase db, String tableName) {
        List<String> result = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            while (cursor.moveToNext()) {
                int index = cursor.getColumnIndex("name");
                if (index >= 0) {
                    result.add(cursor.getString(index));
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    private static String findColumn(List<String> columns, String... candidates) {
        if (columns == null || candidates == null) {
            return null;
        }

        for (String candidate : candidates) {
            for (String column : columns) {
                if (column.equals(candidate)) {
                    return column;
                }
            }
        }

        for (String candidate : candidates) {
            String normalizedCandidate = normalize(candidate);
            for (String column : columns) {
                if (normalize(column).equals(normalizedCandidate)) {
                    return column;
                }
            }
        }

        return null;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.replace("_", "").toLowerCase();
    }
}
