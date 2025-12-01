package com.example.string_events;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Entry activity for QR/deep-link navigation into the app.
 * <p>
 * This activity is typically opened from a deep link (e.g. scanned QR code)
 * with a URL such as {@code stringevents://event/<id>} or a URL containing
 * an {@code eventId} query parameter. It extracts the event ID and forwards
 * the user to {@link MainActivity}, passing the ID along as an extra.
 */
public class EventQrEntryActivity extends AppCompatActivity {

    /**
     * Intent extra key used to pass the event ID into {@link MainActivity}.
     */
    public static final String EXTRA_EVENT_ID = "extra_event_id";

    /**
     * Called when the activity is created.
     * <p>
     * This method:
     * <ul>
     *     <li>Reads the incoming deep-link {@link Intent} and its {@link Uri} data.</li>
     *     <li>Extracts the event ID from the last path segment or an {@code eventId} query parameter.</li>
     *     <li>Starts {@link MainActivity}, including the event ID as an extra if present.</li>
     *     <li>Finishes this entry activity so it does not remain in the back stack.</li>
     * </ul>
     *
     * @param savedInstanceState previously saved state, or {@code null} if created fresh
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String eventId = null;

        // Intent that launched this Activity (from deep link)
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            // For links like stringevents://event/<id>
            String last = data.getLastPathSegment();
            if (last != null && !last.isEmpty()) {
                eventId = last;
            } else {
                // Optional: support query param style ?eventId=...
                String fromQuery = data.getQueryParameter("eventId");
                if (fromQuery != null && !fromQuery.isEmpty()) {
                    eventId = fromQuery;
                }
            }
        }

        // Forward the user to MainActivity, passing the event ID if available
        Intent open = new Intent(this, MainActivity.class);
        if (eventId != null && !eventId.isEmpty()) {
            open.putExtra(EXTRA_EVENT_ID, eventId);
        }
        open.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(open);
        finish();
    }
}
