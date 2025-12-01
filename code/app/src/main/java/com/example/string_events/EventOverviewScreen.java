package com.example.string_events;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Activity that displays a high-level overview of a single event to the organizer.
 * <p>
 * This screen shows:
 * <ul>
 *     <li>Event cover image</li>
 *     <li>Event name and time range</li>
 *     <li>Location</li>
 *     <li>Registration start and end times</li>
 *     <li>Waitlist limit and current attendees count</li>
 *     <li>Up to two category tags</li>
 *     <li>Description</li>
 * </ul>
 * It also provides navigation to:
 * <ul>
 *     <li>The detailed organizer event screen</li>
 *     <li>A QR code screen for the event</li>
 * </ul>
 */
public class EventOverviewScreen extends AppCompatActivity {

    /**
     * Tag used for logging within this activity.
     */
    private static final String TAG = "EventOverview";

    /**
     * Firestore instance used to load event data.
     */
    private FirebaseFirestore db;

    // UI components for event overview
    private ImageView imgEventCover;
    private TextView tvEventName;
    private TextView tvEventTimeRange;
    private TextView tvEventLocation;
    private TextView tvRegStart;
    private TextView tvRegEnd;
    private TextView tvWaitlistLimit;
    private TextView tvAttendees;
    private TextView tvTag1;
    private TextView tvTag2;
    private TextView tvDescription;
    private ImageButton btnBack;
    private ImageButton btnCancelEvent; // image-style button at bottom
    private MaterialButton btnQrCode;

    /**
     * Formatter for displaying date and time values on the UI.
     * <p>
     * Format example: {@code 2025-11-28 16:30}
     */
    private final SimpleDateFormat dateTimeFmt =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    /**
     * Called when the activity is first created.
     * <p>
     * This method:
     * <ul>
     *     <li>Initializes Firestore and binds all views.</li>
     *     <li>Reads the event ID from the launching intent.</li>
     *     <li>Sets up click listeners for back, cancel, details, and QR code buttons.</li>
     *     <li>Triggers loading of event data from Firestore.</li>
     * </ul>
     *
     * @param savedInstanceState previously saved state, or {@code null} if created fresh
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.org_event_overview_screen);

        db = FirebaseFirestore.getInstance();

        imgEventCover     = findViewById(R.id.imgEventCover);
        tvEventName       = findViewById(R.id.tvEventName);
        tvEventTimeRange  = findViewById(R.id.tvEventTimeRange);
        tvEventLocation   = findViewById(R.id.tvEventLocation);
        tvRegStart        = findViewById(R.id.tvRegStart);
        tvRegEnd          = findViewById(R.id.tvRegEnd);
        tvWaitlistLimit   = findViewById(R.id.tvWaitlistLimit);
        tvAttendees       = findViewById(R.id.tvAttendees);
        tvTag1            = findViewById(R.id.tvTag1);
        tvTag2            = findViewById(R.id.tvTag2);
        tvDescription     = findViewById(R.id.tvDescription);
        btnBack           = findViewById(R.id.btnBack);
        btnCancelEvent    = findViewById(R.id.btnCancelEvent);
        MaterialButton btnEventDetails = findViewById(R.id.btnEventDetails);
        btnQrCode         = findViewById(R.id.btnQrCode);

        String eventId = getIntent().getStringExtra("event_id");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "No event id provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Navigate back to the previous screen
        btnBack.setOnClickListener(v -> finish());

        // Placeholder handler for cancel event action
        btnCancelEvent.setOnClickListener(v ->
                Toast.makeText(this, "Cancel event action not implemented yet", Toast.LENGTH_SHORT).show()
        );

        // Open the detailed organizer event screen
        btnEventDetails.setOnClickListener(v -> {
            Intent intent = new Intent(EventOverviewScreen.this, OrganizerEventDetailScreen.class);
            intent.putExtra(OrganizerEventDetailScreen.EVENT_ID, eventId);
            startActivity(intent);
        });

        // Open the QR code screen for this event
        btnQrCode.setOnClickListener(v -> {
            Intent intent = new Intent(EventOverviewScreen.this, QrCodeActivity.class);
            intent.putExtra(QrCodeActivity.EXTRA_EVENT_ID, eventId);
            startActivity(intent);
        });

        // Load event data from Firestore
        loadEvent(eventId);
    }

    /**
     * Loads the event document from Firestore by ID and binds it to the UI.
     *
     * @param eventId the Firestore document ID of the event to load
     */
    private void loadEvent(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(this::bindEvent)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading event", e);
                    Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * Binds event data from a Firestore document to the UI components.
     * <p>
     * This method:
     * <ul>
     *     <li>Reads core fields such as title, location, description, image URL.</li>
     *     <li>Formats and displays start/end times and registration periods.</li>
     *     <li>Displays waitlist limit and attendee count.</li>
     *     <li>Shows up to two category tags.</li>
     *     <li>Starts asynchronous loading of the cover image if a URL is provided.</li>
     * </ul>
     *
     * @param doc the Firestore {@link DocumentSnapshot} representing the event
     */
    private void bindEvent(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String title       = doc.getString("title");
        String location    = doc.getString("location");
        String description = doc.getString("description");
        String imageUrl    = doc.getString("imageUrl");

        Timestamp startAt     = doc.getTimestamp("startAt");
        Timestamp endAt       = doc.getTimestamp("endAt");
        Timestamp regStartAt  = doc.getTimestamp("regStartAt");
        Timestamp regEndAt    = doc.getTimestamp("regEndAt");

        int waitlistLimit     = asInt(doc.get("waitlistLimit"));
        int attendeesCount    = asInt(doc.get("attendeesCount"));

        tvEventName.setText(title != null ? title : "");

        String timeRange = "";
        if (startAt != null) {
            timeRange += dateTimeFmt.format(startAt.toDate());
        }
        if (endAt != null) {
            timeRange += " - " + dateTimeFmt.format(endAt.toDate());
        }
        tvEventTimeRange.setText(timeRange);

        tvEventLocation.setText(location != null ? location : "");

        tvRegStart.setText(regStartAt != null ? dateTimeFmt.format(regStartAt.toDate()) : "");
        tvRegEnd.setText(regEndAt != null ? dateTimeFmt.format(regEndAt.toDate()) : "");

        tvWaitlistLimit.setText(String.valueOf(waitlistLimit));
        tvAttendees.setText(String.valueOf(attendeesCount));

        // Handle categories as either a List or a single String
        Object cats = doc.get("categories");
        if (cats instanceof List) {
            List<?> list = (List<?>) cats;
            if (!list.isEmpty()) {
                tvTag1.setText(String.valueOf(list.get(0)));
                tvTag1.setVisibility(View.VISIBLE);
            } else {
                tvTag1.setVisibility(View.GONE);
            }
            if (list.size() > 1) {
                tvTag2.setText(String.valueOf(list.get(1)));
                tvTag2.setVisibility(View.VISIBLE);
            } else {
                tvTag2.setVisibility(View.GONE);
            }
        } else if (cats instanceof String) {
            tvTag1.setText((String) cats);
            tvTag1.setVisibility(View.VISIBLE);
            tvTag2.setVisibility(View.GONE);
        } else {
            tvTag1.setVisibility(View.GONE);
            tvTag2.setVisibility(View.GONE);
        }

        tvDescription.setText(description != null ? description : "");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            loadImageAsync(imageUrl);
        }
    }

    /**
     * Safely converts an arbitrary object to an {@code int}.
     * <p>
     * Used to read numeric fields from Firestore that might be stored
     * as different numeric types or strings.
     *
     * @param o the object to convert
     * @return the integer value if conversion succeeds, otherwise {@code 0}
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
     * Loads the event cover image from a URL on a background thread and
     * updates the {@link ImageView} on the UI thread.
     *
     * @param imageUrl the URL of the image to load
     */
    private void loadImageAsync(String imageUrl) {
        new Thread(() -> {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream input = conn.getInputStream();
                Bitmap bmp = BitmapFactory.decodeStream(input);
                imgEventCover.post(() -> imgEventCover.setImageBitmap(bmp));
            } catch (Exception e) {
                Log.e(TAG, "Error loading image", e);
            }
        }).start();
    }

    /**
     * Handles the system back button press.
     * <p>
     * Simply finishes the activity and returns to the previous screen.
     */
    @Override
    public void onBackPressed() {
        finish();
    }
}
