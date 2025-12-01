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

/**
 * Activity that displays the full details of a single notification for admins.
 * <p>
 * This screen shows:
 * <ul>
 *     <li>The notification recipient and message content.</li>
 *     <li>Associated event information (title, time, place).</li>
 *     <li>The event cover image loaded from a remote URL.</li>
 * </ul>
 */
public class AdminNotificationDetailActivity extends AppCompatActivity {

    /**
     * Label showing the notification recipient (username).
     */
    private TextView tvRecipientLabel;

    /**
     * Text view displaying the body of the notification or lottery message.
     */
    private TextView tvMessageBody;

    /**
     * Image view showing the cover image of the associated event.
     */
    private ImageView imgCover;

    /**
     * Text view for the associated event title.
     */
    private TextView tvTitle;

    /**
     * Text view for the associated event time.
     */
    private TextView tvTime;

    /**
     * Text view for the associated event location.
     */
    private TextView tvPlace;

    /**
     * Called when the activity is created.
     * <p>
     * This method:
     * <ul>
     *     <li>Inflates the layout for notification details.</li>
     *     <li>Binds all view references.</li>
     *     <li>Reads notification-related extras from the intent.</li>
     *     <li>Displays the recipient and message text based on notification type.</li>
     *     <li>If an event ID is provided, loads event details from Firestore.</li>
     * </ul>
     *
     * @param savedInstanceState previously saved state, or {@code null} if created fresh
     */
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

        // Show the recipient of this notification
        tvRecipientLabel.setText("Recipient: " + username);

        // Choose what to show in the message box based on notification type
        if (isMessage) {
            tvMessageBody.setText(messageText != null ? messageText : "(empty message)");
        } else if (selectedStatus) {
            tvMessageBody.setText("Lottery Accepted");
        } else {
            tvMessageBody.setText("Lottery Denied");
        }

        // If the notification is tied to an event, load that event's details
        if (eventId != null && !eventId.isEmpty()) {
            loadEventDetails(eventId);
        }
    }

    /**
     * Loads event details for the given event ID from Firestore and binds them to the UI.
     *
     * @param eventId the Firestore document ID of the associated event
     */
    private void loadEventDetails(String eventId) {

        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(this::bindEventData);
    }

    /**
     * Maps the retrieved Firestore document into the event UI fields.
     * <p>
     * This method:
     * <ul>
     *     <li>Reads event title, location, start time, and image URL.</li>
     *     <li>Formats and displays the start time.</li>
     *     <li>Displays the event cover image if a URL exists.</li>
     * </ul>
     *
     * @param doc the Firestore document snapshot representing the event
     */
    private void bindEventData(DocumentSnapshot doc) {

        if (!doc.exists()) return;

        String eventName = doc.getString("title");
        String eventLocation = doc.getString("location");
        String imageUrl = doc.getString("imageUrl");
        com.google.firebase.Timestamp start = doc.getTimestamp("startAt");

        tvTitle.setText(eventName != null ? eventName : "(No Title)");
        tvPlace.setText(eventLocation != null ? eventLocation : "(No Location)");

        if (start != null) {
            DateFormat df = DateFormat.getDateTimeInstance(
                    DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
            tvTime.setText(df.format(start.toDate()));
        } else {
            tvTime.setText("--");
        }

        // Load event cover image from the given URL, if provided
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
                    imgCover.post(() ->
                            imgCover.setImageResource(android.R.drawable.ic_menu_report_image));
                }
            }).start();
        }
    }
}
