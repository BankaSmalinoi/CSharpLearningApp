package com.example.c.data.model.practice;

import java.util.ArrayList;
import java.util.List;

public class PracticeTasksResponse {
    public List<PracticeTask> practices;
    public List<PracticeTask> tasks;

    public PracticeTasksResponse() {
        practices = new ArrayList<>();
        tasks = new ArrayList<>();
    }

    public List<PracticeTask> getPractices() {
        if (practices != null && !practices.isEmpty()) {
            return practices;
        }
        return tasks != null ? tasks : new ArrayList<PracticeTask>();
    }
}
