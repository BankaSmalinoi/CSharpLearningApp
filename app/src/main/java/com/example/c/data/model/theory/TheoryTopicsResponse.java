package com.example.c.data.model.theory;

import java.util.ArrayList;
import java.util.List;

public class TheoryTopicsResponse {
    public List<TheoryTopic> topics;

    public TheoryTopicsResponse() {
        topics = new ArrayList<>();
    }

    public List<TheoryTopic> getTopics() {
        return topics != null ? topics : new ArrayList<TheoryTopic>();
    }

    public void setTopics(List<TheoryTopic> topics) {
        this.topics = topics;
    }
}
