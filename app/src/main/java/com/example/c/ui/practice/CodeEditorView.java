package com.example.c.ui.practice;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayDeque;
import java.util.Deque;

public class CodeEditorView extends LinearLayout {

    private final TextView lineNumbersView;
    private final EditText codeEditText;
    private final TextView errorView;

    public CodeEditorView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setBackground(createBackground(Color.parseColor("#1E1E1E"), Color.parseColor("#3B3150"), dp(12)));
        setPadding(dp(8), dp(8), dp(8), dp(8));

        LinearLayout editorRow = new LinearLayout(context);
        editorRow.setOrientation(HORIZONTAL);
        editorRow.setGravity(Gravity.TOP);
        editorRow.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        lineNumbersView = new TextView(context);
        lineNumbersView.setTextColor(Color.parseColor("#8A8A8A"));
        lineNumbersView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        lineNumbersView.setTypeface(Typeface.MONOSPACE);
        lineNumbersView.setGravity(Gravity.RIGHT | Gravity.TOP);
        lineNumbersView.setPadding(0, dp(10), dp(8), dp(10));
        editorRow.addView(lineNumbersView, new LayoutParams(dp(44), ViewGroup.LayoutParams.MATCH_PARENT));

        codeEditText = new EditText(context);
        codeEditText.setTextColor(Color.parseColor("#F5F5F5"));
        codeEditText.setHintTextColor(Color.parseColor("#8A8A8A"));
        codeEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        codeEditText.setTypeface(Typeface.MONOSPACE);
        codeEditText.setGravity(Gravity.TOP | Gravity.START);
        codeEditText.setMinLines(12);
        codeEditText.setSingleLine(false);
        codeEditText.setHorizontallyScrolling(true);
        codeEditText.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        codeEditText.setBackgroundColor(Color.TRANSPARENT);
        codeEditText.setPadding(dp(8), dp(6), dp(8), dp(8));
        codeEditText.setHint("// Напишите решение на C#");
        editorRow.addView(codeEditText, new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        addView(editorRow);

        errorView = new TextView(context);
        errorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        errorView.setTextColor(Color.parseColor("#FFB4AB"));
        errorView.setPadding(dp(8), dp(8), dp(8), dp(2));
        errorView.setText("Ошибок не найдено");
        addView(errorView, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        codeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLineNumbers();
                validateLexically();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        updateLineNumbers();
        validateLexically();
    }

    public EditText getEditText() {
        return codeEditText;
    }

    public String getCode() {
        Editable editable = codeEditText.getText();
        return editable == null ? "" : editable.toString();
    }

    public void setCode(String code) {
        codeEditText.setText(code == null ? "" : code);
        codeEditText.setSelection(codeEditText.getText().length());
        updateLineNumbers();
        validateLexically();
    }

    public void insertAtCursor(String value) {
        if (value == null) {
            return;
        }
        int start = Math.max(codeEditText.getSelectionStart(), 0);
        int end = Math.max(codeEditText.getSelectionEnd(), 0);
        int min = Math.min(start, end);
        int max = Math.max(start, end);
        codeEditText.getText().replace(min, max, value);
        codeEditText.requestFocus();
    }

    public void validateLexically() {
        String code = getCode();
        String error = findLexicalProblem(code);
        if (error == null) {
            errorView.setText("Ошибок не найдено");
            errorView.setTextColor(Color.parseColor("#A5D6A7"));
        } else {
            errorView.setText(error);
            errorView.setTextColor(Color.parseColor("#FFB4AB"));
        }
    }

    private void updateLineNumbers() {
        String code = getCode();
        int lines = 1;
        for (int i = 0; i < code.length(); i++) {
            if (code.charAt(i) == '\n') {
                lines++;
            }
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= lines; i++) {
            if (i > 1) {
                builder.append('\n');
            }
            builder.append(i);
        }
        lineNumbersView.setText(builder.toString());
    }

    private String findLexicalProblem(String code) {
        if (code == null || code.trim().isEmpty()) {
            return "Введите код решения";
        }

        Deque<Character> stack = new ArrayDeque<>();
        Deque<Integer> lineStack = new ArrayDeque<>();

        boolean inString = false;
        boolean inChar = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        boolean escaped = false;
        int line = 1;

        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            char next = i + 1 < code.length() ? code.charAt(i + 1) : '\0';

            if (c == '\n') {
                line++;
                inLineComment = false;
                continue;
            }

            if (inLineComment) {
                continue;
            }

            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i++;
                }
                continue;
            }

            if (!inString && !inChar && c == '/' && next == '/') {
                inLineComment = true;
                i++;
                continue;
            }

            if (!inString && !inChar && c == '/' && next == '*') {
                inBlockComment = true;
                i++;
                continue;
            }

            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            if (inChar) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '\'') {
                    inChar = false;
                }
                continue;
            }

            if (c == '"') {
                inString = true;
                continue;
            }

            if (c == '\'') {
                inChar = true;
                continue;
            }

            if (c == '(' || c == '{' || c == '[') {
                stack.push(c);
                lineStack.push(line);
                continue;
            }

            if (c == ')' || c == '}' || c == ']') {
                if (stack.isEmpty()) {
                    return "Лексическая ошибка, строка " + line + ": лишний символ '" + c + "'";
                }
                char open = stack.pop();
                int openLine = lineStack.pop();
                if (!matches(open, c)) {
                    return "Лексическая ошибка, строка " + line + ": символ '" + c + "' не соответствует открывающему '" + open + "' на строке " + openLine;
                }
                continue;
            }

            if (Character.isISOControl(c) && c != '\t' && c != '\r') {
                return "Лексическая ошибка, строка " + line + ": недопустимый управляющий символ";
            }
        }

        if (inString) {
            return "Лексическая ошибка: строковый литерал не закрыт кавычкой";
        }

        if (inChar) {
            return "Лексическая ошибка: символьный литерал не закрыт кавычкой";
        }

        if (inBlockComment) {
            return "Лексическая ошибка: блочный комментарий /* */ не закрыт";
        }

        if (!stack.isEmpty()) {
            char open = stack.pop();
            int openLine = lineStack.pop();
            return "Лексическая ошибка, строка " + openLine + ": не закрыт символ '" + open + "'";
        }

        return null;
    }

    private boolean matches(char open, char close) {
        return (open == '(' && close == ')')
                || (open == '{' && close == '}')
                || (open == '[' && close == ']');
    }

    private GradientDrawable createBackground(int color, int strokeColor, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
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
}
