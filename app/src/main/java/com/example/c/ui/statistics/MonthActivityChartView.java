package com.example.c.ui.statistics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.LinkedHashMap;
import java.util.Map;

public class MonthActivityChartView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Map<Integer, Integer> data = new LinkedHashMap<>();

    public MonthActivityChartView(Context context) { super(context); init(); }
    public MonthActivityChartView(Context context, AttributeSet attrs) { super(context, attrs); init(); }

    private void init() {
        textPaint.setTextSize(24f);
        setMinimumHeight(dp(220));
    }

    public void setData(Map<Integer, Integer> data) {
        this.data = data == null ? new LinkedHashMap<>() : data;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth() - getPaddingLeft() - getPaddingRight();
        int h = getHeight() - getPaddingTop() - getPaddingBottom() - dp(28);
        int left = getPaddingLeft();
        int top = getPaddingTop();
        if (data.isEmpty() || w <= 0 || h <= 0) return;

        int max = 1;
        for (Integer v : data.values()) if (v != null && v > max) max = v;
        float gap = dp(3);
        float barW = Math.max(3f, (w - gap * (data.size() - 1)) / Math.max(1, data.size()));
        int i = 0;
        for (Map.Entry<Integer, Integer> e : data.entrySet()) {
            int value = e.getValue() == null ? 0 : e.getValue();
            float x = left + i * (barW + gap);
            float barH = (value / (float) max) * h;
            paint.setAlpha(value > 0 ? 220 : 70);
            canvas.drawRoundRect(x, top + h - barH, x + barW, top + h, 8, 8, paint);
            if (e.getKey() == 1 || e.getKey() % 5 == 0) {
                canvas.drawText(String.valueOf(e.getKey()), x, top + h + dp(24), textPaint);
            }
            i++;
        }
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }
}
