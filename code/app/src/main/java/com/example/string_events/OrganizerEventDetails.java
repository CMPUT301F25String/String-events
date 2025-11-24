package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Organizer-facing details screen for a specific event.
 * <p>
 * Provides navigation to participant-related lists (canceled, participating, waitlist).
 * The target event is identified via {@link #EVENT_ID}.
 */
public class OrganizerEventDetails extends AppCompatActivity {

    /**
     * Intent extra key containing the event ID to manage.
     */
    public static final String EVENT_ID = "eventId";

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

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // commented out for testing purposes
        // eventId is currently set to "event3"
//        String eventId = getIntent().getStringExtra(EVENT_ID);
        String eventId = getIntent().getStringExtra(EVENT_ID);

        if (eventId == null) {
            finish();
            return;
        }

        findViewById(R.id.btnRoll).setOnClickListener(v -> {
            Intent it = new Intent(this, LotteryDrawActivity.class);
            it.putExtra(EVENT_ID, eventId);
            startActivity(it);
        });

        findViewById(R.id.btnCanceled).setOnClickListener(v -> {
            Intent it = new Intent(this, CanceledUsersActivity.class);
            it.putExtra(EVENT_ID, eventId);
            startActivity(it);
        });

        findViewById(R.id.btnParticipating).setOnClickListener(v -> {
            Intent it = new Intent(this, ParticipatingUsersActivity.class);
            it.putExtra(EVENT_ID, eventId);
            startActivity(it);
        });

        findViewById(R.id.btnWaitlist).setOnClickListener(v -> {
            Intent it = new Intent(this, WaitlistUsersActivity.class);
            it.putExtra(EVENT_ID, eventId);
            startActivity(it);
        });

        View mapBtn = findViewById(R.id.btnWaitlistMap);
        if (mapBtn != null) {
            mapBtn.setOnClickListener(v -> {
            });
        }
    }
}
