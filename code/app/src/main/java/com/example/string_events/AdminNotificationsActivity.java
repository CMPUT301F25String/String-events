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

/**
 * Activity that displays a log of notifications for admin users.
 * <p>
 * This screen shows a list of notifications (lottery notifications and messages)
 * loaded from the {@code notifications} collection in Firestore.
 * Admins can tap an item to see more details (handled by the adapter).
 */
public class AdminNotificationsActivity extends AppCompatActivity {

    /**
     * In-memory list of notifications displayed in the RecyclerView.
     */
    private ArrayList<Notification> notifList;

    /**
     * Adapter that binds {@link Notification} objects to the RecyclerView.
     */
    private AdminNotificationAdapter adapter;

    /**
     * Called when the activity is first created.
     * <p>
     * This method:
     * <ul>
     *     <li>Inflates the notification log layout.</li>
     *     <li>Initializes the RecyclerView and its adapter.</li>
     *     <li>Configures the back button to close the activity.</li>
     *     <li>Triggers loading of notifications from Firestore.</li>
     * </ul>
     *
     * @param savedInstanceState previously saved state, or {@code null} if created fresh
     */
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

    /**
     * Loads notifications from the Firestore {@code notifications} collection.
     * <p>
     * This method:
     * <ul>
     *     <li>Fetches all documents in the {@code notifications} collection.</li>
     *     <li>Parses each document into a {@link Notification} object depending on its type
     *         (lottery notification or message notification).</li>
     *     <li>Clears and repopulates the internal list, then notifies the adapter.</li>
     *     <li>Shows a toast if loading fails.</li>
     * </ul>
     */
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

                        // Lottery notification
                        if (selectedStatus) {
                            n = new Notification(
                                    username,
                                    true,
                                    eventId,
                                    imageUrl,
                                    eventName
                            );
                        }
                        // Message notification
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

                        // If it matched one of the types, add to list
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
