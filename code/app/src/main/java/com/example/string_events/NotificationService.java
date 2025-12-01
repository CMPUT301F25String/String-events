package com.example.string_events;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Date;

import java.util.HashSet;
import java.util.Set;

/**
 * Foreground service that listens to Firestore for new notification documents
 * for the currently logged-in user and shows system push notifications.
 * <p>
 * Behavior:
 * <ul>
 *     <li>Starts as a foreground service with a sticky notification.</li>
 *     <li>Subscribes to the {@code notifications} collection for the current username.</li>
 *     <li>Ignores existing documents on first load to prevent duplicates.</li>
 *     <li>Shows a push notification for each newly added document belonging to the user.</li>
 * </ul>
 */
public class NotificationService extends Service {

    /**
     * Notification channel ID used for all event update notifications.
     */
    private final String CHANNEL_ID = "event_updates_channel";

    /**
     * Flag used to detect and skip the initial Firestore snapshot
     * (so existing documents do not trigger notifications).
     */
    private boolean isFirstLoad = true;

    /**
     * Set of processed Firestore document IDs to avoid sending duplicate
     * notifications for the same document within a service session.
     */
    private final Set<String> processedDocIds = new HashSet<>();
//    private Timestamp listenerStartTime;

    /**
     * Called when the service is first created.
     * <p>
     * Initializes the notification channel required for posting notifications
     * on Android 8.0 (API 26) and above.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
//        listenerStartTime = new Timestamp(new Date());
    }

    /**
     * Called each time the service is started via {@link android.content.Context#startService(Intent)}.
     * <p>
     * This method:
     * <ul>
     *     <li>Starts the service in the foreground with a persistent notification.</li>
     *     <li>Reads the logged-in username and notification preference from shared preferences.</li>
     *     <li>If notifications are enabled and a username is set, starts the Firestore listener.</li>
     *     <li>Stops the service if no user or if notifications are disabled.</li>
     * </ul>
     *
     * @param intent  the Intent supplied to {@code startService}, if any
     * @param flags   additional flags for the start request
     * @param startId a unique integer representing this specific request to start
     * @return {@link #START_STICKY} so the service is restarted if killed by the system
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start foreground immediately so the service is less likely to be killed
        startForeground(999, getStickyNotification());
        NotificationServiceHelper.isNotificationServiceRunning = true;

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String username = sharedPreferences.getString("user", null);

        // Only start listener if enabled in settings
        boolean isEnabled = sharedPreferences.getBoolean("notifications_enabled", true);

        if (username != null && isEnabled) {
            startFirestoreListener(username);
        } else {
            // No user or notifications disabled → stop service
            stopSelf();
        }

        return START_STICKY;
    }

    /**
     * Starts a real-time Firestore listener on the {@code notifications} collection
     * for documents belonging to the specified username.
     * <p>
     * Logic:
     * <ul>
     *     <li>On the first snapshot, marks existing documents as processed and skips notifications.</li>
     *     <li>On subsequent snapshots, processes {@link DocumentChange.Type#ADDED} changes only.</li>
     *     <li>Generates a push notification depending on the notification type:
     *         message vs. lottery selection status.</li>
     * </ul>
     *
     * @param username username for which notifications should be listened to
     */
    private void startFirestoreListener(String username) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("notifications")
                .whereEqualTo("username", username)
//                .whereGreaterThan("createdAt", listenerStartTime)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        if (snapshots != null) {
//                             1. Handle Initial Load (Existing Data)
                            if (isFirstLoad) {
//                                 Mark all existing docs as "processed" so we don't notify for them
                                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                    processedDocIds.add(dc.getDocument().getId());
                                }
                                isFirstLoad = false;
                                return;
                            }

                            // 2. Handle New Updates
                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                String docId = dc.getDocument().getId();

                                // Only process ADDED documents that we haven't seen this session
                                if (dc.getType() == DocumentChange.Type.ADDED
                                        && !processedDocIds.contains(docId)
                                ) {

                                    // Mark as processed immediately
                                    processedDocIds.add(docId);

                                    String eventName = dc.getDocument().getString("eventName");
                                    String message = "You have a new update!";

                                    if (Boolean.TRUE.equals(dc.getDocument().getBoolean("ismessage"))) {
                                        message = "New Message: " + dc.getDocument().getString("message");
                                    } else if (Boolean.TRUE.equals(dc.getDocument().getBoolean("selectedStatus"))) {
                                        message = "Congratulations! You were selected.";
                                    }

                                    sendPushNotification(eventName, message);
                                }
                            }
                        }
                    }
                });
    }

    /**
     * Builds and shows a push notification that opens {@link NotificationScreen}
     * when tapped.
     *
     * @param title notification title (usually the event name)
     * @param body  notification message body
     */
    private void sendPushNotification(String title, String body) {
        Intent intent = new Intent(this, NotificationScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.event_image)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Use current time as ID so multiple notifications can stack
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    /**
     * Creates the notification channel required for posting notifications
     * on Android Oreo (API 26) and later.
     * <p>
     * If the channel already exists, the call is effectively a no-op.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Event Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Creates the persistent foreground notification used to keep the service alive.
     *
     * @return a non-dismissible notification indicating that the app is listening for updates
     */
    private Notification getStickyNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Event Monitor")
                .setContentText("Listening for new updates...")
                .setSmallIcon(R.drawable.event_image)
                .build();
    }

    /**
     * This service is not designed for binding, so this method always returns {@code null}.
     *
     * @param intent binding intent (unused)
     * @return {@code null}, since binding is not supported
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
