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

public class EventMessageActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText etMessage;

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
                        Map<String, Object> data = new HashMap<>();
                        data.put("username", username);
                        data.put("message", message);
                        data.put("eventId", eventId);
                        data.put("ismessage", true);
                        data.put("eventName", eventName);
                        data.put("imageUrl", imageUrl != null ? imageUrl : "");

                        db.collection("notifications").add(data);
                    }

                    Toast.makeText(this, "Message sent to " + groupName + " users", Toast.LENGTH_LONG).show();
                    finish();
                });
    }
}
