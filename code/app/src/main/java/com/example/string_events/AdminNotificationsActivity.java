package com.example.string_events;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdminNotificationsActivity extends AppCompatActivity {

    private ArrayList<Notification> notifList;
    private AdminNotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_notification_log);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        notifList = new ArrayList<>();
        adapter = new AdminNotificationAdapter(notifList);

        RecyclerView rv = findViewById(R.id.recyclerViewNotifications);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {

        FirebaseFirestore.getInstance()
                .collection("notifications")
                .get()
                .addOnSuccessListener(q -> {
                    notifList.clear();

                    for (QueryDocumentSnapshot d : q) {

                        String username   = d.getString("username");
                        String eventId    = d.getString("eventId");
                        String eventName  = d.getString("eventName");
                        String imageUrl   = d.getString("imageUrl");
                        Boolean sel       = d.getBoolean("selectedStatus");
                        Boolean isMsgFlag = d.getBoolean("ismessage");
                        String msgText    = d.getString("message");

                        boolean selectedStatus = sel != null && sel;
                        boolean isMessage = isMsgFlag != null && isMsgFlag;

                        Notification n = null;

                        // lottery notification
                        if (selectedStatus) {
                            n = new Notification(
                                    username,
                                    true,
                                    eventId,
                                    imageUrl,
                                    eventName
                            );
                        }
                        // message notification
                        else if (isMessage) {
                            n = new Notification(
                                    username,
                                    eventId,
                                    eventName,
                                    imageUrl,
                                    true,
                                    msgText
                            );
                        }

                        // if it matched one of the types, add to list
                        if (n != null) {
                            notifList.add(n);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                });
    }
}
