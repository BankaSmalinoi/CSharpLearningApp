package com.example.c.ui.practice;

import android.content.Context;
import android.content.res.AssetManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PracticeDataLoader {

    private static final String[] PRACTICE_PATHS = {
            "data/practice_tasks.json",
            "data/practical_tasks.json"
    };

    public static List<PracticeTopicData> loadGrouped(Context context) {
        List<PracticeTaskData> tasks = loadTasks(context);
        Map<String, PracticeTopicData> topics = loadTopicsMap(context);

        for (PracticeTaskData task : tasks) {
            PracticeTopicData topic = topics.get(task.topicId);
            if (topic == null) {
                topic = new PracticeTopicData();
                topic.id = task.topicId;
                topic.title = isBlank(task.topicTitle) ? "Дополнительная практика" : task.topicTitle;
                topics.put(topic.id, topic);
            }
            task.topicTitle = topic.title;
            topic.tasks.add(task);
        }

        List<PracticeTopicData> result = new ArrayList<>();
        for (PracticeTopicData topic : topics.values()) {
            if (topic.tasks != null && !topic.tasks.isEmpty()) {
                result.add(topic);
            }
        }
        return result;
    }

    public static PracticeTaskData findTaskById(Context context, String taskId) {
        if (isBlank(taskId)) {
            return null;
        }
        for (PracticeTaskData task : loadTasks(context)) {
            if (taskId.equals(task.id)) {
                return task;
            }
        }
        return null;
    }

    public static List<PracticeTaskData> loadTasks(Context context) {
        for (String path : PRACTICE_PATHS) {
            try {
                String json = readAsset(context, path);
                return parseTasks(json);
            } catch (Exception ignored) {
            }
        }
        return new ArrayList<>();
    }

    private static List<PracticeTaskData> parseTasks(String json) throws Exception {
        List<PracticeTaskData> result = new ArrayList<>();
        Object root = new org.json.JSONTokener(json).nextValue();
        JSONArray array;

        if (root instanceof JSONArray) {
            array = (JSONArray) root;
        } else {
            JSONObject object = (JSONObject) root;
            if (object.has("tasks")) {
                array = object.getJSONArray("tasks");
            } else {
                array = object.getJSONArray("practices");
            }
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            PracticeTaskData task = new PracticeTaskData();
            task.id = firstString(object, "id", "taskId");
            task.topicId = firstString(object, "topicId", "topic_id", "topic");
            task.title = firstString(object, "title", "name");
            task.description = firstString(object, "description", "gameContext", "desc");
            task.taskText = firstString(object, "taskText", "task", "text");
            task.difficulty = firstString(object, "difficulty", "level");
            task.requiresTheoryRead = !object.has("requiresTheoryRead") || object.optBoolean("requiresTheoryRead", true);
            task.inputExample = firstString(object, "inputExample", "sampleInput", "input");
            task.outputExample = firstString(object, "outputExample", "sampleOutput", "expectedResult", "output");
            task.hint = firstString(object, "hint");
            task.solutionExample = firstString(object, "solutionExample", "starterCode", "solution");
            task.requirementsText = parseStringList(object.optJSONArray("requirements"));

            JSONArray hints = object.optJSONArray("hints");
            if (isBlank(task.hint) && hints != null) {
                task.hint = parseStringList(hints);
            }

            if (!isBlank(task.id) && !isBlank(task.topicId)) {
                result.add(task);
            }
        }

        return result;
    }

    private static Map<String, PracticeTopicData> loadTopicsMap(Context context) {
        Map<String, PracticeTopicData> result = new LinkedHashMap<>();
        try {
            String json = readAsset(context, "data/theory_topics.json");
            JSONObject root = new JSONObject(json);
            JSONArray array = root.getJSONArray("topics");
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                PracticeTopicData topic = new PracticeTopicData();
                topic.id = object.optString("id", "");
                topic.title = object.optString("title", "Тема");
                topic.description = object.optString("description", "");
                if (!isBlank(topic.id)) {
                    result.put(topic.id, topic);
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    private static String firstString(JSONObject object, String... keys) {
        for (String key : keys) {
            if (object.has(key) && !object.isNull(key)) {
                String value = object.optString(key, "");
                if (!isBlank(value)) {
                    return value;
                }
            }
        }
        return "";
    }

    private static String parseStringList(JSONArray array) {
        if (array == null || array.length() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length(); i++) {
            String value = array.optString(i, "");
            if (!isBlank(value)) {
                if (builder.length() > 0) {
                    builder.append('\n');
                }
                builder.append("• ").append(value);
            }
        }
        return builder.toString();
    }

    private static String readAsset(Context context, String path) throws Exception {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append('\n');
        }
        reader.close();
        return builder.toString();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
