package com.example.c.ui.practice;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PracticeCheckerClient {

    private static final String CHECK_URL = "http://10.0.2.2:8000/check";

    public interface Callback {
        void onSuccess(CheckResult result);
        void onError(String message);
    }

    public static void checkAsync(final String taskId, final String code, final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    CheckResult result = check(taskId, code);
                    callback.onSuccess(result);
                } catch (Exception e) {
                    callback.onError("Не удалось подключиться к проверяющему модулю: " + e.getMessage());
                }
            }
        }).start();
    }

    private static CheckResult check(String taskId, String code) throws Exception {
        URL url = new URL(CHECK_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json");

        JSONObject request = new JSONObject();
        request.put("taskId", taskId);
        request.put("code", code);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(request.toString().getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();

        int responseCode = connection.getResponseCode();
        InputStream inputStream = responseCode >= 200 && responseCode < 300
                ? connection.getInputStream()
                : connection.getErrorStream();
        String responseText = readStream(inputStream);
        connection.disconnect();

        if (responseCode < 200 || responseCode >= 300) {
            throw new Exception("HTTP " + responseCode + ": " + responseText);
        }

        return parseResult(responseText);
    }

    private static CheckResult parseResult(String json) throws Exception {
        JSONObject root = new JSONObject(json);
        CheckResult result = new CheckResult();
        result.success = root.optBoolean("success", false);
        result.status = root.optString("status", "");
        result.message = root.optString("message", "");

        JSONArray array = root.optJSONArray("results");
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                CheckTestResult item = new CheckTestResult();
                item.input = object.optString("input", "");
                item.expectedOutput = object.optString("expectedOutput", "");
                item.actualOutput = object.optString("actualOutput", "");
                item.passed = object.optBoolean("passed", false);
                item.error = object.isNull("error") ? "" : object.optString("error", "");
                result.tests.add(item);
            }
        }
        return result;
    }

    private static String readStream(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }
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
