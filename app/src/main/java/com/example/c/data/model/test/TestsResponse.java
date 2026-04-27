package com.example.c.data.model.test;

import java.util.ArrayList;
import java.util.List;

public class TestsResponse {
    public List<TestModel> tests;

    public TestsResponse() {
        tests = new ArrayList<>();
    }

    public List<TestModel> getTests() {
        return tests != null ? tests : new ArrayList<TestModel>();
    }

    public void setTests(List<TestModel> tests) {
        this.tests = tests;
    }
}
