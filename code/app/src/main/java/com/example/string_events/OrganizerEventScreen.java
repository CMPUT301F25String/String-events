package com.example.string_events;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class OrganizerEventScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.org_events_screen);

        ImageButton createEventButton = findViewById(R.id.create_event_button);

        createEventButton.setOnClickListener(view -> {
            openCreateEventScreen();
        });
    }

    public void openCreateEventScreen() {
        Context context = OrganizerEventScreen.this;
        Intent myIntent = new Intent(context, CreateEventScreen.class);
        context.startActivity(myIntent);
    }
}
