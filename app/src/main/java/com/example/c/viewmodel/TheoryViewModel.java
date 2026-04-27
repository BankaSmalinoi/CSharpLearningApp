package com.example.c.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.c.data.db.entity.TopicProgressEntity;
import com.example.c.data.model.theory.TheoryTopic;
import com.example.c.data.repository.TheoryRepository;

import java.util.List;
import java.util.Map;

public class TheoryViewModel extends AndroidViewModel {
    private final TheoryRepository repository;

    public TheoryViewModel(@NonNull Application application) {
        super(application);
        repository = new TheoryRepository(application);
    }

    public List<TheoryTopic> getTopics() {
        return repository.getTopics();
    }

    public List<TheoryTopic> getAllTopics() {
        return repository.getAllTopics();
    }

    public TheoryTopic getTopicById(String topicId) {
        return repository.getTopicById(topicId);
    }

    public LiveData<List<TopicProgressEntity>> observeAllProgress() {
        return repository.observeAllProgress();
    }

    public LiveData<TopicProgressEntity> observeProgress(String topicId) {
        return repository.observeProgress(topicId);
    }

    public TopicProgressEntity getProgress(String topicId) {
        return repository.getProgress(topicId);
    }

    public Map<String, TopicProgressEntity> getProgressMap() {
        return repository.getProgressMap();
    }

    public void saveProgress(String topicId, int readPercent, int scrollY) {
        repository.saveProgress(topicId, readPercent, scrollY);
    }

    public boolean isTheoryRead(String topicId) {
        return repository.isTheoryRead(topicId);
    }
}
