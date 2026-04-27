package com.example.c.data.repository;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.c.data.db.AppDatabase;
import com.example.c.data.db.dao.TopicProgressDao;
import com.example.c.data.db.entity.TopicProgressEntity;
import com.example.c.data.model.theory.TheoryBlock;
import com.example.c.data.model.theory.TheoryTopic;
import com.example.c.data.model.theory.TheoryTopicsResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheoryRepository {
    private static final String TAG = "TheoryRepository";
    private static final String THEORY_ASSET_PATH = "data/theory_topics.json";
    private static final int READ_THRESHOLD_PERCENT = 95;

    private final Context context;
    private final TopicProgressDao topicProgressDao;
    private List<TheoryTopic> cachedTopics;

    public TheoryRepository(Context context) {
        this.context = context.getApplicationContext();
        this.topicProgressDao = AppDatabase.getInstance(this.context).topicProgressDao();
    }

    public List<TheoryTopic> getTopics() {
        ensureTopicsLoaded();
        return new ArrayList<>(cachedTopics);
    }

    public List<TheoryTopic> getAllTopics() {
        return getTopics();
    }

    public TheoryTopic getTopicById(String topicId) {
        if (topicId == null || topicId.trim().isEmpty()) {
            return null;
        }
        for (TheoryTopic topic : getTopics()) {
            if (topicId.equals(topic.id)) {
                return topic;
            }
        }
        return null;
    }

    public LiveData<List<TopicProgressEntity>> observeAllProgress() {
        return topicProgressDao.observeAllProgress();
    }

    public LiveData<TopicProgressEntity> observeProgress(String topicId) {
        return topicProgressDao.observeProgress(topicId);
    }

    public TopicProgressEntity getProgress(String topicId) {
        if (topicId == null || topicId.trim().isEmpty()) {
            return null;
        }
        return topicProgressDao.getProgress(topicId);
    }

    public Map<String, TopicProgressEntity> getProgressMap() {
        Map<String, TopicProgressEntity> result = new HashMap<>();
        List<TopicProgressEntity> rows = topicProgressDao.getAllProgressSync();
        if (rows == null) {
            return result;
        }
        for (TopicProgressEntity row : rows) {
            if (row != null && row.topicId != null) {
                result.put(row.topicId, row);
            }
        }
        return result;
    }

    public void saveProgress(String topicId, int readPercent, int scrollY) {
        if (topicId == null || topicId.trim().isEmpty()) {
            return;
        }

        int normalizedPercent = Math.max(0, Math.min(100, readPercent));
        boolean isRead = normalizedPercent >= READ_THRESHOLD_PERCENT;
        if (isRead) {
            normalizedPercent = 100;
        }

        long now = System.currentTimeMillis();
        TopicProgressEntity progress = new TopicProgressEntity(topicId, normalizedPercent, scrollY, isRead, now);
        topicProgressDao.saveProgress(progress);
    }

    public void markAsRead(String topicId) {
        saveProgress(topicId, 100, 0);
    }

    public boolean isTheoryRead(String topicId) {
        if (topicId == null || topicId.trim().isEmpty()) {
            return false;
        }
        return topicProgressDao.isTheoryReadInt(topicId) > 0;
    }

    private void ensureTopicsLoaded() {
        if (cachedTopics == null) {
            cachedTopics = loadTopicsFromAssets();
        }
    }

    private List<TheoryTopic> loadTopicsFromAssets() {
        List<TheoryTopic> result = new ArrayList<>();
        try {
            String json = readAssetFile(THEORY_ASSET_PATH);
            Gson gson = new Gson();

            JsonElement root = new JsonParser().parse(json);
            JsonArray topicsArray = null;

            if (root != null && root.isJsonObject()) {
                JsonObject rootObject = root.getAsJsonObject();
                if (rootObject.has("topics") && rootObject.get("topics").isJsonArray()) {
                    topicsArray = rootObject.getAsJsonArray("topics");
                } else if (rootObject.has("data") && rootObject.get("data").isJsonArray()) {
                    topicsArray = rootObject.getAsJsonArray("data");
                } else {
                    TheoryTopicsResponse response = gson.fromJson(rootObject, TheoryTopicsResponse.class);
                    if (response != null && response.topics != null) {
                        result.addAll(response.topics);
                    }
                }
            } else if (root != null && root.isJsonArray()) {
                topicsArray = root.getAsJsonArray();
            }

            if (topicsArray != null) {
                for (JsonElement element : topicsArray) {
                    try {
                        TheoryTopic topic = gson.fromJson(element, TheoryTopic.class);
                        if (topic != null && topic.id != null && !topic.id.trim().isEmpty()) {
                            result.add(topic);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            Log.d(TAG, "Загружено тем теории: " + result.size());
        } catch (Exception e) {
            Log.e(TAG, "Не удалось загрузить теорию из assets/" + THEORY_ASSET_PATH, e);
        }
        return result;
    }

    private String readAssetFile(String fileName) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append('\n');
        }
        reader.close();
        return builder.toString();
    }
}
