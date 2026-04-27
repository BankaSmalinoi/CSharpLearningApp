package com.example.c.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.c.data.db.entity.TestProgressEntity;
import com.example.c.data.model.test.TestModel;
import com.example.c.data.repository.TestRepository;

import java.util.List;

public class TestViewModel extends AndroidViewModel {
    private final TestRepository repository;

    public TestViewModel(@NonNull Application application) {
        super(application);
        repository = new TestRepository(application);
    }

    public LiveData<List<TestModel>> getTests() {
        return repository.observeTests();
    }

    public LiveData<List<TestModel>> observeTests() {
        return repository.observeTests();
    }

    public List<TestModel> getAllTests() {
        return repository.getAllTests();
    }

    public LiveData<List<TestModel>> getTestsByTopic(String topicId) {
        return repository.observeTestsByTopic(topicId);
    }

    public TestModel getTestById(String testId) {
        return repository.getTestById(testId);
    }

    public TestModel getTest(String testId) {
        return repository.getTestById(testId);
    }

    public TestModel findTestById(String testId) {
        return repository.getTestById(testId);
    }

    public LiveData<List<TestProgressEntity>> observeCurrentUserTestProgress() {
        return repository.observeCurrentUserTestProgress();
    }

    public LiveData<List<TestProgressEntity>> getCurrentUserTestProgress() {
        return repository.observeCurrentUserTestProgress();
    }

    public void saveTestResult(String testId, int scorePercent, int correctAnswers, int totalQuestions) {
        repository.saveTestResult(testId, scorePercent, correctAnswers, totalQuestions);
    }
}
