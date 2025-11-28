package com.example.string_events;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Organizer-facing details screen for a specific event.
 * <p>
 * Provides navigation to participant-related lists (canceled, participating, waitlist).
 * The target event is identified via {@link #EVENT_ID}.
 */
public class OrganizerEventDetailScreen extends AppCompatActivity {

    /**
     * Intent extra key containing the event ID to manage.
     */
    public static final String EVENT_ID = "eventId";

    private FirebaseFirestore db;
    private TextView tvEventName;
    private TextView tvTime;
    private TextView tvLocation;
    private ImageView imgBanner;
    private final SimpleDateFormat dateTimeFmt =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    /**
     * Inflates the organizer event details UI, wires the back button, reads the
     * event ID from the intent, and sets up navigation to related user lists.
     *
     * @param savedInstanceState previously saved instance state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.org_event_detail_screen);

        db = FirebaseFirestore.getInstance();
        tvEventName = findViewById(R.id.tvEventName);
        tvTime = findViewById(R.id.tvTime);
        tvLocation = findViewById(R.id.tvLocation);
        imgBanner = findViewById(R.id.imgBanner);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        String eventId = getIntent().getStringExtra(EVENT_ID);
        assert eventId != null;
        loadEvent(eventId);

        findViewById(R.id.btnRoll).setOnClickListener(v -> {
            Intent it = new Intent(this, LotteryDrawActivity.class);
            it.putExtra(EVENT_ID, eventId);
            startActivity(it);
        });

        findViewById(R.id.btnParticipating).setOnClickListener(v -> {
            Intent it = new Intent(this, ParticipatingUsersActivity.class);
            it.putExtra(EVENT_ID, eventId);
            startActivity(it);
        });

        findViewById(R.id.btnInvited).setOnClickListener(v -> {
            Intent it = new Intent(this, InvitedUsersActivity.class);
            it.putExtra(EVENT_ID, eventId);
            startActivity(it);
        });

        findViewById(R.id.btnWaitlist).setOnClickListener(v -> {
            Intent it = new Intent(this, WaitlistUsersActivity.class);
            it.putExtra(EVENT_ID, eventId);
            startActivity(it);
        });

        findViewById(R.id.btnCanceled).setOnClickListener(v -> {
            Intent it = new Intent(this, CanceledUsersActivity.class);
            it.putExtra(EVENT_ID, eventId);
            startActivity(it);
        });

        View mapBtn = findViewById(R.id.btnWaitlistMap);
        if (mapBtn != null) {
            mapBtn.setOnClickListener(v -> {
            });
        }
    }

    private void loadEvent(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(this::bindEvent)
                .addOnFailureListener(e -> finish());
    }

    private void bindEvent(DocumentSnapshot doc) {
        if (!doc.exists()) {
            finish();
            return;
        }

        String title = doc.getString("title");
        String location = doc.getString("location");
        Timestamp startAt = doc.getTimestamp("startAt");
        Timestamp endAt = doc.getTimestamp("endAt");
        String imageUrl = doc.getString("imageUrl");

        tvEventName.setText(title != null ? title : "");
        tvLocation.setText(location != null ? location : "");

        String timeText = "";
        if (startAt != null) {
            timeText += dateTimeFmt.format(startAt.toDate());
        }
        if (endAt != null) {
            if (!timeText.isEmpty()) {
                timeText += " - ";
            }
            timeText += dateTimeFmt.format(endAt.toDate());
        }
        tvTime.setText(timeText);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    URL url = new URL(imageUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream input = conn.getInputStream();
                    Bitmap bmp = BitmapFactory.decodeStream(input);
                    imgBanner.post(() -> imgBanner.setImageBitmap(bmp));
                } catch (Exception ex) {
                    imgBanner.post(() ->
                            imgBanner.setImageResource(android.R.drawable.ic_menu_report_image));
                }
            }).start();
        } else {
            imgBanner.setImageResource(android.R.drawable.ic_menu_report_image);
        }
    }
}
