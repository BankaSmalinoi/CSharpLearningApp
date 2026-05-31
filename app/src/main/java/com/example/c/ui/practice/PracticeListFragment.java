package com.example.c.ui.practice;

import android.content.Intent;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PracticeListFragment extends Fragment {

    private LinearLayout contentContainer;
    private TextView emptyView;
    private PracticeResultStorage resultStorage;
    private List<PracticeTopicData> topicGroups;
    private Set<String> solvedTaskIds = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FrameLayout root = new FrameLayout(requireContext());
        root.setBackgroundColor(Color.parseColor("#F7F5FB"));

        NestedScrollView scrollView = new NestedScrollView(requireContext());
        scrollView.setFillViewport(true);
        scrollView.setClipToPadding(false);
        scrollView.setPadding(0, dp(8), 0, dp(20));

        contentContainer = new LinearLayout(requireContext());
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setPadding(dp(14), dp(8), dp(14), dp(24));
        scrollView.addView(contentContainer, new NestedScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        emptyView = new TextView(requireContext());
        emptyView.setText("Практические задания не найдены");
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        emptyView.setTextColor(Color.parseColor("#666666"));
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setVisibility(View.GONE);

        root.addView(scrollView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(emptyView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        resultStorage = new PracticeResultStorage(requireContext());
        topicGroups = PracticeDataLoader.loadGrouped(requireContext());
        renderPracticeList();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (resultStorage != null) {
            renderPracticeList();
        }
    }

    private void renderPracticeList() {
        if (contentContainer == null) return;
        contentContainer.removeAllViews();
        solvedTaskIds = resultStorage == null ? new HashSet<String>() : resultStorage.getSolvedTaskIds();

        if (topicGroups == null || topicGroups.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            return;
        }
        emptyView.setVisibility(View.GONE);

        for (PracticeTopicData topic : topicGroups) {
            addTopicSection(topic);
        }
    }

    private void addTopicSection(PracticeTopicData topic) {
        final boolean topicRead = TheoryReadStateStorage.isTheoryRead(requireContext(), topic.id);

        LinearLayout section = new LinearLayout(requireContext());
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(0, 0, 0, dp(8));
        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sectionParams.bottomMargin = dp(16);
        section.setLayoutParams(sectionParams);

        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(14), dp(16), dp(14));
        header.setBackground(createBackground(topicRead ? Color.parseColor("#DDF2E1") : Color.WHITE, Color.parseColor("#DED7EA"), dp(16)));

        TextView headerTexts = new TextView(requireContext());
        headerTexts.setText(topic.title + "\n" + topic.tasks.size() + " практическ" + getTaskWordEnding(topic.tasks.size()) + " задан" + getAssignmentWordEnding(topic.tasks.size()));
        headerTexts.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        headerTexts.setTypeface(Typeface.DEFAULT_BOLD);
        headerTexts.setTextColor(Color.parseColor("#202124"));
        headerTexts.setLineSpacing(dp(2), 1f);
        headerTexts.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView statusIcon = new TextView(requireContext());
        statusIcon.setText(topicRead ? "✓" : "🔒");
        statusIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        statusIcon.setGravity(Gravity.CENTER);
        statusIcon.setTextColor(topicRead ? Color.parseColor("#2E7D32") : Color.parseColor("#6D5B88"));
        statusIcon.setLayoutParams(new LinearLayout.LayoutParams(dp(34), dp(34)));

        header.addView(headerTexts);
        header.addView(statusIcon);
        section.addView(header);

        for (PracticeTaskData task : topic.tasks) {
            section.addView(createTaskCard(task, topicRead));
        }
        contentContainer.addView(section);
    }

    private View createTaskCard(final PracticeTaskData task, final boolean topicRead) {
        final boolean solved = solvedTaskIds.contains(task.id);
        final boolean unlocked = !task.requiresTheoryRead || topicRead;

        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));

        int backgroundColor;
        int strokeColor;
        if (solved) {
            backgroundColor = Color.parseColor("#DDF2E1");
            strokeColor = Color.parseColor("#B5E1BE");
        } else if (unlocked) {
            backgroundColor = Color.WHITE;
            strokeColor = Color.parseColor("#E2D9F6");
        } else {
            backgroundColor = Color.parseColor("#F1EEF7");
            strokeColor = Color.parseColor("#DDD6EA");
        }
        card.setBackground(createBackground(backgroundColor, strokeColor, dp(14)));
        card.setAlpha(unlocked ? 1f : 0.72f);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.leftMargin = dp(8);
        cardParams.rightMargin = dp(8);
        cardParams.topMargin = dp(10);
        card.setLayoutParams(cardParams);

        LinearLayout textBox = new LinearLayout(requireContext());
        textBox.setOrientation(LinearLayout.VERTICAL);
        textBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(requireContext());
        title.setText(task.getTitle());
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor("#202124"));
        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);
        textBox.addView(title);

        TextView description = new TextView(requireContext());
        description.setText(task.getDescription());
        description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        description.setTextColor(Color.parseColor("#666666"));
        description.setMaxLines(3);
        description.setEllipsize(TextUtils.TruncateAt.END);
        description.setPadding(0, dp(5), 0, 0);
        description.setVisibility(isBlank(task.getDescription()) ? View.GONE : View.VISIBLE);
        textBox.addView(description);

        TextView status = new TextView(requireContext());
        status.setText(getTaskStatusText(task, unlocked, solved));
        status.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        status.setTypeface(Typeface.DEFAULT_BOLD);
        status.setTextColor(solved ? Color.parseColor("#2E7D32") : (unlocked ? Color.parseColor("#5E35B1") : Color.parseColor("#8A6D3B")));
        status.setPadding(0, dp(7), 0, 0);
        textBox.addView(status);

        TextView icon = new TextView(requireContext());
        icon.setText(solved ? "✓" : (unlocked ? "›" : "🔒"));
        icon.setGravity(Gravity.CENTER);
        icon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        icon.setTextColor(solved ? Color.parseColor("#2E7D32") : Color.parseColor("#6D5B88"));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(36), dp(36));
        iconParams.leftMargin = dp(10);
        icon.setLayoutParams(iconParams);

        card.addView(textBox);
        card.addView(icon);

        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!unlocked) {
                    Toast.makeText(requireContext(), "Сначала прочитайте теорию по этой теме", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(requireContext(), PracticeTaskActivity.class);
                intent.putExtra(PracticeTaskActivity.EXTRA_TASK_ID, task.id);
                startActivity(intent);
            }
        });

        return card;
    }

    private String getTaskStatusText(PracticeTaskData task, boolean unlocked, boolean solved) {
        if (solved) return "Решено";
        if (!unlocked) return "Закрыто: сначала прочитайте теорию";
        String difficulty = task.getDifficulty();
        if (!isBlank(difficulty)) return "Доступно • " + translateDifficulty(difficulty);
        return "Доступно";
    }

    private String translateDifficulty(String difficulty) {
        String value = difficulty == null ? "" : difficulty.trim().toLowerCase();
        if (value.equals("easy")) return "лёгкое";
        if (value.equals("medium")) return "среднее";
        if (value.equals("hard")) return "сложное";
        return difficulty;
    }

    private String getTaskWordEnding(int count) {
        int mod10 = count % 10;
        int mod100 = count % 100;
        if (mod10 == 1 && mod100 != 11) return "ое";
        return "их";
    }

    private String getAssignmentWordEnding(int count) {
        int mod10 = count % 10;
        int mod100 = count % 100;
        if (mod10 == 1 && mod100 != 11) return "ие";
        return "ий";
    }

    private GradientDrawable createBackground(int color, int strokeColor, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
