package com.example.string_events;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity that allows an organizer/admin to send a message to a specific
 * group of users related to an event (participants, waitlist, or canceled users).
 * <p>
 * Messages are stored in the {@code notifications} collection in Firestore,
 * one document per user recipient.
 */
public class EventMessageActivity extends AppCompatActivity {

    /**
     * Firestore instance used to read event data and write notifications.
     */
    private FirebaseFirestore db;

    /**
     * Input field where the organizer types the message to send.
     */
    private EditText etMessage;

    /**
     * Called when the activity is first created.
     * <p>
     * This method:
     * <ul>
     *     <li>Initializes the Firestore instance.</li>
     *     <li>Sets up the back button to close this screen.</li>
     *     <li>Finds the message input and send button views.</li>
     *     <li>Extracts the target event ID and user group from the intent.</li>
     *     <li>Configures the send button to trigger {@link #sendMessage(String, String)}.</li>
     * </ul>
     *
     * @param savedInstanceState previously saved state, or {@code null} if created fresh
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_message);

        db = FirebaseFirestore.getInstance();

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        etMessage = findViewById(R.id.etMessage);

        String eventId = getIntent().getStringExtra(OrganizerEventDetailScreen.EVENT_ID);
        String group = getIntent().getStringExtra("target_group");

        ImageButton btnSend = findViewById(R.id.btnSendMessage);
        btnSend.setOnClickListener(v -> sendMessage(eventId, group));
    }

    /**
     * Sends a message to all users in the specified group for the given event.
     * <p>
     * The method:
     * <ul>
     *     <li>Validates that the message is not empty.</li>
     *     <li>Loads the event document to obtain event metadata (name, image).</li>
     *     <li>Determines the target user list based on {@code groupName}:
     *         <ul>
     *             <li>{@code "participating"} → {@code attendees}</li>
     *             <li>{@code "waitlist"} → {@code waitlist}</li>
     *             <li>{@code "canceled"} → {@code canceledusers}</li>
     *         </ul>
     *     </li>
     *     <li>Creates a notification document in Firestore for each user.</li>
     *     <li>Shows a confirmation toast and finishes the activity on success.</li>
     * </ul>
     *
     * @param eventId   Firestore ID of the event whose users will receive the message
     * @param groupName target user group name (e.g., "participating", "waitlist", "canceled")
     */
    private void sendMessage(String eventId, String groupName) {
        String message = etMessage.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String eventName = doc.getString("title");
                    String imageUrl = doc.getString("imageUrl");

                    List<String> users = null;

                    switch (groupName) {
                        case "participating":
                            users = (List<String>) doc.get("attendees");
                            break;
                        case "waitlist":
                            users = (List<String>) doc.get("waitlist");
                            break;
                        case "canceled":
                            users = (List<String>) doc.get("canceledusers");
                            break;
                    }

                    if (users == null || users.isEmpty()) {
                        Toast.makeText(this, "No users in this group", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (String username : users) {
                        Notification newNotification = new Notification(username, eventId, eventName, imageUrl, true, message);

                        Map<String, Object> data = new HashMap<>();
                        data.put("username", newNotification.getUsername());
                        data.put("eventId", newNotification.getEventId());
                        data.put("eventName", newNotification.getEventName());
                        data.put("imageUrl", newNotification.getEventPhoto());
                        data.put("ismessage", true);
                        data.put("message", newNotification.getMessageText());
                        data.put("createdAt", newNotification.getTimeStamp());

                        db.collection("notifications").add(data);
                    }

                    Toast.makeText(this, "Message sent to " + groupName + " users", Toast.LENGTH_LONG).show();
                    finish();
                });
    }
}
