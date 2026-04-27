package com.example.c.ui.test;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.c.R;
import com.example.c.data.db.entity.TopicProgressEntity;
import com.example.c.data.model.test.TestModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestListAdapter extends RecyclerView.Adapter<TestListAdapter.TestViewHolder> {

    public interface OnTestClickListener {
        void onTestClick(TestModel test);
    }

    private final Context context;
    private final OnTestClickListener listener;
    private final List<TestModel> tests = new ArrayList<>();
    private Map<String, TopicProgressEntity> progressMap = new HashMap<>();

    public TestListAdapter(Context context, OnTestClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public TestListAdapter(List<TestModel> tests, OnTestClickListener listener) {
        this.context = null;
        this.listener = listener;
        setTests(tests);
    }

    public void setTests(List<TestModel> newTests) {
        tests.clear();
        if (newTests != null) {
            tests.addAll(newTests);
        }
        notifyDataSetChanged();
    }

    public void submitList(List<TestModel> newTests) {
        setTests(newTests);
    }

    public void setProgressList(List<TopicProgressEntity> progressList) {
        Map<String, TopicProgressEntity> map = new HashMap<>();
        if (progressList != null) {
            for (TopicProgressEntity item : progressList) {
                if (item != null && item.topicId != null) {
                    map.put(item.topicId, item);
                }
            }
        }
        progressMap = map;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context ctx = parent.getContext();

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(dp(ctx, 16), dp(ctx, 14), dp(ctx, 16), dp(ctx, 14));
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(dp(ctx, 12), dp(ctx, 8), dp(ctx, 12), dp(ctx, 8));
        root.setLayoutParams(params);

        LinearLayout textBox = new LinearLayout(ctx);
        textBox.setOrientation(LinearLayout.VERTICAL);
        textBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(ctx);
        title.setTextSize(17);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor("#202124"));
        textBox.addView(title);

        TextView description = new TextView(ctx);
        description.setTextSize(14);
        description.setTextColor(Color.parseColor("#666666"));
        description.setPadding(0, dp(ctx, 4), 0, 0);
        textBox.addView(description);

        TextView status = new TextView(ctx);
        status.setTextSize(14);
        status.setTypeface(Typeface.DEFAULT_BOLD);
        status.setPadding(0, dp(ctx, 6), 0, 0);
        textBox.addView(status);

        ImageView icon = new ImageView(ctx);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(ctx, 28), dp(ctx, 28));
        iconParams.leftMargin = dp(ctx, 12);
        icon.setLayoutParams(iconParams);

        root.addView(textBox);
        root.addView(icon);

        return new TestViewHolder(root, title, description, status, icon);
    }

    @Override
    public void onBindViewHolder(@NonNull TestViewHolder holder, int position) {
        final TestModel test = tests.get(position);
        holder.title.setText(safe(test.title, "Тест"));
        holder.description.setText(safe(test.description, ""));
        holder.description.setVisibility(isBlank(test.description) ? View.GONE : View.VISIBLE);

        boolean unlocked = isUnlocked(test);
        if (unlocked) {
            holder.itemView.setAlpha(1f);
            holder.itemView.setBackground(createBackground(Color.WHITE, Color.parseColor("#E2D9F6")));
            holder.icon.setImageResource(R.drawable.ic_visibility_24);
            holder.status.setText("Доступен");
            holder.status.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            holder.itemView.setAlpha(0.72f);
            holder.itemView.setBackground(createBackground(Color.parseColor("#F2F0F6"), Color.parseColor("#DDD6EA")));
            holder.icon.setImageResource(R.drawable.ic_lock_24);
            holder.status.setText("Закрыт: сначала прочитайте теорию");
            holder.status.setTextColor(Color.parseColor("#8A6D3B"));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTestClick(test);
                }
            }
        });
    }

    private boolean isUnlocked(TestModel test) {
        if (test == null) {
            return false;
        }
        if (isBlank(test.topicId)) {
            // Если у теста нет привязки к теме, не блокируем его, чтобы не сломать старые данные.
            return true;
        }
        TopicProgressEntity progress = progressMap.get(test.topicId);
        return progress != null && progress.isActuallyRead();
    }

    @Override
    public int getItemCount() {
        return tests.size();
    }

    static class TestViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        TextView status;
        ImageView icon;

        TestViewHolder(@NonNull View itemView, TextView title, TextView description, TextView status, ImageView icon) {
            super(itemView);
            this.title = title;
            this.description = description;
            this.status = status;
            this.icon = icon;
        }
    }

    private static GradientDrawable createBackground(int color, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(24f);
        drawable.setStroke(1, strokeColor);
        return drawable;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String safe(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }
}
