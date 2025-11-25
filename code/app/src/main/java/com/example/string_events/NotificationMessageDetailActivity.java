package com.example.string_events;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NotificationMessageDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_message_detail);

        ImageView back = findViewById(R.id.back_button);
        back.setOnClickListener(v -> finish());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        TextView tvEventTitle = findViewById(R.id.tvEventTitle);
        TextView tvLocation = findViewById(R.id.tvLocation);
        TextView tvDateLine = findViewById(R.id.tvDateLine);
        TextView tvTimeLine = findViewById(R.id.tvTimeLine);
        TextView tvMessageContent = findViewById(R.id.tvMessageContent);
        ImageView ivEventImage = findViewById(R.id.ivEventImage);

        String eventId = getIntent().getStringExtra("eventId");
        String message = getIntent().getStringExtra("messageText");

        tvMessageContent.setText(message != null ? message : "");

        if (eventId == null) {
            return;
        }

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        return;
                    }

                    String eventName = doc.getString("title");
                    String location = doc.getString("location");
                    String imageUrl = doc.getString("imageUrl");

                    // Convert timestamps to readable text
                    com.google.firebase.Timestamp startTs = doc.getTimestamp("startAt");
                    com.google.firebase.Timestamp endTs = doc.getTimestamp("endAt");

                    if (eventName != null) tvEventTitle.setText(eventName);
                    if (location != null) tvLocation.setText(location);

                    if (startTs != null) {
                        java.util.Date start = startTs.toDate();

                        java.text.SimpleDateFormat dateFmt = new java.text.SimpleDateFormat("EEE, MMM d, yyyy");
                        java.text.SimpleDateFormat timeFmt = new java.text.SimpleDateFormat("h:mm a");

                        tvDateLine.setText(dateFmt.format(start));

                        if (endTs != null) {
                            java.util.Date end = endTs.toDate();
                            tvTimeLine.setText(timeFmt.format(start) + " - " + timeFmt.format(end));
                        } else {
                            tvTimeLine.setText(timeFmt.format(start));
                        }
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
                                ivEventImage.post(() -> ivEventImage.setImageBitmap(bmp));
                            } catch (Exception ex) {
                                ivEventImage.post(() ->
                                        ivEventImage.setImageResource(android.R.drawable.ic_menu_report_image));
                            }
                        }).start();
                    } else {
                        ivEventImage.setImageResource(android.R.drawable.ic_menu_report_image);
                    }
                });
    }
}
