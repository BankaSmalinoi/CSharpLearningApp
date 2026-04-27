package com.example.c.data.repository;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.example.c.data.model.practice.PracticeTask;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PracticeRepository {
    private static final String TAG = "PracticeRepository";
    private static final String[] ASSET_PATHS = {
            "data/practical_tasks.json",
            "data/practice_tasks.json"
    };

    private final Context context;
    private final Gson gson = new Gson();
    private List<PracticeTask> cachedTasks;

    public PracticeRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    public List<PracticeTask> getPracticeTasks() {
        ensureLoaded();
        return new ArrayList<>(cachedTasks);
    }

    public List<PracticeTask> getAllTasks() {
        return getPracticeTasks();
    }

    public PracticeTask getTaskById(String taskId) {
        if (taskId == null || taskId.trim().isEmpty()) {
            return null;
        }
        for (PracticeTask task : getPracticeTasks()) {
            if (taskId.equals(task.id)) {
                return task;
            }
        }
        return null;
    }

    public List<PracticeTask> getTasksByTopic(String topicId) {
        List<PracticeTask> result = new ArrayList<>();
        if (topicId == null || topicId.trim().isEmpty()) {
            return result;
        }
        for (PracticeTask task : getPracticeTasks()) {
            if (topicId.equals(task.topicId)) {
                result.add(task);
            }
        }
        return result;
    }

    public Map<String, List<PracticeTask>> getTasksGroupedByTopic() {
        Map<String, List<PracticeTask>> result = new LinkedHashMap<>();
        for (PracticeTask task : getPracticeTasks()) {
            if (task == null || task.topicId == null) {
                continue;
            }
            List<PracticeTask> list = result.get(task.topicId);
            if (list == null) {
                list = new ArrayList<>();
                result.put(task.topicId, list);
            }
            list.add(task);
        }
        return result;
    }

    private void ensureLoaded() {
        if (cachedTasks == null) {
            cachedTasks = loadFromAssets();
        }
    }

    private List<PracticeTask> loadFromAssets() {
        for (String path : ASSET_PATHS) {
            try {
                String json = readAssetFile(path);
                List<PracticeTask> tasks = parseTasks(json);
                Log.d(TAG, "Загружено практических заданий из assets/" + path + ": " + tasks.size());
                return tasks;
            } catch (IOException e) {
                Log.w(TAG, "Файл assets/" + path + " не найден");
            } catch (Exception e) {
                Log.e(TAG, "Ошибка чтения практических заданий из assets/" + path, e);
            }
        }
        return new ArrayList<>();
    }

    private List<PracticeTask> parseTasks(String json) {
        List<PracticeTask> result = new ArrayList<>();
        JsonElement root = new JsonParser().parse(json);
        JsonArray array = null;

        if (root != null && root.isJsonArray()) {
            array = root.getAsJsonArray();
        } else if (root != null && root.isJsonObject()) {
            JsonObject object = root.getAsJsonObject();
            if (object.has("practices") && object.get("practices").isJsonArray()) {
                array = object.getAsJsonArray("practices");
            } else if (object.has("tasks") && object.get("tasks").isJsonArray()) {
                array = object.getAsJsonArray("tasks");
            } else if (object.has("data") && object.get("data").isJsonArray()) {
                array = object.getAsJsonArray("data");
            }
        }

        if (array == null) {
            return result;
        }

        int index = 0;
        for (JsonElement element : array) {
            if (element == null || !element.isJsonObject()) {
                continue;
            }

            PracticeTask task = gson.fromJson(element, PracticeTask.class);
            normalizeTask(task, index);
            if (task != null && task.id != null && !task.id.trim().isEmpty()) {
                result.add(task);
            }
            index++;
        }
        return result;
    }

    private void normalizeTask(PracticeTask task, int index) {
        if (task == null) {
            return;
        }
        if (task.id == null || task.id.trim().isEmpty()) {
            task.id = "practice_task_" + (index + 1);
        }
        if (task.title == null || task.title.trim().isEmpty()) {
            task.title = "Практическое задание " + (index + 1);
        }
        if (task.requiresTheoryRead == false) {
            // Для учебника по умолчанию практика должна быть закрыта до прочтения теории.
            // Если в JSON поле явно отсутствовало, Gson тоже даёт false, поэтому принудительно включаем.
            task.requiresTheoryRead = true;
        }
    }

    private String readAssetFile(String fileName) throws IOException {
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            return builder.toString();
        }
    }
}
