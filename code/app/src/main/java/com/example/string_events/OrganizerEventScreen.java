package com.example.string_events;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Organizer entry screen listing organizer actions and events.
 * Shows events created by current user and lets them create new events.
 */
public class OrganizerEventScreen extends AppCompatActivity {

    private RecyclerView rvEvents;
    private OrganizerEventAdapter adapter;
    private final ArrayList<OrganizerEventItem> data = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.org_events_screen);

        ImageButton createEventButton = findViewById(R.id.create_event_button);
        createEventButton.setOnClickListener(view -> openCreateEventScreen());

        rvEvents = findViewById(R.id.recyclerEvents);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrganizerEventAdapter(data);
        rvEvents.setAdapter(adapter);

        loadMyEvents();
    }

    /**
     * Launches the screen for creating a new event.
     */
    public void openCreateEventScreen() {
        Context context = OrganizerEventScreen.this;
        Intent myIntent = new Intent(context, CreateEventScreen.class);
        context.startActivity(myIntent);
    }

    /**
     * Loads events from Firestore where creator == currently logged in user.
     */
    private void loadMyEvents() {
        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        String currentUser = sp.getString("user", null);

        if (currentUser == null || currentUser.trim().isEmpty()) {
            Log.w("ORG_EVENTS", "No logged in user in SharedPreferences (userInfo.user).");
            data.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        db.collection("events")
                .whereEqualTo("creator", currentUser)
                .get()
                .addOnSuccessListener(snap -> {
                    data.clear();

                    if (snap.isEmpty()) {
                        Log.d("ORG_EVENTS", "No events found for creator: " + currentUser);
                    } else {
                        Log.d("ORG_EVENTS", "Found " + snap.size() + " events for " + currentUser);
                    }

                    for (QueryDocumentSnapshot d : snap) {
                        OrganizerEventItem e = new OrganizerEventItem();
                        e.id = d.getId();
                        e.title = d.getString("title");
                        e.location = d.getString("location");
                        e.startAt = d.getTimestamp("startAt");
                        e.maxAttendees = asInt(d.get("maxAttendees"));
                        e.attendeesCount = asInt(d.get("attendeesCount"));
                        e.imageUrl = d.getString("imageUrl");
                        e.creator = d.getString("creator");
                        data.add(e);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e("ORG_EVENTS", "Error loading events: ", e));
    }

    private int asInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception ex) {
            return 0;
        }
    }

    static class OrganizerEventItem {
        String id;
        String title;
        String location;
        Timestamp startAt;
        int maxAttendees;
        int attendeesCount;
        String imageUrl;
        String creator;
    }

    private class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.Holder> {

        private final ArrayList<OrganizerEventItem> items;
        private final DateFormat timeFmt =
                DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());

        OrganizerEventAdapter(ArrayList<OrganizerEventItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_event_organizer, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            OrganizerEventItem e = items.get(position);

            if (h.tvTitle != null) {
                h.tvTitle.setText(e.title == null ? "" : e.title);
            }

            if (h.tvPlace != null) {
                h.tvPlace.setText(e.location == null ? "" : e.location);
            }

            if (h.tvTime != null) {
                String t = (e.startAt == null) ? "" : timeFmt.format(e.startAt.toDate());
                h.tvTime.setText(t);
            }

            if (h.tvOrganizer != null) {
                h.tvOrganizer.setText(
                        e.creator == null || e.creator.isEmpty()
                                ? ""
                                : "Organizer: " + e.creator
                );
            }

            if (h.tvSpots != null) {
                int count = Math.max(0, e.attendeesCount);
                int left = Math.max(0, e.maxAttendees - count);
                h.tvSpots.setText(left + " Spots Left");
            }

            if (e.imageUrl != null && !e.imageUrl.isEmpty()) {
                new Thread(() -> {
                    try {
                        URL url = new URL(e.imageUrl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true);
                        conn.connect();
                        InputStream input = conn.getInputStream();
                        Bitmap bmp = BitmapFactory.decodeStream(input);
                        h.imgCover.post(() -> h.imgCover.setImageBitmap(bmp));
                    } catch (Exception ex) {
                        h.imgCover.post(() ->
                                h.imgCover.setImageResource(android.R.drawable.ic_menu_report_image));
                    }
                }).start();
            } else {
                h.imgCover.setImageResource(android.R.drawable.ic_menu_report_image);
            }

            h.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), EventOverviewScreen.class);
                intent.putExtra("event_id", e.id);
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            ImageView imgCover;
            TextView tvTitle, tvTime, tvSpots, tvPlace, tvOrganizer;

            Holder(@NonNull View v) {
                super(v);
                imgCover    = v.findViewById(R.id.imgCover);
                tvTitle     = v.findViewById(R.id.tvTitle);
                tvTime      = v.findViewById(R.id.tvTime);
                tvPlace     = v.findViewById(R.id.tvLocation);
                tvOrganizer = v.findViewById(R.id.tvOrganizer);
                tvSpots     = v.findViewById(R.id.chipStatus);
            }
        }
    }
}
