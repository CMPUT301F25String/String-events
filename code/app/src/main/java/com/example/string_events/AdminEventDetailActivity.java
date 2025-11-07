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

public class AdminEventDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId;

    private ImageView imgEvent;
    private TextView tvTitle, tvLocation, tvRegStart, tvRegEnd, tvWaitlist, tvAttendees,
            tvDescription, tvStatus, tvEventDates, tvCategory, tvVisibility, tvGeo, tvCreator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_event_details_screen);

        // Initialize Firestore and get event ID
        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("event_id");

        // --- Find Views ---
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



        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnDelete = findViewById(R.id.btnDelete);
        ImageButton btnQRCode = findViewById(R.id.btnQRCode);
        ImageButton btnEventLink = findViewById(R.id.btnEventLink);

        // --- Buttons ---
        btnBack.setOnClickListener(v -> finish());
        btnDelete.setOnClickListener(v -> deleteEvent());
        btnQRCode.setOnClickListener(v -> Toast.makeText(this, "QR Code clicked", Toast.LENGTH_SHORT).show());
        btnEventLink.setOnClickListener(v -> Toast.makeText(this, "Event Link clicked", Toast.LENGTH_SHORT).show());

        // Load from Firestore
        loadEventDetails();
    }

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

                    // --- Retrieve Firestore fields ---
                    String title = doc.getString("title");
                    String location = doc.getString("location");
                    String description = doc.getString("description");
                    String imageUrl = doc.getString("imageUrl");
                    String categories = doc.getString("categories");
                    String creator = doc.getString("creator");

                    Boolean geoReq = doc.getBoolean("geolocationReq");
                    Boolean visibility = doc.getBoolean("visibility");

                    Long waitlist = doc.getLong("waitlistLimit");
                    Long attendees = doc.getLong("maxAttendees");
                    Long attendeeCount = doc.getLong("attendeesCount");

                    Timestamp startAt = doc.getTimestamp("startAt");
                    Timestamp endAt = doc.getTimestamp("endAt");
                    Timestamp regStart = doc.getTimestamp("regStartAt");
                    Timestamp regEnd = doc.getTimestamp("regEndAt");

                    // --- Format ---
                    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());

                    // --- Display data ---
                    tvTitle.setText(title != null ? title : "(No title)");
                    tvLocation.setText(location != null ? location : "(No location)");
                    tvDescription.setText(description != null ? description : "(No description)");

                    if (tvCategory != null)
                        tvCategory.setText("Category: " + (categories != null ? categories : "-"));
                    if (tvCreator != null)
                        tvCreator.setText("Creator: " + (creator != null ? creator : "-"));
                    if (tvGeo != null)
                        tvGeo.setText("Geolocation Required: " + (geoReq != null && geoReq ? "Yes" : "No"));
                    if (tvVisibility != null)
                        tvVisibility.setText("Visibility: " + (visibility != null && visibility ? "Public" : "Private"));

                    tvWaitlist.setText("Waitlist Limit: " + (waitlist != null ? waitlist : "-"));
                    tvAttendees.setText("Max Attendees: " + (attendees != null ? attendees : "-") +
                            " | Current: " + (attendeeCount != null ? attendeeCount : "0"));

                    tvRegStart.setText("Registration Start: " + (regStart != null ? df.format(regStart.toDate()) : "-"));
                    tvRegEnd.setText("Registration End: " + (regEnd != null ? df.format(regEnd.toDate()) : "-"));

                    if (startAt != null && endAt != null)
                        tvEventDates.setText(df.format(startAt.toDate()) + " - " + df.format(endAt.toDate()));

                    // --- Status color ---
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

                    // --- Load image from URL ---
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        new Thread(() -> {
                            try {
                                URL url = new URL(imageUrl);
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setDoInput(true);
                                conn.connect();
                                InputStream input = conn.getInputStream();
                                Bitmap bmp = BitmapFactory.decodeStream(input);
                                imgEvent.post(() -> imgEvent.setImageBitmap(bmp));
                            } catch (Exception e) {
                                imgEvent.post(() -> imgEvent.setImageResource(android.R.drawable.ic_menu_report_image));
                            }
                        }).start();
                    } else {
                        imgEvent.setImageResource(android.R.drawable.ic_menu_report_image);
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show());
    }

    private void deleteEvent() {
        if (eventId == null) return;

        db.collection("events").document(eventId).delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete event", Toast.LENGTH_SHORT).show());
    }
}
