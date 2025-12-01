package com.example.string_events;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Locale;

/**
 * Admin detail screen that displays full event information fetched from Firestore.
 * <p>
 * Shows cover image, title, location, registration window, capacity/waitlist,
 * status (scheduled / in-progress / finished), date range, and organizer info.
 */
public class AdminEventDetailActivity extends AppCompatActivity {

    /**
     * Firestore instance used to load and delete event documents.
     */
    private FirebaseFirestore db;

    /**
     * ID of the event being displayed on this screen.
     */
    private String eventId;

    // UI components for displaying event information
    private ImageView imgEvent;
    private TextView tvTitle, tvLocation, tvRegStart, tvRegEnd, tvWaitlist, tvAttendees,
            tvDescription, tvStatus, tvEventDates, tvCategory, tvVisibility, tvGeo, tvCreator,
            tvOrganizer;

    /**
     * Called when the activity is first created.
     * <p>
     * This method:
     * <ul>
     *     <li>Initializes Firestore and retrieves the event ID from the intent.</li>
     *     <li>Binds all required views.</li>
     *     <li>Sets click listeners for back, delete, and QR code buttons.</li>
     *     <li>Triggers loading the event details from Firestore.</li>
     * </ul>
     *
     * @param savedInstanceState previously saved state, or {@code null} if created fresh
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_event_details_screen);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("event_id");

        // Bind views
        imgEvent = findViewById(R.id.imgEvent);
        tvTitle = findViewById(R.id.tvEventName);
        tvLocation = findViewById(R.id.tvLocation);
        tvRegStart = findViewById(R.id.tvRegStart);
        tvRegEnd = findViewById(R.id.tvRegEnd);
        tvWaitlist = findViewById(R.id.tvWaitlist);
        tvAttendees = findViewById(R.id.tvAttendees);
        tvDescription = findViewById(R.id.tvDescription);
        tvStatus = findViewById(R.id.tvStatus);
        tvEventDates = findViewById(R.id.tvEventDates);
        tvOrganizer = findViewById(R.id.tvOrganizer);

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnDelete = findViewById(R.id.btnDelete);
        com.google.android.material.button.MaterialButton btnQRCode = findViewById(R.id.btnQRCode);

        // Navigation/back
        btnBack.setOnClickListener(v -> finish());

        // Delete event from Firestore
        btnDelete.setOnClickListener(v -> deleteEvent());

        // Placeholder action for QR code button
        btnQRCode.setOnClickListener(v ->
                Toast.makeText(this, "QR Code clicked", Toast.LENGTH_SHORT).show());

        // Fetch and display event details
        loadEventDetails();
    }

    /**
     * Loads event details from Firestore and populates the UI.
     * <p>
     * This method:
     * <ul>
     *     <li>Validates that an event ID was provided.</li>
     *     <li>Fetches the event document from the "events" collection.</li>
     *     <li>Maps Firestore fields to UI components.</li>
     *     <li>Determines event status based on start/end timestamps.</li>
     *     <li>Loads the event image from a URL asynchronously.</li>
     * </ul>
     */
    private void loadEventDetails() {
        if (eventId == null) {
            Toast.makeText(this, "Invalid event", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Basic string fields
                    String title = doc.getString("title");
                    String location = doc.getString("location");
                    String description = doc.getString("description");
                    String imageUrl = doc.getString("imageUrl");
                    String categories = doc.getString("categories");
                    String creator = doc.getString("creator");

                    // Boolean fields
                    Boolean geoReq = doc.getBoolean("geolocationReq");
                    Boolean visibility = doc.getBoolean("visibility");

                    // Numeric fields
                    Long waitlist = doc.getLong("waitlistLimit");
                    Long attendees = doc.getLong("maxAttendees");
                    Long attendeeCount = doc.getLong("attendeesCount");

                    // Timestamp fields
                    Timestamp startAt = doc.getTimestamp("startAt");
                    Timestamp endAt = doc.getTimestamp("endAt");
                    Timestamp regStart = doc.getTimestamp("regStartAt");
                    Timestamp regEnd = doc.getTimestamp("regEndAt");

                    DateFormat df = DateFormat.getDateTimeInstance(
                            DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());

                    // Text fields with fallbacks
                    tvTitle.setText(title != null ? title : "(No title)");
                    tvLocation.setText(location != null ? location : "(No location)");
                    tvDescription.setText(description != null ? description : "(No description)");

                    // Organizer label
                    tvOrganizer.setText("Organizer: " + (creator != null ? creator : "Unknown"));

                    // Optional detail labels (may be null depending on layout)
                    if (tvCategory != null) {
                        tvCategory.setText("Category: " + (categories != null ? categories : "-"));
                    }
                    if (tvCreator != null) {
                        tvCreator.setText("Creator: " + (creator != null ? creator : "-"));
                    }
                    if (tvGeo != null) {
                        tvGeo.setText("Geolocation Required: " +
                                (geoReq != null && geoReq ? "Yes" : "No"));
                    }
                    if (tvVisibility != null) {
                        tvVisibility.setText("Visibility: " +
                                (visibility != null && visibility ? "Public" : "Private"));
                    }

                    tvWaitlist.setText("Waitlist Limit: " + (waitlist != null ? waitlist : "-"));
                    tvAttendees.setText("Max Attendees: " + (attendees != null ? attendees : "-") +
                            " | Current: " + (attendeeCount != null ? attendeeCount : "0"));

                    tvRegStart.setText("Registration Start: " +
                            (regStart != null ? df.format(regStart.toDate()) : "-"));
                    tvRegEnd.setText("Registration End: " +
                            (regEnd != null ? df.format(regEnd.toDate()) : "-"));

                    if (startAt != null && endAt != null) {
                        tvEventDates.setText(df.format(startAt.toDate()) + " - " +
                                df.format(endAt.toDate()));
                    }

                    // Compute status based on current time and event window
                    long now = System.currentTimeMillis();
                    long start = startAt != null ? startAt.toDate().getTime() : Long.MAX_VALUE;
                    long end = endAt != null ? endAt.toDate().getTime() : Long.MAX_VALUE;

                    if (now < start) {
                        tvStatus.setText("Scheduled");
                        tvStatus.setBackgroundColor(0xFF43C06B);
                    } else if (now > end) {
                        tvStatus.setText("Finished");
                        tvStatus.setBackgroundColor(0xFFE45A5A);
                    } else {
                        tvStatus.setText("In Progress");
                        tvStatus.setBackgroundColor(0xFFF1A428);
                    }

                    // Load cover image from URL if present
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        new Thread(() -> {
                            try {
                                URL url = new URL(imageUrl);
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setDoInput(true);
                                conn.connect();
                                try (InputStream input = conn.getInputStream()) {
                                    Bitmap bmp = BitmapFactory.decodeStream(input);
                                    imgEvent.post(() -> imgEvent.setImageBitmap(bmp));
                                }
                            } catch (Exception e) {
                                imgEvent.post(() ->
                                        imgEvent.setImageResource(android.R.drawable.ic_menu_report_image));
                            }
                        }).start();
                    } else {
                        imgEvent.setImageResource(android.R.drawable.ic_menu_report_image);
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show());
    }

    /**
     * Deletes the current event document from Firestore.
     * <p>
     * On success, a short confirmation message is shown and the activity finishes.
     * On failure, an error message is displayed and the user remains on this screen.
     */
    private void deleteEvent() {
        if (eventId == null) {
            return;
        }

        db.collection("events").document(eventId).delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete event", Toast.LENGTH_SHORT).show());
    }
}
