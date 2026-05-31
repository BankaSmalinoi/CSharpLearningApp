package com.example.c.ui.statistics;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.c.data.statistics.StatisticsStore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StatisticsFragment extends Fragment {
    private LinearLayout content;
    private TextView selectedDayInfo;
    private MonthActivityChartView chartView;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(requireContext());
        content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(16), dp(16), dp(24));
        scrollView.addView(content);
        buildUi();
        return scrollView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void buildUi() {
        TextView title = title("Статистика");
        content.addView(title);

        selectedDayInfo = normal("Выберите день в календаре, чтобы увидеть активность за дату.");
        chartView = new MonthActivityChartView(requireContext());
        chartView.setPadding(0, dp(10), 0, dp(10));

        refresh();
    }

    private void refresh() {
        if (content == null) return;
        content.removeAllViews();
        content.addView(title("Статистика"));

        StatisticsStore store = StatisticsStore.getInstance(requireContext());
        StatisticsStore.Snapshot s = store.getSnapshot();

        addCard("Прочитано теории", s.readTheoryCount + " тем", s.readTheoryPercent + "% прохождения теории");
        addCard("Решено задач", s.solvedPracticeCount + " задач", s.solvedPracticePercent + "% прохождения задач");
        addCard("Решено тестов", s.solvedTestsCount + " тестов", s.solvedTestsPercent + "% прохождения тестов");
        addCard("Дни заходов", s.openDaysCount + " дней", "Количество дней, когда приложение открывалось");

        content.addView(section("Календарь активности"));
        CalendarView calendarView = new CalendarView(requireContext());
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            showDayInfo(date);
        });
        content.addView(calendarView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        selectedDayInfo = normal("");
        content.addView(selectedDayInfo);
        showDayInfo(dateFormat.format(new Date()));

        content.addView(section("Активности за текущий месяц"));
        Calendar c = Calendar.getInstance();
        chartView = new MonthActivityChartView(requireContext());
        chartView.setData(store.getMonthActivities(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1));
        content.addView(chartView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(240)));

        content.addView(section("История решений задач"));
        if (s.taskHistory.isEmpty()) {
            content.addView(normal("Пока нет решенных задач."));
        } else {
            int count = 0;
            for (StatisticsStore.HistoryItem item : s.taskHistory) {
                content.addView(normal(item.date + " • " + item.id + " • " + item.percent + "%"));
                if (++count >= 10) break;
            }
        }

        content.addView(section("История решений тестов"));
        if (s.testHistory.isEmpty()) {
            content.addView(normal("Пока нет решенных тестов."));
        } else {
            int count = 0;
            for (StatisticsStore.HistoryItem item : s.testHistory) {
                content.addView(normal(item.date + " • " + item.id + " • " + item.percent + "%"));
                if (++count >= 10) break;
            }
        }
    }

    private void showDayInfo(String date) {
        StatisticsStore.DayInfo d = StatisticsStore.getInstance(requireContext()).getDayInfo(date);
        selectedDayInfo.setText(
                "Активность за " + date + ":\n" +
                        "• прочитано теории: " + d.theory + "\n" +
                        "• решено задач: " + d.practice + "\n" +
                        "• решено тестов: " + d.tests + "\n" +
                        "• открытий приложения: " + d.opens
        );
    }

    private void addCard(String header, String value, String subtitle) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(8), 0, dp(8));
        card.setLayoutParams(lp);
        card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        card.addView(section(header));
        card.addView(value(value));
        card.addView(normal(subtitle));
        content.addView(card);
    }

    private TextView title(String text) {
        TextView v = new TextView(requireContext());
        v.setText(text);
        v.setTextSize(24);
        v.setGravity(Gravity.START);
        v.setPadding(0, 0, 0, dp(10));
        return v;
    }

    private TextView section(String text) {
        TextView v = new TextView(requireContext());
        v.setText(text);
        v.setTextSize(18);
        v.setPadding(0, dp(12), 0, dp(4));
        return v;
    }

    private TextView value(String text) {
        TextView v = new TextView(requireContext());
        v.setText(text);
        v.setTextSize(22);
        v.setPadding(0, dp(2), 0, dp(2));
        return v;
    }

    private TextView normal(String text) {
        TextView v = new TextView(requireContext());
        v.setText(text);
        v.setTextSize(15);
        v.setPadding(0, dp(4), 0, dp(4));
        return v;
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }
}
