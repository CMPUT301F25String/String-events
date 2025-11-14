package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;
import java.util.ArrayList;

/**
 * Admin screen for browsing and managing all events stored in Firestore.
 * <p>Displays a scrollable list of events ordered by {@code startAt} ascending
 * and navigates back to {@link MainActivity} (with admin role) via the toolbar back button.</p>
 *
 * @since 1.0
 */
public class AdminEventManagementActivity extends AppCompatActivity {

    /** Event list UI. */
    private RecyclerView recyclerView;
    /** Adapter for rendering event cards. */
    private AdminEventAdapter adapter;
    /** Backing list for the adapter. */
    private ArrayList<EventItem> events;
    /** Firestore instance used to fetch events. */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Inflates the layout, configures RecyclerView and triggers Firestore load.
     *
     * @param savedInstanceState standard saved state; may be {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_event_management_screen);

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(AdminEventManagementActivity.this, MainActivity.class);
                intent.putExtra("role", "admin");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        events = new ArrayList<>();
        adapter = new AdminEventAdapter(events, this);
        recyclerView.setAdapter(adapter);

        loadEvents();
    }

    /**
     * Loads events from the {@code events} collection in Firestore and updates the adapter.
     * <p>Documents are ordered by {@code startAt} ascending. All fields are read defensively.</p>
     */
    private void loadEvents() {
        db.collection("events")
                .orderBy("startAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    events.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        EventItem e = new EventItem();
                        e.id = d.getId();
                        e.title = d.getString("title");
                        e.location = d.getString("location");
                        e.imageUrl = d.getString("imageUrl");
                        e.description = d.getString("description");
                        e.maxAttendees = asInt(d.get("maxAttendees"));
                        e.attendeesCount = asInt(d.get("attendeesCount"));
                        e.waitlistLimit = asInt(d.get("waitlistLimit"));
                        e.categories = d.getString("categories");
                        e.startAt = d.getTimestamp("startAt");
                        e.endAt = d.getTimestamp("endAt");
                        e.regStartAt = d.getTimestamp("regStartAt");
                        e.regEndAt = d.getTimestamp("regEndAt");
                        events.add(e);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    /**
     * Converts an arbitrary object to {@code int}, returning 0 on failure/null.
     *
     * @param o object to convert (may be {@link Number}, {@link String}, or {@code null})
     * @return parsed integer value or 0 if not convertible
     */
    private int asInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Lightweight DTO used by the adapter for rendering event rows.
     * <p>Fields map 1:1 to Firestore document keys where applicable.</p>
     */
    public static class EventItem {
        /** Firestore document id. */
        String id;
        /** Event title. */
        String title;
        /** Venue/location text. */
        String location;
        /** Cover image URL (may be {@code null} or empty). */
        String imageUrl;
        /** Event description (may be {@code null}). */
        String description;
        /** Optional categories string. */
        String categories;
        /** Capacity values. */
        int maxAttendees, attendeesCount, waitlistLimit;
        /** Timing fields. */
        Timestamp startAt, endAt, regStartAt, regEndAt;
    }
}
