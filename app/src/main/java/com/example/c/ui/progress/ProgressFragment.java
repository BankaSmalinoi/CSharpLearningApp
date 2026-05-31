package com.example.c.ui.progress;

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

import com.example.c.data.local.entity.DailyActivityEntity;
import com.example.c.data.local.entity.TaskSolutionHistoryEntity;
import com.example.c.data.local.entity.TestSolutionHistoryEntity;
import com.example.c.data.repository.PracticeRepository;
import com.example.c.data.repository.StatisticsRepository;
import com.example.c.data.repository.TestRepository;
import com.example.c.data.repository.TheoryRepository;
import com.example.c.ui.statistics.MonthActivityChartView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProgressFragment extends Fragment {

    private LinearLayout content;
    private TextView selectedDayInfo;
    private final SimpleDateFormat historyDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(requireContext());
        content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(16), dp(16), dp(24));
        scrollView.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        refresh();
        return scrollView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        if (content == null) return;
        content.removeAllViews();

        StatisticsRepository statistics = new StatisticsRepository(requireContext());
        TheoryRepository theoryRepository = new TheoryRepository(requireContext());
        PracticeRepository practiceRepository = new PracticeRepository(requireContext());
        TestRepository testRepository = new TestRepository(requireContext());

        int totalTheory = theoryRepository.getAllTopics().size();
        int totalPractice = practiceRepository.getAllTasks().size();
        int totalTests = testRepository.getAllTests().size();

        int readTheory = statistics.getTotalTheoryReadCount();
        int solvedPractice = statistics.getTotalTasksSolvedCount();
        int solvedTests = statistics.getTotalTestsSolvedCount();

        content.addView(title("Статистика"));

        addCard(
                "Прочитанная теория",
                readTheory + " тем",
                percent(readTheory, totalTheory) + "% из " + totalTheory + " тем"
        );
        addCard(
                "Решенные задачи",
                solvedPractice + " задач",
                percent(solvedPractice, totalPractice) + "% из " + totalPractice + " задач"
        );
        addCard(
                "Прохождение задач",
                statistics.getTaskSuccessPercent() + "% успешных попыток",
                "Успешных попыток: " + statistics.getAcceptedTasksCount() + " из " + statistics.getTaskAttemptsCount()
        );
        addCard(
                "Решенные тесты",
                solvedTests + " тестов",
                percent(solvedTests, totalTests) + "% из " + totalTests + " тестов"
        );
        addCard(
                "Прохождение тестов",
                statistics.getAverageTestPercent() + "% средний результат",
                "Средний процент по истории прохождения тестов"
        );
        addCard(
                "Дни заходов в приложение",
                statistics.getActiveDaysCount() + " дней",
                "Считаются дни, когда приложение было открыто"
        );

        content.addView(section("Календарь активности"));
        CalendarView calendarView = new CalendarView(requireContext());
        content.addView(calendarView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        selectedDayInfo = normal("");
        content.addView(selectedDayInfo);

        Calendar current = Calendar.getInstance();
        String today = String.format(Locale.US, "%04d-%02d-%02d",
                current.get(Calendar.YEAR),
                current.get(Calendar.MONTH) + 1,
                current.get(Calendar.DAY_OF_MONTH));
        showDayInfo(today);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            showDayInfo(date);
        });

        content.addView(section("Активности за текущий месяц"));
        MonthActivityChartView chartView = new MonthActivityChartView(requireContext());
        chartView.setData(buildMonthChartData(statistics, current));
        content.addView(chartView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(240)
        ));

        addTaskHistory(statistics.getTaskHistory());
        addTestHistory(statistics.getTestHistory());
    }

    private void showDayInfo(String date) {
        if (selectedDayInfo == null) return;
        StatisticsRepository statistics = new StatisticsRepository(requireContext());
        DailyActivityEntity activity = statistics.getDayActivity(date);

        int opens = activity == null ? 0 : activity.appOpenCount;
        int theory = activity == null ? 0 : activity.theoryReadCount;
        int tasks = activity == null ? 0 : activity.tasksSolvedCount;
        int tests = activity == null ? 0 : activity.testsSolvedCount;

        selectedDayInfo.setText(
                "Активность за " + date + ":\n" +
                        "• открытий приложения: " + opens + "\n" +
                        "• прочитано теории: " + theory + "\n" +
                        "• решено задач: " + tasks + "\n" +
                        "• решено тестов: " + tests
        );
    }

    private Map<Integer, Integer> buildMonthChartData(StatisticsRepository statistics, Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        Map<Integer, Integer> result = new LinkedHashMap<>();
        for (int day = 1; day <= daysInMonth; day++) {
            result.put(day, 0);
        }

        String prefix = String.format(Locale.US, "%04d-%02d%%", year, month);
        List<DailyActivityEntity> activities = statistics.getActivitiesForMonth(prefix);
        for (DailyActivityEntity activity : activities) {
            if (activity == null || activity.date == null || activity.date.length() < 10) continue;
            try {
                int day = Integer.parseInt(activity.date.substring(8, 10));
                int count = activity.theoryReadCount + activity.tasksSolvedCount + activity.testsSolvedCount;
                result.put(day, count);
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    private void addTaskHistory(List<TaskSolutionHistoryEntity> history) {
        content.addView(section("История решений задач"));
        if (history == null || history.isEmpty()) {
            content.addView(normal("Пока нет решений задач."));
            return;
        }

        int count = 0;
        for (TaskSolutionHistoryEntity item : history) {
            String title = isBlank(item.taskTitle) ? item.taskId : item.taskTitle;
            String status = item.accepted ? "принято" : "не принято";
            content.addView(historyLine(
                    historyDateFormat.format(item.solvedAt) + " • " + title + " • " + status + " • " + item.percent + "%"
            ));
            if (++count >= 15) break;
        }
    }

    private void addTestHistory(List<TestSolutionHistoryEntity> history) {
        content.addView(section("История решений тестов"));
        if (history == null || history.isEmpty()) {
            content.addView(normal("Пока нет пройденных тестов."));
            return;
        }

        int count = 0;
        for (TestSolutionHistoryEntity item : history) {
            String title = isBlank(item.testTitle) ? item.testId : item.testTitle;
            content.addView(historyLine(
                    historyDateFormat.format(item.solvedAt) + " • " + title + " • " +
                            item.correctAnswers + "/" + item.totalQuestions + " • " + item.percent + "%"
            ));
            if (++count >= 15) break;
        }
    }

    private void addCard(String header, String value, String subtitle) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, dp(7), 0, dp(7));
        card.setLayoutParams(lp);

        card.addView(section(header));
        card.addView(value(value));
        card.addView(normal(subtitle));
        content.addView(card);
    }

    private TextView title(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(24);
        view.setGravity(Gravity.START);
        view.setPadding(0, 0, 0, dp(10));
        return view;
    }

    private TextView section(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(18);
        view.setPadding(0, dp(10), 0, dp(4));
        return view;
    }

    private TextView value(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(22);
        view.setPadding(0, dp(2), 0, dp(2));
        return view;
    }

    private TextView normal(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(15);
        view.setPadding(0, dp(4), 0, dp(4));
        return view;
    }

    private TextView historyLine(String text) {
        TextView view = normal(text);
        view.setPadding(dp(8), dp(6), dp(8), dp(6));
        return view;
    }

    private int percent(int current, int total) {
        if (total <= 0) return 0;
        return Math.min(100, Math.round(current * 100f / total));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
