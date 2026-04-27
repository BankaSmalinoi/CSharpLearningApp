package com.example.c.ui.test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.c.R;
import com.example.c.ui.main.MainActivity;
import com.google.android.material.button.MaterialButton;

public class TestResultActivity extends AppCompatActivity {

    private TextView tvTestPercent;
    private TextView tvTestSummary;
    private TextView tvTestComment;
    private MaterialButton btnRetakeTest;
    private MaterialButton btnBackToTests;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_result);

        tvTestPercent = findViewById(R.id.tvTestPercent);
        tvTestSummary = findViewById(R.id.tvTestSummary);
        tvTestComment = findViewById(R.id.tvTestComment);
        btnRetakeTest = findViewById(R.id.btnRetakeTest);
        btnBackToTests = findViewById(R.id.btnBackToTests);

        int correct = getIntent().getIntExtra(TestPassingActivity.EXTRA_CORRECT, 0);
        int total = getIntent().getIntExtra(TestPassingActivity.EXTRA_TOTAL, 1);
        int percent = (correct * 100) / total;

        tvTestPercent.setText(percent + "%");
        tvTestSummary.setText("Правильных ответов: " + correct + " из " + total);
        tvTestComment.setText(buildComment(percent));

        btnRetakeTest.setOnClickListener(v -> {
            Intent intent = new Intent(TestResultActivity.this, TestPassingActivity.class);
            startActivity(intent);
            finish();
        });

        btnBackToTests.setOnClickListener(v -> openMainSection(MainActivity.SECTION_TESTS));
    }

    private String buildComment(int percent) {
        if (percent >= 85) {
            return "Отличный результат. Тема усвоена хорошо.";
        } else if (percent >= 60) {
            return "Хороший результат. Рекомендуется повторить отдельные моменты темы.";
        } else {
            return "Результат ниже ожидаемого. Желательно повторить теорию и ещё раз пройти практику.";
        }
    }

    private void openMainSection(String section) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_OPEN_SECTION, section);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}