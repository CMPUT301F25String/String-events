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

public class NotificationService extends Service {

    private final String CHANNEL_ID = "event_updates_channel";

    // Flags to prevent duplicates
    private boolean isFirstLoad = true;
    private final Set<String> processedDocIds = new HashSet<>();
//    private Timestamp listenerStartTime;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
//        listenerStartTime = new Timestamp(new Date());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start Foreground immediately
        startForeground(999, getStickyNotification());
        NotificationServiceHelper.isNotificationServiceRunning = true;

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String username = sharedPreferences.getString("user", null);

        // Only start listener if enabled in settings
        boolean isEnabled = sharedPreferences.getBoolean("notifications_enabled", true);

        if (username != null && isEnabled) {
            startFirestoreListener(username);
        } else {
            stopSelf();
        }

        return START_STICKY;
    }

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

    private Notification getStickyNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Event Monitor")
                .setContentText("Listening for new updates...")
                .setSmallIcon(R.drawable.event_image)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}