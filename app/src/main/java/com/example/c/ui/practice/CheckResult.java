package com.example.c.ui.practice;

import java.util.ArrayList;
import java.util.List;

public class CheckResult {
    public boolean success;
    public String status = "";
    public String message = "";
    public List<CheckTestResult> tests = new ArrayList<>();

    public String getExpectedOutputForStorage() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tests.size(); i++) {
            if (i > 0) builder.append("\n---\n");
            builder.append(tests.get(i).expectedOutput == null ? "" : tests.get(i).expectedOutput);
        }
        return builder.toString();
    }

    public String getActualOutputForStorage() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tests.size(); i++) {
            if (i > 0) builder.append("\n---\n");
            builder.append(tests.get(i).actualOutput == null ? "" : tests.get(i).actualOutput);
        }
        return builder.toString();
    }

    public String getErrorForStorage() {
        StringBuilder builder = new StringBuilder();
        for (CheckTestResult test : tests) {
            if (test.error != null && !test.error.trim().isEmpty()) {
                if (builder.length() > 0) builder.append('\n');
                builder.append(test.error);
            }
        }
        return builder.toString();
    }
}
