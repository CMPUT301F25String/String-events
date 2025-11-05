package com.example.string_events;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotificationScreen extends AppCompatActivity {
    ArrayList<Notification> notificationsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_screen);

        ImageButton homeImageButton = findViewById(R.id.btnHome);
        ImageButton cameraImageButton = findViewById(R.id.btnCamera);
        ImageButton notificationImageButton = findViewById(R.id.btnNotification);
        ImageButton profileImageButton = findViewById(R.id.btnProfile);

        // testing data
        notificationsList = new ArrayList<>();
        Notification testNotification1 = new Notification(true, Uri.EMPTY, "New Event1");
        Notification testNotification2 = new Notification(false, Uri.EMPTY, "New Event2");
        notificationsList.add(testNotification1);
        notificationsList.add(testNotification2);

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        RecyclerView notificationRecyclerview = findViewById(R.id.notifications_recyclerView);
        NotificationAdapter notificationAdapter = new NotificationAdapter(this, notificationsList);
        notificationRecyclerview.setAdapter(notificationAdapter);
        notificationRecyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }
}
