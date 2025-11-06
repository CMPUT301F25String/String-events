package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class OrganizerEventDetails extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "extra_event_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.org_event_details);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        String eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);

//        findViewById(R.id.btnRoll).setOnClickListener(v -> {
//            Intent it = new Intent(this, LotteryActivity.class);
//            it.putExtra(EXTRA_EVENT_ID, eventId);
//            startActivity(it);
//        });

        findViewById(R.id.btnCanceled).setOnClickListener(v -> {
            Intent it = new Intent(this, CanceledUsersActivity.class);
            it.putExtra(EXTRA_EVENT_ID, eventId);
            startActivity(it);
        });

        findViewById(R.id.btnParticipating).setOnClickListener(v -> {
            Intent it = new Intent(this, ParticipatingUsersActivity.class);
            it.putExtra(EXTRA_EVENT_ID, eventId);
            startActivity(it);
        });

        findViewById(R.id.btnWaitlist).setOnClickListener(v -> {
            Intent it = new Intent(this, WaitlistUsersActivity.class);
            it.putExtra(EXTRA_EVENT_ID, eventId);
            startActivity(it);
        });

        View mapBtn = findViewById(R.id.btnWaitlistMap);
        if (mapBtn != null) {
            mapBtn.setOnClickListener(v -> {
            });
        }
    }
}
