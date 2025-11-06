package com.example.string_events;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminEventManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_event_management_screen);

        ImageButton back = findViewById(R.id.btn_back);
        if (back != null) back.setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rv_admin_events);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<AdminEvent> data = new ArrayList<>();
        // 1) 黄 In Progress
        data.add(new AdminEvent(
                getString(R.string.sample_event_title),
                getString(R.string.sample_time),
                getString(R.string.sample_location),
                getString(R.string.sample_organizer),
                AdminEvent.Status.IN_PROGRESS,
                R.drawable.sample_event,
                R.drawable.community_centre_image
        ));
        // 2) 绿 Scheduled
        data.add(new AdminEvent(
                getString(R.string.sample_event_title),
                getString(R.string.sample_time),
                getString(R.string.sample_location),
                getString(R.string.sample_organizer),
                AdminEvent.Status.SCHEDULED,
                R.drawable.sample_event,
                R.drawable.community_centre_image
        ));
        // 3) 红 Finished
        data.add(new AdminEvent(
                getString(R.string.sample_event_title),
                getString(R.string.sample_time),
                getString(R.string.sample_location),
                getString(R.string.sample_organizer),
                AdminEvent.Status.FINISHED,
                R.drawable.sample_event,
                R.drawable.community_centre_image
        ));

        rv.setAdapter(new AdminEventAdapter(data));
    }
}
