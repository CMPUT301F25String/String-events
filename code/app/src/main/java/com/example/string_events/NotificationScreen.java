package com.example.string_events;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Build;

/**
 * Screen that lists notifications for the signed-in user.
 * <p>
 * Fetches documents from the {@code notifications} collection filtered by username
 * and displays them in a {@link RecyclerView}.
 */
public class NotificationScreen extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final ArrayList<Notification> notificationsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_screen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }

        ImageButton homeImageButton = findViewById(R.id.btnHome);
        ImageButton cameraImageButton = findViewById(R.id.btnCamera);
        ImageButton notificationImageButton = findViewById(R.id.btnNotification);
        ImageButton profileImageButton = findViewById(R.id.btnProfile);

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String username = sharedPreferences.getString("user", null);

        homeImageButton.setOnClickListener(view -> {
            Intent intent = new Intent(NotificationScreen.this, MainActivity.class);
            startActivity(intent);
        });

        // camera button opens QrScanActivity
        cameraImageButton.setOnClickListener(view -> {
            Intent intent = new Intent(NotificationScreen.this, QrScanActivity.class);
            startActivity(intent);
        });

        profileImageButton.setOnClickListener(view -> {
            Intent intent = new Intent(NotificationScreen.this, ProfileScreen.class);
            startActivity(intent);
        });

        notificationImageButton.setOnClickListener(view -> {
            boolean currentlyEnabled = NotificationHelper.areNotificationsEnabled(NotificationScreen.this);
            NotificationHelper.setNotificationsEnabled(NotificationScreen.this, !currentlyEnabled);

            if (!currentlyEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !NotificationHelper.hasPostNotificationPermission(NotificationScreen.this)) {

                    ActivityCompat.requestPermissions(
                            NotificationScreen.this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            1001
                    );
                }
                Toast.makeText(NotificationScreen.this,
                        "Push notifications enabled in app.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(NotificationScreen.this,
                        "Push notifications disabled in app.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        db.collection("notifications")
                .whereEqualTo("username", username)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {

                    notificationsList.clear();

                    for (QueryDocumentSnapshot d : snap) {

                        String eventId = d.getString("eventId");
                        String eventName = d.getString("eventName");
                        String imageUrl = d.getString("imageUrl");
                        String photo = (imageUrl == null || imageUrl.isEmpty()) ? null : imageUrl;

                        boolean hasMessageField = d.contains("ismessage");
                        boolean isMessage = hasMessageField && Boolean.TRUE.equals(d.getBoolean("ismessage"));

                        Notification notif;

                        if (isMessage) {
                            String messageText = d.getString("message");
                            notif = new Notification(
                                    username,
                                    eventId,
                                    eventName,
                                    photo,
                                    true,
                                    messageText
                            );
                        } else {
                            boolean selected = Boolean.TRUE.equals(d.getBoolean("selectedStatus"));
                            notif = new Notification(
                                    username,
                                    selected,
                                    eventId,
                                    photo,
                                    eventName
                            );
                        }

                        notificationsList.add(notif);
                    }

                    setupRecyclerView(notificationsList);
                });
    }

    private void setupRecyclerView(ArrayList<Notification> notificationsList) {
        RecyclerView notificationRecyclerview = findViewById(R.id.notifications_recyclerView);
        NotificationAdapter notificationAdapter = new NotificationAdapter(this, notificationsList);
        notificationRecyclerview.setAdapter(notificationAdapter);
        notificationRecyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }
}
