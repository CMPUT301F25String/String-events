package com.example.string_events;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Locale;

public class AdminNotificationDetailActivity extends AppCompatActivity {

    private TextView tvRecipientLabel, tvMessageBody;
    private ImageView imgCover;
    private TextView tvTitle, tvTime, tvPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_details);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvRecipientLabel = findViewById(R.id.tvRecipientLabel);
        tvMessageBody = findViewById(R.id.tvMessageBody);
        imgCover = findViewById(R.id.img_cover);
        tvTitle = findViewById(R.id.tv_title);
        tvTime = findViewById(R.id.tv_time);
        tvPlace = findViewById(R.id.tv_place);

        String username = getIntent().getStringExtra("username");
        String eventId = getIntent().getStringExtra("eventId");
        boolean selectedStatus = getIntent().getBooleanExtra("selectedStatus", false);
        boolean isMessage = getIntent().getBooleanExtra("isMessage", false);
        String messageText = getIntent().getStringExtra("messageText");

        tvRecipientLabel.setText("Recipient: " + username);

        // choose what to show in the message box
        if (isMessage) {
            tvMessageBody.setText(messageText != null ? messageText : "(empty message)");
        } else if (selectedStatus) {
            tvMessageBody.setText("Lottery Accepted");
        } else {
            tvMessageBody.setText("Lottery Denied");
        }

        if (eventId != null && !eventId.isEmpty()) {
            loadEventDetails(eventId);
        }
    }

    private void loadEventDetails(String eventId) {

        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(this::bindEventData);
    }

    private void bindEventData(DocumentSnapshot doc) {

        if (!doc.exists()) return;

        String eventName = doc.getString("title");
        String eventLocation = doc.getString("location");
        String imageUrl = doc.getString("imageUrl");
        com.google.firebase.Timestamp start = doc.getTimestamp("startAt");

        tvTitle.setText(eventName != null ? eventName : "(No Title)");
        tvPlace.setText(eventLocation != null ? eventLocation : "(No Location)");

        if (start != null) {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
            tvTime.setText(df.format(start.toDate()));
        } else {
            tvTime.setText("--");
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    URL url = new URL(imageUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream input = conn.getInputStream();
                    Bitmap bmp = BitmapFactory.decodeStream(input);

                    imgCover.post(() -> imgCover.setImageBitmap(bmp));

                } catch (Exception e) {
                    imgCover.post(() -> imgCover.setImageResource(android.R.drawable.ic_menu_report_image));
                }
            }).start();
        }
    }
}
