package com.example.c.ui.theory;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;

import com.example.c.data.db.entity.TopicProgressEntity;
import com.example.c.data.model.theory.TheoryBlock;
import com.example.c.data.model.theory.TheoryTopic;
import com.example.c.viewmodel.TheoryViewModel;

import java.util.List;

public class TheoryTopicActivity extends AppCompatActivity {

    public static final String EXTRA_TOPIC_ID = "topic_id";

    private TheoryViewModel viewModel;
    private TheoryTopic topic;
    private String topicId;

    private NestedScrollView scrollView;
    private LinearLayout contentContainer;
    private TextView emptyView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TheoryViewModel.class);
        topicId = getIntent() == null ? null : getIntent().getStringExtra(EXTRA_TOPIC_ID);
        if (isBlank(topicId) && getIntent() != null) {
            topicId = getIntent().getStringExtra("TOPIC_ID");
        }
        if (isBlank(topicId) && getIntent() != null) {
            topicId = getIntent().getStringExtra("id");
        }

        topic = viewModel.getTopicById(topicId);

        buildLayout();
        renderTopic();
        restoreScrollPosition();
    }

    private void buildLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#F8F6FC"));

        Toolbar toolbar = new Toolbar(this);
        toolbar.setTitle(topic == null ? "Теория" : safe(topic.title, "Теория"));
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setBackgroundColor(Color.parseColor("#6F50B5"));
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        root.addView(toolbar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
        ));

        scrollView = new NestedScrollView(this);
        scrollView.setFillViewport(true);
        contentContainer = new LinearLayout(this);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setPadding(dp(18), dp(18), dp(18), dp(28));
        scrollView.addView(contentContainer, new NestedScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        emptyView = new TextView(this);
        emptyView.setGravity(android.view.Gravity.CENTER);
        emptyView.setTextSize(16);
        emptyView.setTextColor(Color.parseColor("#666666"));
        emptyView.setVisibility(View.GONE);

        root.addView(scrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));
        root.addView(emptyView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        setContentView(root);
    }

    private void renderTopic() {
        contentContainer.removeAllViews();

        if (topic == null) {
            scrollView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("Тема не найдена");
            return;
        }

        TextView title = new TextView(this);
        title.setText(safe(topic.title, "Тема"));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor("#202124"));
        title.setPadding(0, 0, 0, dp(10));
        contentContainer.addView(title);

        if (!isBlank(topic.description)) {
            TextView description = new TextView(this);
            description.setText(topic.description);
            description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            description.setTextColor(Color.parseColor("#666666"));
            description.setLineSpacing(0, 1.15f);
            description.setPadding(0, 0, 0, dp(16));
            contentContainer.addView(description);
        }

        if (topic.blocks != null && !topic.blocks.isEmpty()) {
            for (TheoryBlock block : topic.blocks) {
                addBlock(block);
            }
        }
    }

    private void addBlock(TheoryBlock block) {
        if (block == null) {
            return;
        }

        String type = block.type == null ? "text" : block.type.trim().toLowerCase();
        String text = block.text == null ? "" : block.text;
        if (isBlank(text) && block.code != null) {
            text = block.code;
        }
        if (isBlank(text)) {
            return;
        }

        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(Color.parseColor("#242424"));
        view.setLineSpacing(0, 1.16f);
        view.setTextIsSelectable(true);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        if (type.contains("heading") || type.contains("title") || type.contains("header")) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21);
            view.setTypeface(Typeface.DEFAULT_BOLD);
            params.topMargin = dp(18);
            params.bottomMargin = dp(8);
        } else if (type.contains("code")) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            view.setTypeface(Typeface.MONOSPACE);
            view.setTextColor(Color.parseColor("#1B1B1B"));
            view.setBackgroundColor(Color.parseColor("#EFEAF8"));
            view.setPadding(dp(12), dp(10), dp(12), dp(10));
            params.topMargin = dp(8);
            params.bottomMargin = dp(12);
        } else {
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            params.bottomMargin = dp(12);
        }

        view.setLayoutParams(params);
        contentContainer.addView(view);
    }

    private void restoreScrollPosition() {
        if (isBlank(topicId)) {
            return;
        }

        final TopicProgressEntity progress = viewModel.getProgress(topicId);
        if (progress != null && progress.scrollY > 0 && !progress.isActuallyRead()) {
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(0, progress.scrollY);
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveReadingProgress();
    }

    @Override
    protected void onDestroy() {
        saveReadingProgress();
        super.onDestroy();
    }

    private void saveReadingProgress() {
        if (topic == null || isBlank(topicId) || scrollView == null) {
            return;
        }
        int percent = calculateReadPercent();
        int scrollY = scrollView.getScrollY();
        viewModel.saveProgress(topicId, percent, scrollY);
    }

    private int calculateReadPercent() {
        if (scrollView == null || scrollView.getChildCount() == 0) {
            return 0;
        }
        View child = scrollView.getChildAt(0);
        int contentHeight = child.getHeight();
        int visibleHeight = scrollView.getHeight();
        int maxScroll = Math.max(0, contentHeight - visibleHeight);
        if (maxScroll <= 0) {
            return 100;
        }
        int scrollY = Math.max(0, scrollView.getScrollY());
        int percent = Math.round((scrollY * 100f) / maxScroll);
        if (percent >= 95) {
            return 100;
        }
        return Math.max(0, Math.min(100, percent));
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }
}
