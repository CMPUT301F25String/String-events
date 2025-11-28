package com.example.string_events;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Organizer entry screen listing organizer actions and events.
 * Shows events created by current user and lets them create new events.
 */
public class OrganizerEventScreen extends AppCompatActivity {

    private final ArrayList<OrganizerEvent> data = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.org_events_screen);

        ImageButton createEventButton = findViewById(R.id.create_event_button);
        ImageButton profileButton = findViewById(R.id.btnProfile);

        createEventButton.setOnClickListener(view -> {
            Intent intent = new Intent(OrganizerEventScreen.this, CreateEventScreen.class);
            startActivity(intent);
        });
        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(OrganizerEventScreen.this, ProfileScreen.class);
            startActivity(intent);
        });

        RecyclerView rvEvents = findViewById(R.id.recyclerEvents);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        OrganizerEventAdapter adapter = new OrganizerEventAdapter(data);
        rvEvents.setAdapter(adapter);

        loadMyEvents(adapter);
    }

    /**
     * Loads events from Firestore where creator == currently logged in user.
     */
    private void loadMyEvents(OrganizerEventAdapter organizerEventAdapter) {
        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        String currentUser = sp.getString("user", null);

        if (currentUser == null || currentUser.trim().isEmpty()) {
            Log.w("ORG_EVENTS", "No logged in user in SharedPreferences (userInfo.user).");
            data.clear();
            return;
        }

        db.collection("events")
                .whereEqualTo("creator", currentUser)
                .get()
                .addOnSuccessListener(snap -> {
                    data.clear();

                    if (snap.isEmpty()) {
                        Log.d("ORG_EVENTS", "No events found for creator: " + currentUser);
                    } else {
                        Log.d("ORG_EVENTS", "Found " + snap.size() + " events for " + currentUser);
                    }

                    for (QueryDocumentSnapshot d : snap) {
                        OrganizerEvent e = new OrganizerEvent();
                        e.id = d.getId();
                        e.title = d.getString("title");
                        e.location = d.getString("location");
                        e.startAt = d.getTimestamp("startAt");
                        e.maxAttendees = asInt(d.get("maxAttendees"));
                        e.attendeesCount = asInt(d.get("attendeesCount"));
                        e.imageUrl = d.getString("imageUrl");
                        e.creator = d.getString("creator");
                        data.add(e);
                    }

                    organizerEventAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e("ORG_EVENTS", "Error loading events: ", e));
    }

    private int asInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception ex) {
            return 0;
        }
    }
}
