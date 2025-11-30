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

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;



//client side listener, server wont push message to the phoen, phone uses something that keep watching notification on firebase User A creates a notification in the database for User B.
//
//User B's phone (running the background service) detects a new document instantly.
//
//User B's phone triggers a local notification.
// should be no backend not sure how to test it.




public class NotificationService extends Service {

    private final String CHANNEL_ID = "event_updates_channel";
    private boolean isFirstLoad = true; // Prevents spamming old notifications on startup

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // start the service in the foreground
        startForeground(999, getStickyNotification());

        // 2. Get current username
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String username = sharedPreferences.getString("user", null);

        if (username != null) {
            startFirestoreListener(username);
        }

        return START_STICKY; //  restart this service
    }

    private void startFirestoreListener(String username) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // watch for any documents where username matches the logged in user
        db.collection("notifications")
                .whereEqualTo("username", username)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("NotifService", "Listen failed.", e);
                            return;
                        }

                        if (snapshots != null) {
                            // If its the very first time connect, don't send notifications

                            if (isFirstLoad) {
                                isFirstLoad = false;
                                return;
                            }

                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                // Only trigger if a new document is added
                                if (dc.getType() == DocumentChange.Type.ADDED) {

                                    String eventName = dc.getDocument().getString("eventName");
                                    String message = "You have a new update!";

                                    // Customizing message based on your fields
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
                .setContentText("Listening for new events...")
                .setSmallIcon(R.drawable.event_image)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}