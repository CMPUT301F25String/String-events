package com.example.string_events;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class EventQrEntryActivity extends AppCompatActivity {

    // Key used to pass event id into MainActivity
    public static final String EXTRA_EVENT_ID = "extra_event_id";

    // Fallback to event3 if parsing fails
    private static final String EVENT3_ID = "07d4dd53-3efe-4613-b852-0720a924be8b";

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

        if (eventId == null || eventId.isEmpty()) {
            eventId = EVENT3_ID;
        }

        // Forward user into MainActivity and pass along the event id
        Intent open = new Intent(this, MainActivity.class);
        open.putExtra(EXTRA_EVENT_ID, eventId);
        open.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(open);
        finish();
    }
}
