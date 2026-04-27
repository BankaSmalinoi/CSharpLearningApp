package com.example.c.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.c.data.model.practice.PracticeTask;
import com.example.c.data.repository.PracticeRepository;

import java.util.List;
import java.util.Map;

public class PracticeViewModel extends AndroidViewModel {
    private final PracticeRepository repository;

    public PracticeViewModel(@NonNull Application application) {
        super(application);
        repository = new PracticeRepository(application);
    }

    public List<PracticeTask> getPracticeTasks() {
        return repository.getPracticeTasks();
    }

    public List<PracticeTask> getAllTasks() {
        return repository.getAllTasks();
    }

    public List<PracticeTask> getTasksByTopic(String topicId) {
        return repository.getTasksByTopic(topicId);
    }

    public PracticeTask getTaskById(String taskId) {
        return repository.getTaskById(taskId);
    }

    public Map<String, List<PracticeTask>> getTasksGroupedByTopic() {
        return repository.getTasksGroupedByTopic();
    }
}
