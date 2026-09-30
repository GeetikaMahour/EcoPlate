package com.aspire.ecoplate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class ReminderReceiver extends BroadcastReceiver {

    static final String ACTION_DAILY = "com.aspire.ecoplate.ACTION_DAILY_REMINDER";
    private static final String CHANNEL_ID = "expiry_reminders";
    private static final int NOTIFICATION_ID = 2001;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            ReminderScheduler.schedule(context);
            return;
        }
        notifyNow(context, false);
    }

    /**
     * Shows the "use these soon" notification.
     * When force is true (used for demos) a notification is shown even if nothing is urgent.
     */
    @SuppressLint("MissingPermission")
    static void notifyNow(Context context, boolean force) {
        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        List<PantryItem> urgent = new ArrayList<>();
        for (PantryItem item : new DatabaseHelper(context).getActive()) {
            if (item.daysLeft() <= 2) urgent.add(item);
        }

        String title;
        String text;
        if (urgent.isEmpty()) {
            if (!force) return;
            title = "All clear";
            text = "Nothing in your pantry expires in the next 2 days.";
        } else {
            title = urgent.size() == 1 ? "1 item to use up soon" : urgent.size() + " items to use up soon";
            text = summarize(urgent);
        }

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Expiry reminders", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Daily reminder about food that is about to expire");
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }

        Intent open = new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(
                context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setColor(ContextCompat.getColor(context, R.color.eco_primary))
                .setContentIntent(pending)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
    }

    private static String summarize(List<PantryItem> items) {
        StringBuilder sb = new StringBuilder();
        int shown = Math.min(3, items.size());
        for (int i = 0; i < shown; i++) {
            if (i > 0) sb.append(", ");
            sb.append(items.get(i).name);
        }
        if (items.size() > shown) {
            sb.append(" and ").append(items.size() - shown).append(" more");
        }
        sb.append(". Open EcoPlate for recipe ideas.");
        return sb.toString();
    }
}
