package com.example.string_events;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class NotificationScreen extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_screen);

        ImageButton homeImageButton = findViewById(R.id.btnHome);
        ImageButton cameraImageButton = findViewById(R.id.btnCamera);
        ImageButton notificationImageButton = findViewById(R.id.btnNotification);
        ImageButton profileImageButton = findViewById(R.id.btnProfile);

        homeImageButton.setOnClickListener(view -> {
            Intent intent = new Intent(NotificationScreen.this, MainActivity.class);
            startActivity(intent);
        });

        profileImageButton.setOnClickListener(view -> {
            Intent intent = new Intent(NotificationScreen.this, ProfileScreen.class);
            startActivity(intent);
        });

//        // testing data
//        notificationsList = new ArrayList<>();
//        Notification testNotification1 = new Notification(true, Uri.EMPTY, "New Event1");
//        Notification testNotification2 = new Notification(false, Uri.EMPTY, "New Event2");
//        notificationsList.add(testNotification1);
//        notificationsList.add(testNotification2);

        ArrayList<Notification> notificationsList = new ArrayList<>();
        db.collection("notifications")
//                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    notificationsList.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        boolean selected = Boolean.TRUE.equals(d.getBoolean("selectedStatus"));
                        String name = d.getString("eventName");
                        String imageUrl = d.getString("imageUrl");
                        Uri photo = imageUrl == null || imageUrl.isEmpty() ? null : Uri.parse(imageUrl);
                        notificationsList.add(new Notification(selected, photo, name));
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
