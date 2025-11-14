package com.example.string_events;

import android.content.Intent;
import android.content.SharedPreferences;
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

/**
 * Screen that lists notifications for the signed-in user.
 * <p>
 * Fetches documents from the {@code notifications} collection filtered by username
 * and displays them in a {@link RecyclerView}.
 */
public class NotificationScreen extends AppCompatActivity {
    /** Firestore client used to query notifications. */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Inflates the layout, wires basic navigation buttons, retrieves the current
     * username from {@code SharedPreferences}, queries notifications for that user,
     * and renders them in a {@link RecyclerView}.
     *
     * @param savedInstanceState previously saved instance state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_screen);

        ImageButton homeImageButton = findViewById(R.id.btnHome);
        ImageButton cameraImageButton = findViewById(R.id.btnCamera);
        ImageButton notificationImageButton = findViewById(R.id.btnNotification);
        ImageButton profileImageButton = findViewById(R.id.btnProfile);

        // get the username of the user using the app
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String username = sharedPreferences.getString("user", null);

        homeImageButton.setOnClickListener(view -> {
            Intent intent = new Intent(NotificationScreen.this, MainActivity.class);
            startActivity(intent);
        });

        profileImageButton.setOnClickListener(view -> {
            Intent intent = new Intent(NotificationScreen.this, ProfileScreen.class);
            startActivity(intent);
        });

        ArrayList<Notification> notificationsList = new ArrayList<>();
        db.collection("notifications")
                .whereEqualTo("username", username)
//                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    notificationsList.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        boolean selected = Boolean.TRUE.equals(d.getBoolean("selectedStatus"));
                        String eventId = d.getString("eventId");
                        String name = d.getString("eventName");
                        String imageUrl = d.getString("imageUrl");
                        Uri photo = imageUrl == null || imageUrl.isEmpty() ? null : Uri.parse(imageUrl);
                        notificationsList.add(new Notification(username, selected, eventId, photo, name));
                    }
                    setupRecyclerView(notificationsList);
                });
    }

    /**
     * Configures the notifications {@link RecyclerView} with an adapter and a
     * vertical {@link LinearLayoutManager}.
     *
     * @param notificationsList list of notifications to display
     */
    private void setupRecyclerView(ArrayList<Notification> notificationsList) {
        RecyclerView notificationRecyclerview = findViewById(R.id.notifications_recyclerView);
        NotificationAdapter notificationAdapter = new NotificationAdapter(this, notificationsList);
        notificationRecyclerview.setAdapter(notificationAdapter);
        notificationRecyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }
}
