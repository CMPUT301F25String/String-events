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

public class EventOverviewScreen extends AppCompatActivity {

    private static final String TAG = "EventOverview";

    private FirebaseFirestore db;

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

    private final SimpleDateFormat dateTimeFmt =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

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

        btnBack.setOnClickListener(v -> finish());

        btnCancelEvent.setOnClickListener(v ->
                Toast.makeText(this, "Cancel event action not implemented yet", Toast.LENGTH_SHORT).show()
        );

        btnEventDetails.setOnClickListener(v -> {
            Intent intent = new Intent(EventOverviewScreen.this, OrganizerEventDetailScreen.class);
            intent.putExtra(OrganizerEventDetailScreen.EVENT_ID, eventId);
            startActivity(intent);
        });

        btnQrCode.setOnClickListener(v -> {
            Intent intent = new Intent(EventOverviewScreen.this, QrCodeActivity.class);
            intent.putExtra(QrCodeActivity.EXTRA_EVENT_ID, eventId);
            startActivity(intent);
        });

        loadEvent(eventId);
    }

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

    private int asInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            return 0;
        }
    }

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

    @Override
    public void onBackPressed() {
        finish();
    }
}
