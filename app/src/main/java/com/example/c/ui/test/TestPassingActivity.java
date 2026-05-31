package com.example.c.ui.test;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;

import com.example.c.R;
import com.example.c.data.model.test.TestModel;
import com.example.c.data.model.test.TestOption;
import com.example.c.data.model.test.TestQuestion;
import com.example.c.viewmodel.TestViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestPassingActivity extends AppCompatActivity {

    public static final String EXTRA_TEST_ID = "test_id";
    public static final String EXTRA_CORRECT = "extra_correct";
    public static final String EXTRA_TOTAL = "extra_total";
    public static final String EXTRA_SCORE_PERCENT = "extra_score_percent";
    public static final String EXTRA_TEST_TITLE = "extra_test_title";

    private Toolbar toolbar;
    private TextView textTitle;
    private TextView textSubtitle;
    private LinearLayout questionsContainer;
    private Button buttonFinish;
    private TextView textEmpty;

    private TestViewModel testViewModel;
    private TestModel currentTest;

    private final Map<String, RadioGroup> radioAnswers = new HashMap<>();
    private final Map<String, ArrayList<CheckBox>> checkboxAnswers = new HashMap<>();
    private final Map<String, EditText> inputAnswers = new HashMap<>();
    private final Map<String, Map<String, Spinner>> matchingAnswers = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_passing);

        initViews();
        setupToolbar();

        testViewModel = new ViewModelProvider(this).get(TestViewModel.class);

        String testId = getStringExtraAny(EXTRA_TEST_ID, "TEST_ID", "testId", "id");
        if (isBlank(testId)) {
            showEmptyState("Не удалось открыть тест: отсутствует идентификатор теста.");
            return;
        }

        currentTest = testViewModel.getTestById(testId);
        if (currentTest == null) {
            currentTest = testViewModel.findTestById(testId);
        }
        if (currentTest == null) {
            showEmptyState("Тест не найден.");
            return;
        }

        renderTest();

        buttonFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishTest();
            }
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        textTitle = findViewById(R.id.textTitle);
        textSubtitle = findViewById(R.id.textSubtitle);
        questionsContainer = findViewById(R.id.questionsContainer);
        buttonFinish = findViewById(R.id.buttonFinish);
        textEmpty = findViewById(R.id.textEmpty);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void renderTest() {
        textTitle.setText(safe(currentTest.title, "Тест"));

        int questionsCount = currentTest.questions == null ? 0 : currentTest.questions.size();
        textSubtitle.setText("Вопросов: " + questionsCount);
        textEmpty.setVisibility(View.GONE);
        questionsContainer.setVisibility(View.VISIBLE);
        buttonFinish.setVisibility(View.VISIBLE);

        questionsContainer.removeAllViews();
        radioAnswers.clear();
        checkboxAnswers.clear();
        inputAnswers.clear();
        matchingAnswers.clear();

        if (questionsCount == 0) {
            showEmptyState("В этом тесте пока нет вопросов.");
            return;
        }

        for (int i = 0; i < currentTest.questions.size(); i++) {
            TestQuestion question = currentTest.questions.get(i);
            questionsContainer.addView(createQuestionCard(i, question));
        }
    }

    private View createQuestionCard(int index, TestQuestion question) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(createRoundedBackground(Color.WHITE, Color.parseColor("#E3DDF1")));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(20);
        card.setLayoutParams(cardParams);
        card.setElevation(dp(1));

        TextView numberView = new TextView(this);
        numberView.setText((index + 1) + ". " + safe(getQuestionText(question), "Вопрос"));
        numberView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        numberView.setTypeface(Typeface.DEFAULT_BOLD);
        numberView.setTextColor(Color.parseColor("#222222"));
        numberView.setLineSpacing(0f, 1.15f);
        LinearLayout.LayoutParams numParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        numParams.bottomMargin = dp(14);
        numberView.setLayoutParams(numParams);
        card.addView(numberView);

        addQuestionContent(card, question);
        return card;
    }

    private void addQuestionContent(LinearLayout card, TestQuestion question) {
        String type = normalizeType(question.type);

        if (isMatchingQuestion(question, type)) {
            renderMatchingQuestion(card, question);
            return;
        }

        if (type.contains("checkbox") || type.contains("multiple") || type.contains("multi")) {
            renderCheckboxQuestion(card, question);
            return;
        }

        if (type.contains("input") || type.contains("text") || type.contains("open")) {
            renderInputQuestion(card, question);
            return;
        }

        renderRadioQuestion(card, question);
    }

    private void renderRadioQuestion(LinearLayout card, TestQuestion question) {
        RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(LinearLayout.VERTICAL);
        radioGroup.setPadding(0, 0, 0, 0);

        for (TestOption option : safeOptions(question.options)) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(safe(option == null ? null : option.text, ""));
            radioButton.setTag(buildOptionTag(option));
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            radioButton.setPadding(0, dp(4), 0, dp(8));
            radioGroup.addView(radioButton);
        }

        radioAnswers.put(getQuestionKey(question), radioGroup);
        card.addView(radioGroup);
    }

    private void renderCheckboxQuestion(LinearLayout card, TestQuestion question) {
        ArrayList<CheckBox> checkBoxes = new ArrayList<>();

        for (TestOption option : safeOptions(question.options)) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(safe(option == null ? null : option.text, ""));
            checkBox.setTag(buildOptionTag(option));
            checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            checkBox.setPadding(0, dp(4), 0, dp(8));
            checkBoxes.add(checkBox);
            card.addView(checkBox);
        }

        checkboxAnswers.put(getQuestionKey(question), checkBoxes);
    }

    private void renderInputQuestion(LinearLayout card, TestQuestion question) {
        EditText editText = new EditText(this);
        editText.setHint("Введите ответ");
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setMinLines(1);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        editText.setPadding(dp(12), dp(12), dp(12), dp(12));
        editText.setBackground(createRoundedBackground(Color.parseColor("#FAFAFD"), Color.parseColor("#D9CFF0")));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(4);
        editText.setLayoutParams(params);
        inputAnswers.put(getQuestionKey(question), editText);
        card.addView(editText);
    }

    private void renderMatchingQuestion(LinearLayout card, TestQuestion question) {
        TextView helperText = new TextView(this);
        helperText.setText("Сопоставьте элементы: выберите подходящий вариант для каждого пункта.");
        helperText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        helperText.setTextColor(Color.parseColor("#666666"));
        LinearLayout.LayoutParams helperParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        helperParams.bottomMargin = dp(10);
        helperText.setLayoutParams(helperParams);
        card.addView(helperText);

        List<String> leftItems = getMatchingLeftItems(question);
        List<String> rightItems = getMatchingRightItems(question);
        Map<String, Spinner> spinnerMap = new HashMap<>();

        if (leftItems.isEmpty() || rightItems.isEmpty()) {
            TextView errorText = new TextView(this);
            errorText.setText("Не удалось отобразить вопрос на сопоставление: отсутствуют данные.");
            errorText.setTextColor(Color.parseColor("#B00020"));
            card.addView(errorText);
            matchingAnswers.put(getQuestionKey(question), spinnerMap);
            return;
        }

        ArrayList<String> spinnerItems = new ArrayList<>();
        spinnerItems.add("Выберите вариант");
        spinnerItems.addAll(rightItems);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                spinnerItems
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for (String leftItem : leftItems) {
            LinearLayout rowCard = new LinearLayout(this);
            rowCard.setOrientation(LinearLayout.VERTICAL);
            rowCard.setPadding(dp(12), dp(12), dp(12), dp(12));
            rowCard.setBackground(createRoundedBackground(Color.parseColor("#FAFAFD"), Color.parseColor("#E2D9F6")));
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            rowParams.bottomMargin = dp(12);
            rowCard.setLayoutParams(rowParams);

            TextView leftView = new TextView(this);
            leftView.setText(safe(leftItem, ""));
            leftView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            leftView.setTypeface(Typeface.DEFAULT_BOLD);
            leftView.setTextColor(Color.parseColor("#222222"));
            LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            leftParams.bottomMargin = dp(8);
            leftView.setLayoutParams(leftParams);
            rowCard.addView(leftView);

            Spinner spinner = new Spinner(this, Spinner.MODE_DROPDOWN);
            spinner.setAdapter(adapter);
            spinner.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            rowCard.addView(spinner);

            spinnerMap.put(leftItem, spinner);
            card.addView(rowCard);
        }

        matchingAnswers.put(getQuestionKey(question), spinnerMap);
    }

    private void finishTest() {
        if (currentTest == null || currentTest.questions == null || currentTest.questions.isEmpty()) {
            Toast.makeText(this, "Нет данных для завершения теста", Toast.LENGTH_SHORT).show();
            return;
        }

        int total = currentTest.questions.size();
        int correct = 0;

        for (TestQuestion question : currentTest.questions) {
            if (isAnswerCorrect(question)) {
                correct++;
            }
        }

        int scorePercent = total == 0 ? 0 : Math.round((correct * 100f) / total);

        try {
            testViewModel.saveTestResult(currentTest.id, scorePercent, correct, total);
        } catch (Exception ignored) {
        }

        Intent intent = new Intent(this, TestResultActivity.class);
        intent.putExtra(EXTRA_TEST_ID, currentTest.id);
        intent.putExtra(EXTRA_CORRECT, correct);
        intent.putExtra(EXTRA_TOTAL, total);
        intent.putExtra(EXTRA_SCORE_PERCENT, scorePercent);
        intent.putExtra(EXTRA_TEST_TITLE, safe(currentTest.title, "Тест"));
        startActivity(intent);
        finish();
    }

    private boolean isAnswerCorrect(TestQuestion question) {
        String type = normalizeType(question.type);

        if (isMatchingQuestion(question, type)) {
            return isMatchingAnswerCorrect(question);
        }

        if (type.contains("checkbox") || type.contains("multiple") || type.contains("multi")) {
            return isCheckboxAnswerCorrect(question);
        }

        if (type.contains("input") || type.contains("text") || type.contains("open")) {
            return isInputAnswerCorrect(question);
        }

        return isRadioAnswerCorrect(question);
    }

    private boolean isRadioAnswerCorrect(TestQuestion question) {
        RadioGroup radioGroup = radioAnswers.get(getQuestionKey(question));
        if (radioGroup == null) {
            return false;
        }

        int checkedId = radioGroup.getCheckedRadioButtonId();
        if (checkedId == -1) {
            return false;
        }

        View checkedView = radioGroup.findViewById(checkedId);
        if (!(checkedView instanceof RadioButton)) {
            return false;
        }

        Object tag = checkedView.getTag();
        String userAnswer = normalizeValue(tag == null ? null : String.valueOf(tag));
        String correctAnswer = normalizeValue(getCorrectSingleChoiceAnswer(question));
        return !isBlank(userAnswer) && userAnswer.equals(correctAnswer);
    }

    private boolean isCheckboxAnswerCorrect(TestQuestion question) {
        ArrayList<CheckBox> checkBoxes = checkboxAnswers.get(getQuestionKey(question));
        if (checkBoxes == null || checkBoxes.isEmpty()) {
            return false;
        }

        Set<String> userAnswers = new HashSet<>();
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                Object tag = checkBox.getTag();
                userAnswers.add(normalizeValue(tag == null ? null : String.valueOf(tag)));
            }
        }

        Set<String> correctAnswers = new HashSet<>();
        for (String item : getCorrectCheckboxAnswers(question)) {
            correctAnswers.add(normalizeValue(item));
        }

        return !correctAnswers.isEmpty() && userAnswers.equals(correctAnswers);
    }

    private boolean isInputAnswerCorrect(TestQuestion question) {
        EditText editText = inputAnswers.get(getQuestionKey(question));
        if (editText == null) {
            return false;
        }

        String userAnswer = normalizeValue(editText.getText() == null ? null : editText.getText().toString());
        if (isBlank(userAnswer)) {
            return false;
        }

        if (!isBlank(question.correctAnswer) && userAnswer.equals(normalizeValue(question.correctAnswer))) {
            return true;
        }

        if (question.answers != null) {
            for (String answer : question.answers) {
                if (userAnswer.equals(normalizeValue(answer))) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isMatchingAnswerCorrect(TestQuestion question) {
        Map<String, Spinner> spinnerMap = matchingAnswers.get(getQuestionKey(question));
        if (spinnerMap == null || spinnerMap.isEmpty()) {
            return false;
        }

        Map<String, String> correctPairs = question.correctPairs == null ? Collections.<String, String>emptyMap() : question.correctPairs;
        if (correctPairs.isEmpty()) {
            return false;
        }

        for (Map.Entry<String, String> entry : correctPairs.entrySet()) {
            Spinner spinner = spinnerMap.get(entry.getKey());
            if (spinner == null || spinner.getSelectedItem() == null) {
                return false;
            }

            String selected = String.valueOf(spinner.getSelectedItem());
            if (spinner.getSelectedItemPosition() == 0) {
                return false;
            }

            if (!normalizeValue(selected).equals(normalizeValue(entry.getValue()))) {
                return false;
            }
        }

        return true;
    }

    private List<String> getCorrectCheckboxAnswers(TestQuestion question) {
        List<String> result = new ArrayList<>();

        if (question.correctAnswers != null && !question.correctAnswers.isEmpty()) {
            result.addAll(question.correctAnswers);
        }

        if (result.isEmpty()) {
            for (TestOption option : safeOptions(question.options)) {
                if (option != null && (option.isCorrect || option.correct)) {
                    result.add(buildOptionTag(option));
                }
            }
        }

        return result;
    }

    private String getCorrectSingleChoiceAnswer(TestQuestion question) {
        if (!isBlank(question.correctAnswer)) {
            return question.correctAnswer;
        }

        for (TestOption option : safeOptions(question.options)) {
            if (option != null && (option.isCorrect || option.correct)) {
                return buildOptionTag(option);
            }
        }

        return "";
    }

    private boolean isMatchingQuestion(TestQuestion question, String type) {
        if (type.contains("match") || type.contains("соответ")) {
            return true;
        }

        return (question.left != null && !question.left.isEmpty())
                || (question.right != null && !question.right.isEmpty())
                || (question.correctPairs != null && !question.correctPairs.isEmpty());
    }

    private List<String> getMatchingLeftItems(TestQuestion question) {
        if (question.left != null && !question.left.isEmpty()) {
            return question.left;
        }
        if (question.correctPairs != null && !question.correctPairs.isEmpty()) {
            return new ArrayList<>(question.correctPairs.keySet());
        }
        return new ArrayList<>();
    }

    private List<String> getMatchingRightItems(TestQuestion question) {
        if (question.right != null && !question.right.isEmpty()) {
            return new ArrayList<>(new LinkedHashSet<>(question.right));
        }
        if (question.correctPairs != null && !question.correctPairs.isEmpty()) {
            return new ArrayList<>(new LinkedHashSet<>(question.correctPairs.values()));
        }
        return new ArrayList<>();
    }

    private List<TestOption> safeOptions(List<TestOption> options) {
        return options == null ? Collections.<TestOption>emptyList() : options;
    }

    private String buildOptionTag(TestOption option) {
        if (option == null) {
            return "";
        }
        if (!isBlank(option.id)) {
            return option.id;
        }
        return safe(option.text, "");
    }

    private String getQuestionKey(TestQuestion question) {
        if (question != null && !isBlank(question.id)) {
            return question.id;
        }
        return String.valueOf(question == null ? 0 : question.hashCode());
    }

    private String getQuestionText(TestQuestion question) {
        if (question == null) {
            return "";
        }
        if (!isBlank(question.text)) {
            return question.text;
        }
        return safe(question.question, "");
    }

    private String normalizeType(String type) {
        return normalizeValue(type);
    }

    private String normalizeValue(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase().replace("ё", "е");
    }

    private String getStringExtraAny(String... keys) {
        Intent intent = getIntent();
        if (intent == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            String value = intent.getStringExtra(key);
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private void showEmptyState(String message) {
        textTitle.setText("Тест");
        textSubtitle.setText("");
        questionsContainer.removeAllViews();
        questionsContainer.setVisibility(View.GONE);
        buttonFinish.setVisibility(View.GONE);
        textEmpty.setText(message);
        textEmpty.setVisibility(View.VISIBLE);
    }

    private GradientDrawable createRoundedBackground(int fillColor, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(dp(16));
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        ));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }
}
