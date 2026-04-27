package com.example.c.ui.practice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.c.R;
import com.example.c.ui.main.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class PracticeTaskActivity extends AppCompatActivity {

    private TextInputEditText etCodeInput;
    private TextView tvPracticeResult;
    private MaterialButton btnCheckPractice;
    private MaterialButton btnBackPracticeList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_task);

        etCodeInput = findViewById(R.id.etCodeInput);
        tvPracticeResult = findViewById(R.id.tvPracticeResult);
        btnCheckPractice = findViewById(R.id.btnCheckPractice);
        btnBackPracticeList = findViewById(R.id.btnBackPracticeList);

        btnCheckPractice.setOnClickListener(v -> checkCode());
        btnBackPracticeList.setOnClickListener(v -> openMainSection(MainActivity.SECTION_PRACTICE));
    }

    private void checkCode() {
        String code = etCodeInput.getText() != null ? etCodeInput.getText().toString().trim() : "";

        if (code.isEmpty()) {
            tvPracticeResult.setText("Код не введён. Введите решение и повторите проверку.");
            return;
        }

        boolean hasInt = code.contains("int");
        boolean hasConsole = code.contains("Console.WriteLine");

        if (hasInt && hasConsole) {
            tvPracticeResult.setText("Проверка пройдена успешно. В решении найдены объявление переменной и вывод значения на экран.");
            Toast.makeText(this, "Задание проверено успешно", Toast.LENGTH_SHORT).show();
        } else {
            tvPracticeResult.setText("Проверка не пройдена. Убедитесь, что вы создали переменную типа int и выводите её через Console.WriteLine(...).");
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