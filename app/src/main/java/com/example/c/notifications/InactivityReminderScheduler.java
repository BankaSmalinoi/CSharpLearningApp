package com.example.c.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class InactivityReminderScheduler {
    private static final int REQUEST_CODE = 4107;
    private static final long DAY = 24L * 60L * 60L * 1000L;

    public static void reschedule(Context context) {
        cancel(context);
        schedule(context, System.currentTimeMillis() + DAY, 1);
    }

    public static void scheduleNextAfterNotification(Context context, int stage) {
        int nextStage;
        long delay;
        if (stage <= 1) { nextStage = 3; delay = 2L * DAY; }
        else if (stage <= 3) { nextStage = 7; delay = 4L * DAY; }
        else { nextStage = 14; delay = 7L * DAY; }
        schedule(context, System.currentTimeMillis() + delay, nextStage);
    }

    public static void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        alarmManager.cancel(pendingIntent(context, 1));
    }

    private static void schedule(Context context, long triggerAtMillis, int stage) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        PendingIntent pi = pendingIntent(context, stage);
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
    }

    private static PendingIntent pendingIntent(Context context, int stage) {
        Intent intent = new Intent(context, InactivityReminderReceiver.class);
        intent.putExtra("stage", stage);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
