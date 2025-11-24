package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;
import java.util.ArrayList;

public class AdminEventManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminEventAdapter adapter;
    private ArrayList<EventItem> events;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_event_management_screen);


        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(AdminEventManagementActivity.this, MainActivity.class);
                intent.putExtra("role", "admin");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        events = new ArrayList<>();
        adapter = new AdminEventAdapter(events, this);
        recyclerView.setAdapter(adapter);

        loadEvents();
    }

    private void loadEvents() {
        db.collection("events")
                .orderBy("startAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    events.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        EventItem e = new EventItem();
                        e.id = d.getId();
                        e.title = d.getString("title");
                        e.location = d.getString("location");
                        e.imageUrl = d.getString("imageUrl");
                        e.description = d.getString("description");
                        e.maxAttendees = asInt(d.get("maxAttendees"));
                        e.attendeesCount = asInt(d.get("attendeesCount"));
                        e.waitlistLimit = asInt(d.get("waitlistLimit"));
                        e.categories = d.getString("categories");
                        e.startAt = d.getTimestamp("startAt");
                        e.endAt = d.getTimestamp("endAt");
                        e.regStartAt = d.getTimestamp("regStartAt");
                        e.regEndAt = d.getTimestamp("regEndAt");
                        events.add(e);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private int asInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            return 0;
        }
    }

    // Used by adapter
    public static class EventItem {
        String id;
        String title;
        String location;
        String imageUrl;
        String description;
        String categories;
        int maxAttendees, attendeesCount, waitlistLimit;
        Timestamp startAt, endAt, regStartAt, regEndAt;
    }
}
