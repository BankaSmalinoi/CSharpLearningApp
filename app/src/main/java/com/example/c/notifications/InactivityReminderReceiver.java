package com.example.c.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.c.R;
import com.example.c.ui.main.MainActivity;

public class InactivityReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "study_inactivity";

    @Override
    public void onReceive(Context context, Intent intent) {
        int stage = intent == null ? 1 : intent.getIntExtra("stage", 1);
        showNotification(context, stage);
        InactivityReminderScheduler.scheduleNextAfterNotification(context, stage);
    }

    private void showNotification(Context context, int stage) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Напоминания об обучении",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String text = stage <= 1
                ? "Вы не занимались 1 день. Вернитесь к C# хотя бы на пару минут."
                : stage <= 3
                ? "Вы не занимались уже 3 дня. Самое время продолжить обучение."
                : "Вы давно не занимались. Продолжите проходить теорию, задачи и тесты.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Пора вернуться к обучению")
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(openPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        manager.notify(4107, builder.build());
    }
}
