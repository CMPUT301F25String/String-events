package com.example.string_events;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Detail screen for a message-style notification.
 * <p>
 * This activity shows:
 * <ul>
 *     <li>The event title, location, date and time range.</li>
 *     <li>The full text of the message sent to the user.</li>
 *     <li>The event's cover image (if available).</li>
 * </ul>
 * It fetches event information from Firestore using the {@code eventId}
 * passed in the launching {@link android.content.Intent}.
 */
public class NotificationMessageDetailActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * <p>
     * This method:
     * <ul>
     *     <li>Inflates the layout for the notification message details.</li>
     *     <li>Wires the back button to finish the activity.</li>
     *     <li>Reads {@code eventId} and {@code messageText} from the intent extras.</li>
     *     <li>Displays the message text.</li>
     *     <li>Loads event metadata (title, location, time, image) from Firestore
     *         and binds it to the UI.</li>
     * </ul>
     *
     * @param savedInstanceState previously saved state, or {@code null} if created fresh
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_message_detail);

        // Back button to return to the previous screen
        ImageView back = findViewById(R.id.back_button);
        back.setOnClickListener(v -> finish());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // UI references
        TextView tvEventTitle = findViewById(R.id.tvEventTitle);
        TextView tvLocation = findViewById(R.id.tvLocation);
        TextView tvDateLine = findViewById(R.id.tvDateLine);
        TextView tvTimeLine = findViewById(R.id.tvTimeLine);
        TextView tvMessageContent = findViewById(R.id.tvMessageContent);
        ImageView ivEventImage = findViewById(R.id.ivEventImage);

        // Read values passed from NotificationAdapter
        String eventId = getIntent().getStringExtra("eventId");
        String message = getIntent().getStringExtra("messageText");

        // Show the message body (or empty string if null)
        tvMessageContent.setText(message != null ? message : "");

        // If no event ID was provided, there is nothing more to load
        if (eventId == null) {
            return;
        }

        // Load event details from Firestore
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

                    if (eventName != null) {
                        tvEventTitle.setText(eventName);
                    }
                    if (location != null) {
                        tvLocation.setText(location);
                    }

                    // Format and display date and time interval
                    if (startTs != null) {
                        java.util.Date start = startTs.toDate();

                        java.text.SimpleDateFormat dateFmt =
                                new java.text.SimpleDateFormat("EEE, MMM d, yyyy");
                        java.text.SimpleDateFormat timeFmt =
                                new java.text.SimpleDateFormat("h:mm a");

                        tvDateLine.setText(dateFmt.format(start));

                        if (endTs != null) {
                            java.util.Date end = endTs.toDate();
                            tvTimeLine.setText(timeFmt.format(start) + " - " + timeFmt.format(end));
                        } else {
                            tvTimeLine.setText(timeFmt.format(start));
                        }
                    }

                    // Load event image if an image URL is available
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
