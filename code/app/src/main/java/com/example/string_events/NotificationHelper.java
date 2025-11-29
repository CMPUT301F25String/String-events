package com.example.string_events;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

/**
 * Helper for showing system push notifications for event selection
 * results and organizer messages. Can be called from any Activity.
 */
public class NotificationHelper {

    private static final String CHANNEL_ID = "event_notifications";
    private static final String CHANNEL_NAME = "Event Notifications";
    private static final String CHANNEL_DESC =
            "Notifications about lottery results and event messages";

    private static final String PREFS_NAME = "notification_prefs";
    private static final String PREF_KEY_ENABLED = "push_enabled";

    /**
     * 是否开启了应用内的通知总开关（默认 true）
     */
    public static boolean areNotificationsEnabled(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(PREF_KEY_ENABLED, true);
    }

    /**
     * 更新应用内通知开关
     */
    public static void setNotificationsEnabled(Context context, boolean enabled) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(PREF_KEY_ENABLED, enabled).apply();
    }


    public static boolean hasPostNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Creates the notification channel on API 26+ (safe to call many times).
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Shows a notification for a selection / non-selection result.
     * Clicking it opens EventDetailActivity like the in-app card.
     */
    public static void showSelectionNotification(Context context,
                                                 Notification notification) {


        if (!hasPostNotificationPermission(context) || !areNotificationsEnabled(context)) {
            return;
        }

        createNotificationChannel(context.getApplicationContext());

        String title;
        String text;

        if (notification.getSelectedStatus()) {
            title = "Congratulations, you were selected!";
            text = "You were selected for " + notification.getEventName();
        } else {
            title = "Unfortunately, you weren't selected";
            text = "You weren't selected for " + notification.getEventName();
        }

        Intent intent = new Intent(context, EventDetailActivity.class);
        intent.putExtra("event_id", notification.getEventId());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int requestCode = notification.getEventId() != null
                ? notification.getEventId().hashCode()
                : (int) System.currentTimeMillis();

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        int notificationId = ("SEL_" + notification.getEventId()).hashCode();
        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

    /**
     * Shows a notification for a custom organizer message.
     * Clicking it opens NotificationMessageDetailActivity.
     */
    public static void showMessageNotification(Context context,
                                               Notification notification) {


        if (!hasPostNotificationPermission(context) || !areNotificationsEnabled(context)) {
            return;
        }

        createNotificationChannel(context.getApplicationContext());

        String title = "Message regarding " + notification.getEventName();
        String text = notification.getMessageText();
        if (text == null || text.isEmpty()) {
            text = "Tap to view the message.";
        }

        Intent intent = new Intent(context, NotificationMessageDetailActivity.class);
        intent.putExtra("eventId", notification.getEventId());
        intent.putExtra("eventName", notification.getEventName());
        intent.putExtra("imageUrl",
                notification.getEventPhoto() != null ? notification.getEventPhoto() : "");
        intent.putExtra("messageText", notification.getMessageText());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int requestCode = ("MSG_" + notification.getEventId()).hashCode();

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        int notificationId = ("MSG_" + notification.getEventId()).hashCode();
        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

    /**
     * Entry point used by callers – decides which type to show.
     */
    public static void showNotification(Context context,
                                        Notification notification) {
        if (notification.isMessage()) {
            showMessageNotification(context, notification);
        } else {
            showSelectionNotification(context, notification);
        }
    }
}
