package com.example.c.data.repository;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.c.data.db.entity.TestProgressEntity;
import com.example.c.data.db.AppDatabase;
import com.example.c.data.model.test.TestModel;
import com.example.c.data.model.test.TestOption;
import com.example.c.data.model.test.TestQuestion;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestRepository {
    private static final String TAG = "TestRepository";
    private static final String TESTS_ASSET_PATH = "data/tests.json";

    private final Context context;

    private List<TestModel> cachedTests;
    private final MutableLiveData<List<TestModel>> testsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<TestProgressEntity>> progressLiveData =
            new MutableLiveData<List<TestProgressEntity>>();

    public TestRepository(Context context) {
        this.context = context.getApplicationContext();
        progressLiveData.setValue(new ArrayList<TestProgressEntity>());
    }

    /**
     * Синхронный список тестов. Оставлен для Activity/Repository-кода.
     */
    public List<TestModel> getTests() {
        return getAllTests();
    }

    public List<TestModel> getAllTests() {
        ensureTestsLoaded();
        return new ArrayList<>(cachedTests);
    }

    /**
     * LiveData-версия для TestViewModel/Fragment.
     */
    public LiveData<List<TestModel>> observeTests() {
        ensureTestsLoaded();
        return testsLiveData;
    }

    public LiveData<List<TestModel>> getTestsLiveData() {
        return observeTests();
    }

    public LiveData<List<TestModel>> observeTestsByTopic(String topicId) {
        return new MutableLiveData<>(getTestsByTopic(topicId));
    }

    public List<TestModel> loadTests() {
        return getAllTests();
    }

    public TestModel getTestById(String testId) {
        if (testId == null || testId.trim().isEmpty()) {
            return null;
        }

        for (TestModel test : getAllTests()) {
            if (testId.equals(test.id)) {
                return test;
            }
        }
        return null;
    }

    public TestModel getTest(String testId) {
        return getTestById(testId);
    }

    public TestModel findTestById(String testId) {
        return getTestById(testId);
    }

    public List<TestModel> getTestsByTopic(String topicId) {
        List<TestModel> allTests = getAllTests();

        // Когда раздел "Тесты" открыт из бокового меню, topicId обычно не передаётся.
        // В этом случае нужно показывать все тесты, а не пустой список.
        if (topicId == null || topicId.trim().isEmpty()) {
            return allTests;
        }

        List<TestModel> result = new ArrayList<>();
        for (TestModel test : allTests) {
            if (topicId.equals(test.topicId)) {
                result.add(test);
            }
        }
        return result;
    }

    public List<TestModel> getTestsByTopicId(String topicId) {
        return getTestsByTopic(topicId);
    }

    public List<TestModel> getTestsForTopic(String topicId) {
        return getTestsByTopic(topicId);
    }


    public boolean isTheoryReadForTest(TestModel test) {
        if (test == null || test.topicId == null || test.topicId.trim().isEmpty()) {
            return false;
        }
        try {
            return AppDatabase.getInstance(context).topicProgressDao().isTheoryReadInt(test.topicId) > 0;
        } catch (Exception e) {
            Log.e(TAG, "Не удалось проверить доступность теста", e);
            return false;
        }
    }

    public boolean isTheoryRead(String topicId) {
        if (topicId == null || topicId.trim().isEmpty()) {
            return false;
        }
        try {
            return AppDatabase.getInstance(context).topicProgressDao().isTheoryReadInt(topicId) > 0;
        } catch (Exception e) {
            Log.e(TAG, "Не удалось проверить прочтение теории", e);
            return false;
        }
    }

    public LiveData<List<TestProgressEntity>> observeCurrentUserTestProgress() {
        return progressLiveData;
    }

    public void saveTestResult(String testId, int scorePercent, int correctAnswers, int totalQuestions) {
        // Здесь намеренно не ломаем запуск тестов из-за БД.
        // Если в проекте уже есть DAO для прогресса, его можно подключить отдельно,
        // но отображение и прохождение тестов не должно зависеть от ошибки сохранения статистики.
        Log.d(TAG, "Результат теста: testId=" + testId
                + ", scorePercent=" + scorePercent
                + ", correctAnswers=" + correctAnswers
                + ", totalQuestions=" + totalQuestions);
    }

    private void ensureTestsLoaded() {
        if (cachedTests == null) {
            cachedTests = loadTestsFromAssets();
            testsLiveData.setValue(new ArrayList<>(cachedTests));
        }
    }

    private List<TestModel> loadTestsFromAssets() {
        List<TestModel> result = new ArrayList<>();

        try {
            String json = readAssetFile(TESTS_ASSET_PATH);
            JsonElement rootElement = new JsonParser().parse(json);

            JsonArray testsArray = null;

            if (rootElement != null && rootElement.isJsonObject()) {
                JsonObject rootObject = rootElement.getAsJsonObject();

                if (rootObject.has("tests") && rootObject.get("tests").isJsonArray()) {
                    testsArray = rootObject.getAsJsonArray("tests");
                } else if (rootObject.has("data") && rootObject.get("data").isJsonArray()) {
                    testsArray = rootObject.getAsJsonArray("data");
                }
            } else if (rootElement != null && rootElement.isJsonArray()) {
                testsArray = rootElement.getAsJsonArray();
            }

            if (testsArray == null) {
                Log.e(TAG, "В файле assets/" + TESTS_ASSET_PATH + " не найден массив tests");
                return result;
            }

            int testIndex = 0;
            for (JsonElement testElement : testsArray) {
                if (!testElement.isJsonObject()) {
                    continue;
                }

                TestModel test = parseTest(testElement.getAsJsonObject(), testIndex);
                if (test != null && test.id != null && !test.id.trim().isEmpty()
                        && test.title != null && !test.title.trim().isEmpty()
                        && test.questions != null && !test.questions.isEmpty()) {
                    result.add(test);
                } else {
                    Log.w(TAG, "Пропущен некорректный тест с индексом " + testIndex);
                }

                testIndex++;
            }

            Log.d(TAG, "Загружено тестов из assets/" + TESTS_ASSET_PATH + ": " + result.size());
        } catch (IOException e) {
            Log.e(TAG, "Не удалось открыть assets/" + TESTS_ASSET_PATH, e);
        } catch (JsonSyntaxException | IllegalStateException e) {
            Log.e(TAG, "Ошибка формата JSON в assets/" + TESTS_ASSET_PATH, e);
        } catch (Exception e) {
            Log.e(TAG, "Не удалось загрузить тесты", e);
        }

        return result;
    }

    private TestModel parseTest(JsonObject object, int testIndex) {
        TestModel test = new TestModel();

        test.id = firstString(object, "id", "testId", "slug");
        if (isBlank(test.id)) {
            test.id = "test_" + (testIndex + 1);
        }

        test.topicId = firstString(object, "topicId", "topic_id", "topic");
        test.title = firstString(object, "title", "name");
        if (isBlank(test.title)) {
            test.title = "Тест " + (testIndex + 1);
        }

        test.description = firstString(object, "description", "desc");
        test.questions = new ArrayList<>();

        JsonArray questionsArray = firstArray(object, "questions", "items");
        if (questionsArray != null) {
            int questionIndex = 0;
            for (JsonElement questionElement : questionsArray) {
                if (questionElement.isJsonObject()) {
                    TestQuestion question = parseQuestion(
                            questionElement.getAsJsonObject(),
                            test.id,
                            questionIndex
                    );

                    if (question != null && !isBlank(question.text)) {
                        test.questions.add(question);
                    }
                }
                questionIndex++;
            }
        }

        if (isBlank(test.description)) {
            int count = test.questions != null ? test.questions.size() : 0;
            test.description = "Вопросов: " + count;
        }

        return test;
    }

    private TestQuestion parseQuestion(JsonObject object, String testId, int questionIndex) {
        TestQuestion question = new TestQuestion();

        question.id = firstString(object, "id", "questionId", "qid");
        if (isBlank(question.id)) {
            question.id = testId + "_q" + (questionIndex + 1);
        }

        question.type = normalizeType(firstString(object, "type", "questionType"));
        question.text = firstString(object, "text", "question", "title");
        question.question = question.text;
        question.explanation = firstString(object, "explanation", "hint", "comment");

        question.options = parseOptions(firstArray(object, "options", "variants"));
        question.answers = parseStringList(firstArray(object, "answers", "acceptedAnswers"));
        question.left = parseStringList(firstArray(object, "left", "leftItems"));
        question.right = parseStringList(firstArray(object, "right", "rightItems"));
        question.correctPairs = parseStringMap(firstObject(object, "correctPairs", "pairs"));

        String rawCorrectAnswer = correctValueToString(firstElement(object, "correctAnswer", "answer"));
        List<String> rawCorrectAnswers = correctValuesToStringList(firstArray(object, "correctAnswers", "answersIndexes"));

        question.correctAnswer = convertAnswerToOptionIdIfNeeded(rawCorrectAnswer, question.options);
        question.correctAnswers = convertAnswersToOptionIdsIfNeeded(rawCorrectAnswers, question.options);

        if (question.correctAnswers == null) {
            question.correctAnswers = new ArrayList<>();
        }

        if (question.answers == null) {
            question.answers = new ArrayList<>();
        }

        // Для ручного ввода правильные ответы чаще лежат в answers.
        if (isInputType(question.type)) {
            if (isBlank(question.correctAnswer) && !question.answers.isEmpty()) {
                question.correctAnswer = question.answers.get(0);
            }

            if (question.correctAnswers.isEmpty() && !question.answers.isEmpty()) {
                question.correctAnswers.addAll(question.answers);
            }
        }

        applyCorrectFlags(question);

        if (isBlank(question.type)) {
            question.type = guessType(question);
        }

        return question;
    }

    private List<TestOption> parseOptions(JsonArray array) {
        List<TestOption> result = new ArrayList<>();

        if (array == null) {
            return result;
        }

        int index = 0;
        for (JsonElement element : array) {
            TestOption option = new TestOption();

            option.id = String.valueOf(index);

            if (element.isJsonPrimitive()) {
                option.text = element.getAsString();
            } else if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();

                String parsedId = firstString(object, "id", "value", "key");
                if (!isBlank(parsedId)) {
                    option.id = parsedId;
                }

                option.text = firstString(object, "text", "title", "label", "name");
                option.isCorrect = firstBoolean(object, "isCorrect", "correct");
                option.correct = option.isCorrect;
            }

            if (!isBlank(option.text)) {
                result.add(option);
            }

            index++;
        }

        return result;
    }

    private void applyCorrectFlags(TestQuestion question) {
        if (question.options == null || question.options.isEmpty()) {
            return;
        }

        Set<String> correctIds = new HashSet<>();

        if (!isBlank(question.correctAnswer)) {
            correctIds.add(question.correctAnswer);
        }

        if (question.correctAnswers != null) {
            correctIds.addAll(question.correctAnswers);
        }

        for (TestOption option : question.options) {
            if (option == null) {
                continue;
            }

            if (option.isCorrect || option.correct) {
                if (question.correctAnswers == null) {
                    question.correctAnswers = new ArrayList<>();
                }

                if (!isBlank(option.id) && !question.correctAnswers.contains(option.id)) {
                    question.correctAnswers.add(option.id);
                }

                if (isBlank(question.correctAnswer)) {
                    question.correctAnswer = option.id;
                }
            }

            if (correctIds.contains(option.id)) {
                option.isCorrect = true;
                option.correct = true;
            }
        }
    }

    private String convertAnswerToOptionIdIfNeeded(String answer, List<TestOption> options) {
        if (isBlank(answer)) {
            return answer;
        }

        if (options == null || options.isEmpty()) {
            return answer;
        }

        // Если correctAnswer в JSON задан числом 0/1/2, id вариантов у нас тоже 0/1/2.
        if (isInteger(answer)) {
            int index = Integer.parseInt(answer);
            if (index >= 0 && index < options.size()) {
                return options.get(index).id;
            }
        }

        return answer;
    }

    private List<String> convertAnswersToOptionIdsIfNeeded(List<String> answers, List<TestOption> options) {
        List<String> result = new ArrayList<>();

        if (answers == null) {
            return result;
        }

        for (String answer : answers) {
            result.add(convertAnswerToOptionIdIfNeeded(answer, options));
        }

        return result;
    }

    private String guessType(TestQuestion question) {
        if (question.correctPairs != null && !question.correctPairs.isEmpty()) {
            return "matching";
        }

        if (question.options != null && !question.options.isEmpty()) {
            if (question.correctAnswers != null && question.correctAnswers.size() > 1) {
                return "checkbox";
            }
            return "radio";
        }

        return "input";
    }

    private boolean isInputType(String type) {
        return "input".equals(type) || "text".equals(type);
    }

    private String normalizeType(String type) {
        if (type == null) {
            return "";
        }

        String value = type.trim().toLowerCase();

        if (value.equals("single") || value.equals("single_choice")
                || value.equals("one") || value.equals("choice")) {
            return "radio";
        }

        if (value.equals("multiple") || value.equals("multiple_choice")
                || value.equals("multi")) {
            return "checkbox";
        }

        if (value.equals("manual") || value.equals("open") || value.equals("text_answer")) {
            return "input";
        }

        if (value.equals("match") || value.equals("pairs") || value.equals("connect")) {
            return "matching";
        }

        return value;
    }

    private JsonArray firstArray(JsonObject object, String... names) {
        JsonElement element = firstElement(object, names);
        return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
    }

    private JsonObject firstObject(JsonObject object, String... names) {
        JsonElement element = firstElement(object, names);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private JsonElement firstElement(JsonObject object, String... names) {
        if (object == null || names == null) {
            return null;
        }

        for (String name : names) {
            if (object.has(name) && !object.get(name).isJsonNull()) {
                return object.get(name);
            }
        }

        return null;
    }

    private String firstString(JsonObject object, String... names) {
        JsonElement element = firstElement(object, names);

        if (element == null || !element.isJsonPrimitive()) {
            return "";
        }

        try {
            return element.getAsString();
        } catch (Exception e) {
            return "";
        }
    }

    private boolean firstBoolean(JsonObject object, String... names) {
        JsonElement element = firstElement(object, names);

        if (element == null || !element.isJsonPrimitive()) {
            return false;
        }

        try {
            return element.getAsBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    private String correctValueToString(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
            return "";
        }

        try {
            return element.getAsString();
        } catch (Exception e) {
            return "";
        }
    }

    private List<String> correctValuesToStringList(JsonArray array) {
        List<String> result = new ArrayList<>();

        if (array == null) {
            return result;
        }

        for (JsonElement element : array) {
            String value = correctValueToString(element);
            if (!isBlank(value)) {
                result.add(value);
            }
        }

        return result;
    }

    private List<String> parseStringList(JsonArray array) {
        List<String> result = new ArrayList<>();

        if (array == null) {
            return result;
        }

        for (JsonElement element : array) {
            if (element == null || element.isJsonNull()) {
                continue;
            }

            if (element.isJsonPrimitive()) {
                String value = element.getAsString();
                if (!isBlank(value)) {
                    result.add(value);
                }
            }
        }

        return result;
    }

    private Map<String, String> parseStringMap(JsonObject object) {
        Map<String, String> result = new LinkedHashMap<>();

        if (object == null) {
            return result;
        }

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            JsonElement value = entry.getValue();

            if (value != null && value.isJsonPrimitive()) {
                result.put(entry.getKey(), value.getAsString());
            }
        }

        return result;
    }

    private boolean isInteger(String value) {
        if (isBlank(value)) {
            return false;
        }

        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String readAssetFile(String fileName) throws IOException {
        AssetManager assetManager = context.getAssets();

        try (InputStream inputStream = assetManager.open(fileName);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }

            return builder.toString();
        }
    }
}
