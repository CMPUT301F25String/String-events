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
 * Organizer-facing overview screen for a single event.
 * <p>
 * This activity:
 * <ul>
 *     <li>Displays the event cover image, title, time range, location, registration window,
 *     waitlist and attendee counts, tags, and description.</li>
 *     <li>Allows the organizer to:
 *         <ul>
 *             <li>Edit the event (opens {@link CreateEventScreen}).</li>
 *             <li>View detailed information (opens {@link OrganizerEventDetailScreen}).</li>
 *             <li>Generate a QR code (opens {@link QrCodeActivity}).</li>
 *             <li>Cancel and delete the event.</li>
 *         </ul>
 *     </li>
 * </ul>
 */
public class OrganizerEventOverviewScreen extends AppCompatActivity {

    /**
     * Tag used for logging errors or debug information.
     */
    private static final String TAG = "EventOverview";

    /**
     * Firestore database instance for loading and deleting events.
     */
    private FirebaseFirestore db;

    // UI elements for displaying event information
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
    /**
     * Bottom image-style button that confirms and triggers event cancellation/deletion.
     */
    private ImageButton btnCancelEvent;

    /**
     * Button that opens a QR code screen for the current event.
     */
    private MaterialButton btnQrCode;

    /**
     * Formatter used to display date/time values (e.g., {@code 2025-11-28 17:00}).
     */
    private final SimpleDateFormat dateTimeFmt =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    /**
     * Called when the activity is first created.
     * <p>
     * This method:
     * <ul>
     *     <li>Inflates the organizer event overview layout.</li>
     *     <li>Binds all UI elements.</li>
     *     <li>Retrieves the {@code event_id} from the launching intent.</li>
     *     <li>Wires up navigation and action buttons (back, cancel, edit, details, QR code).</li>
     *     <li>Loads the event data from Firestore.</li>
     * </ul>
     *
     * @param savedInstanceState saved state, or {@code null} if newly created
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
        MaterialButton btnEditEvent = findViewById(R.id.btnEditEvent);
        MaterialButton btnEventDetails = findViewById(R.id.btnEventDetails);
        btnQrCode         = findViewById(R.id.btnQrCode);

        // Read event id from intent; if missing, show error and close
        String eventId = getIntent().getStringExtra("event_id");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "No event id provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Navigate back to previous screen
        btnBack.setOnClickListener(v -> finish());

        // Confirm and delete the event when cancel button is pressed
        btnCancelEvent.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Cancel Event")
                    .setMessage("Are you sure you want to permanently cancel and delete this event? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteEvent(eventId);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Open event in edit screen, passing the event ID
        btnEditEvent.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerEventOverviewScreen.this, CreateEventScreen.class);
            intent.putExtra(OrganizerEventDetailScreen.EVENT_ID, eventId);
            startActivity(intent);
        });

        // Open detailed organizer view of the event
        btnEventDetails.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerEventOverviewScreen.this, OrganizerEventDetailScreen.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });

        // Open QR code screen for this event
        btnQrCode.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerEventOverviewScreen.this, QrCodeActivity.class);
            intent.putExtra(QrCodeActivity.EXTRA_EVENT_ID, eventId);
            startActivity(intent);
        });

        // Load event data from Firestore
        loadEvent(eventId);
    }

    /**
     * Fetches event data from Firestore and delegates to {@link #bindEvent(DocumentSnapshot)}
     * to populate the UI.
     *
     * @param eventId ID of the event document to load
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
     * Binds Firestore document fields to the overview UI.
     * <p>
     * This method:
     * <ul>
     *     <li>Extracts title, location, description, image URL, timestamps, categories, and counts.</li>
     *     <li>Formats and displays the event time range and registration dates.</li>
     *     <li>Shows waitlist and attendee counts.</li>
     *     <li>Displays up to two category tags.</li>
     *     <li>Loads the cover image asynchronously if a URL is present.</li>
     * </ul>
     *
     * @param doc Firestore document snapshot containing event data
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

        // Handle categories field, which can be a list or a single string
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
     * Deletes the event document from Firestore.
     * <p>
     * On success, the user is informed and navigated back to
     * {@link OrganizerEventScreen}. On failure, an error message
     * is shown in a toast and a log entry is recorded.
     *
     * @param eventId ID of the event document to delete
     */
    private void deleteEvent(String eventId) {
        db.collection("events").document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Event successfully deleted!");
                    Toast.makeText(OrganizerEventOverviewScreen.this, "Event has been deleted.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(OrganizerEventOverviewScreen.this, OrganizerEventScreen.class));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting event", e);
                    Toast.makeText(OrganizerEventOverviewScreen.this, "Error: Could not delete event.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Utility method to safely convert an {@link Object} to {@code int}.
     * <p>
     * Supports {@link Number} instances directly and falls back to
     * parsing via {@link String#valueOf(Object)} when possible.
     * Returns {@code 0} if parsing fails or the value is {@code null}.
     *
     * @param o value to convert
     * @return integer representation, or {@code 0} if invalid
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
     * Loads the event cover image from a URL in a background thread and sets it into
     * {@link #imgEventCover}. If any error occurs, it is logged and the image is left unchanged.
     *
     * @param imageUrl HTTP(S) URL of the image to load
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
     * Handles the back button press from the system navigation.
     * <p>
     * Calls the super implementation and ensures the activity is finished.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
