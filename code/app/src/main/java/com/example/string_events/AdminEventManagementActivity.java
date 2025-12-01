package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Activity that allows admins to browse and manage all events stored in Firestore.
 * <p>
 * This screen:
 * <ul>
 *     <li>Displays a scrollable list of events using a RecyclerView.</li>
 *     <li>Loads event data from the "events" collection in Firestore.</li>
 *     <li>Provides a back button to navigate to the admin dashboard.</li>
 * </ul>
 */
public class AdminEventManagementActivity extends AppCompatActivity {

    /**
     * RecyclerView that displays the list of events.
     */
    private RecyclerView recyclerView;

    /**
     * Adapter used to bind {@link EventItem} data to the RecyclerView.
     */
    private AdminEventAdapter adapter;

    /**
     * In-memory list of events retrieved from Firestore.
     */
    private ArrayList<EventItem> events;

    /**
     * Firestore database reference used to query events.
     */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Called when the activity is first created.
     * <p>
     * This method:
     * <ul>
     *     <li>Sets up the layout for the event management screen.</li>
     *     <li>Initializes the RecyclerView and its adapter.</li>
     *     <li>Configures the back button to return to the admin dashboard.</li>
     *     <li>Triggers loading of events from Firestore.</li>
     * </ul>
     *
     * @param savedInstanceState previously saved state, or {@code null} if created fresh
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_event_management_screen);

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(AdminEventManagementActivity.this, AdminDashboardActivity.class);
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
     * Loads events from the Firestore "events" collection and updates the RecyclerView.
     * <p>
     * Events are ordered by their {@code startAt} timestamp in ascending order.
     * For each document, an {@link EventItem} is constructed and added to the list.
     * Once loading is complete, the adapter is notified to refresh the UI.
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

                        e.categories = d.getString("categories");
                        e.creator = d.getString("creator");

                        e.maxAttendees = asInt(d.get("maxAttendees"));
                        e.attendeesCount = asInt(d.get("attendeesCount"));
                        e.waitlistLimit = asInt(d.get("waitlistLimit"));

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
     * Safely converts an arbitrary object to an {@code int}.
     * <p>
     * This is used to handle Firestore fields that may be stored as different numeric types
     * or may be missing.
     *
     * @param o the object to convert (may be {@code null}, a {@link Number}, or a String)
     * @return the integer value if conversion succeeds; {@code 0} otherwise
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
     * Simple data holder representing a single event document from Firestore.
     * <p>
     * Fields correspond to Firestore document fields in the "events" collection.
     */
    public static class EventItem {
        /**
         * Firestore document ID of the event.
         */
        String id;

        /**
         * Title/name of the event.
         */
        String title;

        /**
         * Location where the event takes place.
         */
        String location;

        /**
         * URL of the event's cover image.
         */
        String imageUrl;

        /**
         * Text description of the event.
         */
        String description;

        /**
         * Categories/tags associated with the event.
         */
        String categories;

        /**
         * Identifier or name of the event creator.
         */
        String creator;

        /**
         * Maximum allowed number of attendees.
         */
        int maxAttendees;

        /**
         * Current number of attendees registered for the event.
         */
        int attendeesCount;

        /**
         * Maximum allowed number of people in the waitlist.
         */
        int waitlistLimit;

        /**
         * Timestamp when the event starts.
         */
        Timestamp startAt;

        /**
         * Timestamp when the event ends.
         */
        Timestamp endAt;

        /**
         * Timestamp when registration opens.
         */
        Timestamp regStartAt;

        /**
         * Timestamp when registration closes.
         */
        Timestamp regEndAt;
    }
}
