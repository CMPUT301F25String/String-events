package com.example.eventspage;

import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupListView();
        setupBottomNavigation();
    }

    private void setupListView() {
        listView = findViewById(R.id.list);

        List<Event> events = new ArrayList<>();
        events.add(new Event(
                "Badminton Drop In",
                "11:00 am",
                "10 Spots Left",
                "City Centre Community Centre",
                "Scheduled"
        ));
        events.add(new Event(
                "Basketball Tournament",
                "2:00 pm",
                "5 Spots Left",
                "Downtown Sports Complex",
                "Scheduled"
        ));
        events.add(new Event(
                "Tennis Practice",
                "4:00 pm",
                "Full",
                "Tennis Club",
                "Full"
        ));
        events.add(new Event(
                "Swimming Lessons",
                "9:00 am",
                "2 Spots Left",
                "Aquatic Center",
                "Scheduled"
        ));
        events.add(new Event(
                "Yoga Class",
                "6:00 pm",
                "Canceled",
                "Wellness Studio",
                "Canceled"
        ));

        eventAdapter = new EventAdapter(this, events);
        listView.setAdapter(eventAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = events.get(position);

        });
    }

    private void setupBottomNavigation() {

    }
}