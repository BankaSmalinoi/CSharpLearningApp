package com.example.c.ui.theory;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.c.R;
import com.example.c.data.db.entity.TopicProgressEntity;
import com.example.c.data.model.theory.TheoryTopic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheoryTopicAdapter extends RecyclerView.Adapter<TheoryTopicAdapter.TopicViewHolder> {

    public interface OnTopicClickListener {
        void onTopicClick(TheoryTopic topic);
    }

    private final Context context;
    private final OnTopicClickListener listener;
    private final List<TheoryTopic> topics = new ArrayList<>();
    private Map<String, TopicProgressEntity> progressMap = new HashMap<>();

    public TheoryTopicAdapter(Context context, OnTopicClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public TheoryTopicAdapter(OnTopicClickListener listener) {
        this.context = null;
        this.listener = listener;
    }

    public void setTopics(List<TheoryTopic> newTopics) {
        topics.clear();
        if (newTopics != null) {
            topics.addAll(newTopics);
        }
        notifyDataSetChanged();
    }

    public void submitList(List<TheoryTopic> newTopics) {
        setTopics(newTopics);
    }

    public void setProgressMap(Map<String, TopicProgressEntity> progressMap) {
        this.progressMap = progressMap == null ? new HashMap<String, TopicProgressEntity>() : progressMap;
        notifyDataSetChanged();
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
        setProgressMap(map);
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
        LinearLayout.LayoutParams textBoxParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textBox.setLayoutParams(textBoxParams);

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

        TextView percent = new TextView(ctx);
        percent.setTextSize(14);
        percent.setTypeface(Typeface.DEFAULT_BOLD);
        percent.setTextColor(Color.parseColor("#6D4CB3"));
        percent.setPadding(0, dp(ctx, 6), 0, 0);
        textBox.addView(percent);

        ImageView icon = new ImageView(ctx);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(ctx, 28), dp(ctx, 28));
        iconParams.leftMargin = dp(ctx, 12);
        icon.setLayoutParams(iconParams);

        root.addView(textBox);
        root.addView(icon);

        return new TopicViewHolder(root, title, description, percent, icon);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        final TheoryTopic topic = topics.get(position);
        holder.title.setText(safe(topic.title, "Тема"));
        holder.description.setText(safe(topic.description, ""));
        holder.description.setVisibility(isBlank(topic.description) ? View.GONE : View.VISIBLE);

        TopicProgressEntity progress = topic.id == null ? null : progressMap.get(topic.id);
        int percentValue = progress == null ? 0 : Math.max(0, Math.min(100, progress.readPercent));
        boolean isRead = progress != null && progress.isActuallyRead();

        if (isRead) {
            holder.itemView.setBackground(createBackground(Color.parseColor("#DDF2E1"), Color.parseColor("#B8E0C0")));
            holder.icon.setImageResource(R.drawable.ic_visibility_24);
            holder.percent.setVisibility(View.GONE);
        } else if (percentValue > 0) {
            holder.itemView.setBackground(createBackground(Color.WHITE, Color.parseColor("#E2D9F6")));
            holder.icon.setImageResource(R.drawable.ic_visibility_off_24);
            holder.percent.setVisibility(View.VISIBLE);
            holder.percent.setText("Прочитано: " + percentValue + "%");
        } else {
            holder.itemView.setBackground(createBackground(Color.WHITE, Color.parseColor("#E2D9F6")));
            holder.icon.setImageResource(R.drawable.ic_visibility_off_24);
            holder.percent.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTopicClick(topic);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    static class TopicViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        TextView percent;
        ImageView icon;

        TopicViewHolder(@NonNull View itemView, TextView title, TextView description, TextView percent, ImageView icon) {
            super(itemView);
            this.title = title;
            this.description = description;
            this.percent = percent;
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
