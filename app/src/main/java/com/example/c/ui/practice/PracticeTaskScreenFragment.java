package com.example.c.ui.practice;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.c.data.model.practice.PracticeTask;
import com.example.c.viewmodel.PracticeViewModel;

public class PracticeTaskScreenFragment extends Fragment {

    private static final String ARG_TASK_ID = "task_id";

    private PracticeViewModel viewModel;
    private PracticeTask task;

    private LinearLayout rootLayout;
    private LinearLayout contentContainer;
    private LinearLayout infoContainer;
    private LinearLayout editorContainer;
    private LinearLayout symbolsPanel;
    private CodeEditorView codeEditorView;
    private TextView titleView;
    private TextView fullscreenButton;
    private Button runButton;

    private boolean fullscreenEditor = false;
    private int oldSoftInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED;

    public static PracticeTaskScreenFragment newInstance(String taskId) {
        PracticeTaskScreenFragment fragment = new PracticeTaskScreenFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TASK_ID, taskId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootLayout = new LinearLayout(requireContext());
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Color.parseColor("#F7F5FB"));
        rootLayout.setClickable(true);
        rootLayout.setFocusable(true);

        rootLayout.addView(createToolbar());

        NestedScrollView scrollView = new NestedScrollView(requireContext());
        scrollView.setFillViewport(true);

        contentContainer = new LinearLayout(requireContext());
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setPadding(dp(16), dp(14), dp(16), dp(16));

        scrollView.addView(contentContainer, new NestedScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        rootLayout.addView(scrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        return rootLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        oldSoftInputMode = requireActivity().getWindow().getAttributes().softInputMode;
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        viewModel = new ViewModelProvider(this).get(PracticeViewModel.class);

        String taskId = getArguments() == null ? null : getArguments().getString(ARG_TASK_ID);
        task = viewModel.getTaskById(taskId);

        if (task == null) {
            Toast.makeText(requireContext(), "Задание не найдено", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        renderTask();
    }

    @Override
    public void onDestroyView() {
        try {
            requireActivity().getWindow().setSoftInputMode(oldSoftInputMode);
        } catch (Exception ignored) {
        }
        super.onDestroyView();
    }

    private View createToolbar() {
        LinearLayout toolbar = new LinearLayout(requireContext());
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(10), dp(8), dp(12), dp(8));
        toolbar.setBackgroundColor(Color.parseColor("#6F52B5"));

        TextView backButton = new TextView(requireContext());
        backButton.setText("‹");
        backButton.setTextColor(Color.WHITE);
        backButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);
        backButton.setGravity(Gravity.CENTER);
        toolbar.addView(backButton, new LinearLayout.LayoutParams(dp(42), dp(46)));
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        titleView = new TextView(requireContext());
        titleView.setText("Практическое задание");
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setSingleLine(true);
        titleView.setEllipsize(TextUtils.TruncateAt.END);
        toolbar.addView(titleView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        return toolbar;
    }

    private void renderTask() {
        titleView.setText(task.getTitle());
        contentContainer.removeAllViews();

        infoContainer = new LinearLayout(requireContext());
        infoContainer.setOrientation(LinearLayout.VERTICAL);

        addInfoSection("Описание", task.getDescription());
        addInfoSection("Задача", task.getTaskText());
        addInfoSection("Требования", task.getRequirementsText());
        addInfoSection("Пример входных данных", task.getInputExample());
        addInfoSection("Пример выходных данных", task.getOutputExample());
        addInfoSection("Подсказка", task.getHint());

        Button solutionButton = new Button(requireContext());
        solutionButton.setText("Показать пример решения");
        solutionButton.setAllCaps(false);
        solutionButton.setVisibility(isBlank(task.getSolutionExample()) ? View.GONE : View.VISIBLE);

        final LinearLayout solutionBox = new LinearLayout(requireContext());
        solutionBox.setOrientation(LinearLayout.VERTICAL);
        solutionBox.setVisibility(View.GONE);
        solutionBox.addView(createLabel("Пример решения"));
        solutionBox.addView(createCodeText(task.getSolutionExample()));

        solutionButton.setOnClickListener(new View.OnClickListener() {
            private boolean visible = false;

            @Override
            public void onClick(View v) {
                visible = !visible;
                solutionBox.setVisibility(visible ? View.VISIBLE : View.GONE);
                ((Button) v).setText(visible ? "Скрыть пример решения" : "Показать пример решения");
            }
        });

        infoContainer.addView(solutionButton);
        infoContainer.addView(solutionBox);

        contentContainer.addView(infoContainer);

        editorContainer = new LinearLayout(requireContext());
        editorContainer.setOrientation(LinearLayout.VERTICAL);
        editorContainer.setPadding(0, dp(12), 0, 0);

        LinearLayout editorHeader = new LinearLayout(requireContext());
        editorHeader.setGravity(Gravity.CENTER_VERTICAL);
        editorHeader.setOrientation(LinearLayout.HORIZONTAL);

        TextView editorTitle = createLabel("Код решения");
        editorTitle.setPadding(0, 0, 0, 0);
        editorHeader.addView(editorTitle, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        fullscreenButton = new TextView(requireContext());
        fullscreenButton.setText("⛶ Развернуть");
        fullscreenButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        fullscreenButton.setTypeface(Typeface.DEFAULT_BOLD);
        fullscreenButton.setTextColor(Color.parseColor("#5E35B1"));
        fullscreenButton.setGravity(Gravity.CENTER);
        fullscreenButton.setPadding(dp(8), dp(8), dp(8), dp(8));
        editorHeader.addView(fullscreenButton);
        fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFullscreenEditor();
            }
        });

        editorContainer.addView(editorHeader);
        symbolsPanel = createSymbolsPanel();
        editorContainer.addView(symbolsPanel);

        codeEditorView = new CodeEditorView(requireContext());
        codeEditorView.setCode(getStarterCode());
        LinearLayout.LayoutParams editorParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(310)
        );
        editorParams.topMargin = dp(8);
        editorContainer.addView(codeEditorView, editorParams);

        runButton = new Button(requireContext());
        runButton.setText("Запустить");
        runButton.setAllCaps(false);
        LinearLayout.LayoutParams runParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52)
        );
        runParams.topMargin = dp(14);
        editorContainer.addView(runButton, runParams);

        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                codeEditorView.validateLexically();
                Toast.makeText(requireContext(), "Запуск и проверка решения будут добавлены позже", Toast.LENGTH_SHORT).show();
            }
        });

        contentContainer.addView(editorContainer);
    }

    private LinearLayout createSymbolsPanel() {
        HorizontalScrollView scroll = new HorizontalScrollView(requireContext());
        scroll.setHorizontalScrollBarEnabled(false);

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(6), 0, dp(4));

        String[] symbols = {
                "{", "}", "(", ")", "[", "]", ";", "\"", "'", ".", ",",
                "+", "-", "*", "/", "%", "=", "==", "!=", ">", "<", ">=", "<=",
                "&&", "||", "!", "//", "\\n", "Console.WriteLine();", "if ()", "for ()", "while ()"
        };

        for (final String symbol : symbols) {
            TextView button = new TextView(requireContext());
            button.setText(symbol);
            button.setTextColor(Color.parseColor("#2B2140"));
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            button.setTypeface(Typeface.DEFAULT_BOLD);
            button.setGravity(Gravity.CENTER);
            button.setPadding(dp(12), dp(8), dp(12), dp(8));
            button.setBackground(createBackground(Color.WHITE, Color.parseColor("#D9CFF0"), dp(12)));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.rightMargin = dp(8);
            row.addView(button, params);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    insertSymbol(symbol);
                }
            });
        }

        scroll.addView(row);
        LinearLayout wrapper = new LinearLayout(requireContext());
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return wrapper;
    }

    private void insertSymbol(String symbol) {
        if (codeEditorView == null) {
            return;
        }

        if ("\\n".equals(symbol)) {
            codeEditorView.insertAtCursor("\n");
            return;
        }

        if ("Console.WriteLine();".equals(symbol)) {
            codeEditorView.insertAtCursor("Console.WriteLine();");
            return;
        }

        if ("if ()".equals(symbol)) {
            codeEditorView.insertAtCursor("if ()\n{\n    \n}");
            return;
        }

        if ("for ()".equals(symbol)) {
            codeEditorView.insertAtCursor("for (int i = 0; i < ; i++)\n{\n    \n}");
            return;
        }

        if ("while ()".equals(symbol)) {
            codeEditorView.insertAtCursor("while ()\n{\n    \n}");
            return;
        }

        codeEditorView.insertAtCursor(symbol);
    }

    private void toggleFullscreenEditor() {
        fullscreenEditor = !fullscreenEditor;

        if (fullscreenEditor) {
            infoContainer.setVisibility(View.GONE);
            fullscreenButton.setText("▣ Свернуть");

            ViewGroup.LayoutParams params = codeEditorView.getLayoutParams();
            params.height = getResources().getDisplayMetrics().heightPixels - dp(230);
            codeEditorView.setLayoutParams(params);

            runButton.setVisibility(View.VISIBLE);
        } else {
            infoContainer.setVisibility(View.VISIBLE);
            fullscreenButton.setText("⛶ Развернуть");

            ViewGroup.LayoutParams params = codeEditorView.getLayoutParams();
            params.height = dp(310);
            codeEditorView.setLayoutParams(params);
        }
    }

    private void addInfoSection(String label, String value) {
        if (isBlank(value)) {
            return;
        }

        infoContainer.addView(createLabel(label));

        if (label.toLowerCase().contains("данных")) {
            infoContainer.addView(createCodeText(value));
        } else {
            TextView text = new TextView(requireContext());
            text.setText(value);
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            text.setTextColor(Color.parseColor("#333333"));
            text.setLineSpacing(dp(2), 1.06f);
            text.setPadding(0, 0, 0, dp(10));
            infoContainer.addView(text);
        }
    }

    private TextView createLabel(String text) {
        TextView label = new TextView(requireContext());
        label.setText(text);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        label.setTextColor(Color.parseColor("#5E35B1"));
        label.setPadding(0, dp(10), 0, dp(5));
        return label;
    }

    private TextView createCodeText(String value) {
        TextView text = new TextView(requireContext());
        text.setText(value);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        text.setTypeface(Typeface.MONOSPACE);
        text.setTextColor(Color.parseColor("#202124"));
        text.setPadding(dp(12), dp(10), dp(12), dp(10));
        text.setBackground(createBackground(Color.parseColor("#F5F3FA"), Color.parseColor("#DED7EA"), dp(10)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(10);
        text.setLayoutParams(params);
        return text;
    }

    private String getStarterCode() {
        return "using System;\n\nclass Program\n{\n    static void Main()\n    {\n        // Напишите решение здесь\n    }\n}";
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
