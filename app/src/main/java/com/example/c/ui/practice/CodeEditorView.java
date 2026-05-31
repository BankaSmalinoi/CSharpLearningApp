package com.example.c.ui.practice;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayDeque;
import java.util.Deque;

public class CodeEditorView extends LinearLayout {

    private static final int EDITOR_BACKGROUND = Color.rgb(30, 30, 30);
    private static final int EDITOR_TEXT = Color.rgb(245, 245, 245);
    private static final int EDITOR_HINT = Color.rgb(150, 150, 150);
    private static final int EDITOR_LINE_NUMBER = Color.rgb(145, 145, 145);
    private static final int EDITOR_SELECTION = Color.rgb(90, 75, 130);

    private final TextView lineNumbersView;
    private final EditText codeEditText;
    private final TextView errorView;

    public CodeEditorView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setBackground(createBackground(EDITOR_BACKGROUND, Color.parseColor("#3B3150"), dp(12)));
        setPadding(dp(8), dp(8), dp(8), dp(8));

        LinearLayout editorRow = new LinearLayout(context);
        editorRow.setOrientation(HORIZONTAL);
        editorRow.setGravity(Gravity.TOP);
        editorRow.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        lineNumbersView = new TextView(context);
        lineNumbersView.setTextColor(EDITOR_LINE_NUMBER);
        lineNumbersView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        lineNumbersView.setTypeface(Typeface.MONOSPACE);
        lineNumbersView.setGravity(Gravity.RIGHT | Gravity.TOP);
        lineNumbersView.setPadding(0, dp(10), dp(8), dp(10));
        lineNumbersView.setIncludeFontPadding(false);
        editorRow.addView(lineNumbersView, new LayoutParams(dp(44), ViewGroup.LayoutParams.MATCH_PARENT));

        codeEditText = new EditText(context);
        applyEditorColors();

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
        codeEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_NONE);
        codeEditText.setBackgroundColor(Color.TRANSPARENT);
        codeEditText.setPadding(dp(8), dp(6), dp(8), dp(8));
        codeEditText.setHint("// Напишите решение на C#");
        codeEditText.setIncludeFontPadding(false);
        codeEditText.setCursorVisible(true);
        codeEditText.setSelectAllOnFocus(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            codeEditText.getTextCursorDrawable();
            codeEditText.setTextCursorDrawable(null);
        }

        editorRow.addView(codeEditText, new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        addView(editorRow);

        errorView = new TextView(context);
        errorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        errorView.setPadding(dp(8), dp(8), dp(8), dp(2));
        addView(errorView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        codeEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyEditorColors();
                updateLineNumbers();
                validateLexically();
            }

            @Override public void afterTextChanged(Editable s) {
                applyEditorColors();
            }
        });

        updateLineNumbers();
        validateLexically();
    }

    private void applyEditorColors() {
        codeEditText.setTextColor(EDITOR_TEXT);
        codeEditText.setHintTextColor(EDITOR_HINT);
        codeEditText.setHighlightColor(EDITOR_SELECTION);
        codeEditText.setLinkTextColor(EDITOR_TEXT);
    }

    public EditText getEditText() {
        applyEditorColors();
        return codeEditText;
    }

    public String getCode() {
        Editable editable = codeEditText.getText();
        return editable == null ? "" : editable.toString();
    }

    public void setCode(String code) {
        codeEditText.setTextColor(EDITOR_TEXT);
        codeEditText.setText(code == null ? "" : code);
        codeEditText.setTextColor(EDITOR_TEXT);
        codeEditText.setSelection(codeEditText.getText().length());
        updateLineNumbers();
        validateLexically();
    }

    public void insertAtCursor(String value) {
        if (value == null) return;
        int start = Math.max(codeEditText.getSelectionStart(), 0);
        int end = Math.max(codeEditText.getSelectionEnd(), 0);
        int min = Math.min(start, end);
        int max = Math.max(start, end);
        codeEditText.getText().replace(min, max, value);
        applyEditorColors();
        codeEditText.requestFocus();
    }

    public boolean validateLexically() {
        String error = findLexicalProblem(getCode());
        if (error == null) {
            errorView.setText("Ошибок не найдено");
            errorView.setTextColor(Color.parseColor("#A5D6A7"));
            return true;
        } else {
            errorView.setText(error);
            errorView.setTextColor(Color.parseColor("#FFB4AB"));
            return false;
        }
    }

    private void updateLineNumbers() {
        String code = getCode();
        int lines = 1;
        for (int i = 0; i < code.length(); i++) {
            if (code.charAt(i) == '\n') lines++;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= lines; i++) {
            if (i > 1) builder.append('\n');
            builder.append(i);
        }
        lineNumbersView.setText(builder.toString());
    }

    private String findLexicalProblem(String code) {
        if (code == null || code.trim().isEmpty()) return "Введите код решения";

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

            if (inLineComment) continue;

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
                if (escaped) escaped = false;
                else if (c == '\\') escaped = true;
                else if (c == '"') inString = false;
                continue;
            }

            if (inChar) {
                if (escaped) escaped = false;
                else if (c == '\\') escaped = true;
                else if (c == '\'') inChar = false;
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
                if (stack.isEmpty()) return "Лексическая ошибка, строка " + line + ": лишний символ '" + c + "'";
                char open = stack.pop();
                int openLine = lineStack.pop();
                if (!matches(open, c)) {
                    return "Лексическая ошибка, строка " + line + ": '" + c + "' не соответствует '" + open + "' на строке " + openLine;
                }
            }
        }

        if (inString) return "Лексическая ошибка: строковый литерал не закрыт кавычкой";
        if (inChar) return "Лексическая ошибка: символьный литерал не закрыт кавычкой";
        if (inBlockComment) return "Лексическая ошибка: блочный комментарий /* */ не закрыт";
        if (!stack.isEmpty()) return "Лексическая ошибка, строка " + lineStack.pop() + ": не закрыт символ '" + stack.pop() + "'";
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
