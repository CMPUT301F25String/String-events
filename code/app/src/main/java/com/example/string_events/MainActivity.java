package com.example.string_events;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private enum Screen { ADMIN_HOME, USER_HOME, NOTIFICATIONS, PROFILE }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String username;
    private String fullName;
    private String email;
    private Screen currentScreen = Screen.USER_HOME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String role = getIntent().getStringExtra("role");
        username = getIntent().getStringExtra("user");
        fullName = getIntent().getStringExtra("name");
        email = getIntent().getStringExtra("email");

        if ("admin".equalsIgnoreCase(role)) {
            show(Screen.ADMIN_HOME);
        } else {
            show(Screen.USER_HOME);
        }
    }

    private void show(Screen s) {
        currentScreen = s;
        switch (s) {
            case ADMIN_HOME:
                setContentView(R.layout.admin_dashboard);
                wireCommon();
                break;

            case USER_HOME:
                setContentView(R.layout.events_screen);
                wireCommon();
                onClick(R.id.nav_bell, () -> show(Screen.NOTIFICATIONS));
                onClick(R.id.nav_person, () -> show(Screen.PROFILE));
                loadEventsIntoList();
                break;

            case NOTIFICATIONS:
                setContentView(R.layout.notification_screen);
                wireCommon();
                onClick(R.id.btnHome, () -> show(Screen.USER_HOME));
                onClick(R.id.btnProfile, () -> show(Screen.PROFILE));
                loadNotificationsIntoRecycler();
                break;

            case PROFILE:
                setContentView(R.layout.profile_screen);
                wireCommon();
                onClick(R.id.btnNotification, () -> show(Screen.NOTIFICATIONS));
                onClick(getId("btnHome"), () -> show(Screen.USER_HOME));

                // display name and email immediately
                TextView nameTextView = findViewById(R.id.name_textView);
                TextView emailTextView = findViewById(R.id.email_textView);
                nameTextView.setText("Name: " + (fullName != null ? fullName : ""));
                emailTextView.setText("Email: " + (email != null ? email : ""));

                onClick(R.id.edit_textView, () -> {
                    Intent intent = new Intent(this, EditInformationActivity.class);
                    intent.putExtra("user", username);
                    startActivity(intent);
                });

                onClick(R.id.info_imageButton, () -> startActivity(new Intent(this, LotteryInformationActivity.class)));
                onClick(R.id.info_textView, () -> startActivity(new Intent(this, LotteryInformationActivity.class)));


                new ProfileScreen().setupProfileScreen(this, username, fullName, email);
                break;
        }
    }

    private void wireCommon() {
        onClick(R.id.btnLogout, this::logoutAndGoToSignIn);
    }

    private void onClick(int viewId, Runnable action) {
        if (viewId == 0) return;
        View v = findViewById(viewId);
        if (v != null) v.setOnClickListener(_v -> action.run());
    }

    private int getId(String name) {
        return getResources().getIdentifier(name, "id", getPackageName());
    }

    private void logoutAndGoToSignIn() {
        SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
        sp.edit().clear().apply();
        Intent i = new Intent(this, WelcomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finishAffinity();
        overridePendingTransition(0, 0);
    }

    private void loadNotificationsIntoRecycler() {
        RecyclerView rv = findViewById(R.id.notifications_recyclerView);
        if (rv == null) return;
        rv.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<Notification> data = new ArrayList<>();
        NotificationAdapter adapter = new NotificationAdapter(this, data);
        rv.setAdapter(adapter);

        db.collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    data.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        boolean selected = Boolean.TRUE.equals(d.getBoolean("selectedStatus"));
                        String name = d.getString("eventName");
                        String imageUrl = d.getString("imageUrl");
                        Uri photo = imageUrl == null || imageUrl.isEmpty() ? null : Uri.parse(imageUrl);
                        data.add(new Notification(selected, photo, name));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void loadEventsIntoList() {
        final ListView lv = findViewById(R.id.list);
        if (lv == null) return;
        final ArrayList<EventItem> data = new ArrayList<>();
        final EventAdapter adapter = new EventAdapter(data);
        lv.setAdapter(adapter);

        db.collection("events")
                .orderBy("startAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    data.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        EventItem e = new EventItem();
                        e.id = d.getId();
                        e.title = d.getString("title");
                        e.location = d.getString("location");
                        e.startAt = d.getTimestamp("startAt");
                        e.endAt = d.getTimestamp("endAt");
                        e.maxAttendees = asInt(d.get("maxAttendees"));
                        e.attendeesCount = asInt(d.get("attendeesCount"));
                        data.add(e);
                    }
                    adapter.notifyDataSetChanged();
                });

        lv.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            EventItem clicked = data.get(position);
            Intent i = new Intent(MainActivity.this, EventDetailActivity.class);
            i.putExtra("event_id", clicked.id);
            i.putExtra("user", username);
            startActivity(i);
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

    private static class EventItem {
        String id;
        String title;
        String location;
        Timestamp startAt, endAt;
        int maxAttendees, attendeesCount;
    }

    private class EventAdapter extends BaseAdapter {
        private final List<EventItem> items;
        private final DateFormat timeFmt = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());

        EventAdapter(List<EventItem> items) {
            this.items = items;
        }

        @Override public int getCount() { return items.size(); }
        @Override public EventItem getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int pos, View convertView, android.view.ViewGroup parent) {
            View v = convertView;
            Holder h;
            if (v == null) {
                v = getLayoutInflater().inflate(R.layout.item_event_card, parent, false);
                h = new Holder();
                h.imgCover = v.findViewById(R.id.img_cover);
                h.tvTitle = v.findViewById(R.id.tv_title);
                h.tvTime = v.findViewById(R.id.tv_time);
                h.tvSpots = v.findViewById(R.id.tv_spots);
                h.tvPlace = v.findViewById(R.id.tv_place);
                h.btnStatus = v.findViewById(R.id.btn_status);
                v.setTag(h);
            } else {
                h = (Holder) v.getTag();
            }

            EventItem e = getItem(pos);
            if (h.tvTitle != null) h.tvTitle.setText(e.title == null ? "" : e.title);
            if (h.tvPlace != null) h.tvPlace.setText(e.location == null ? "" : e.location);
            if (h.tvTime != null) {
                String t = (e.startAt == null) ? "" : timeFmt.format(e.startAt.toDate());
                h.tvTime.setText(t);
            }
            if (h.tvSpots != null) {
                int left = Math.max(0, e.maxAttendees - e.attendeesCount);
                h.tvSpots.setText(left + " Spots Left");
            }
            if (h.btnStatus != null) {
                long now = System.currentTimeMillis();
                long start = e.startAt == null ? Long.MAX_VALUE : e.startAt.toDate().getTime();
                long end = e.endAt == null ? Long.MAX_VALUE : e.endAt.toDate().getTime();
                CharSequence text;
                int color;
                if (now < start) {
                    text = "Scheduled";
                    color = 0xFF43C06B;
                } else if (now > end) {
                    text = "Finished";
                    color = 0xFFE45A5A;
                } else {
                    text = "In Progress";
                    color = 0xFFF1A428;
                }
                h.btnStatus.setText(text);
                h.btnStatus.setBackgroundTintList(ColorStateList.valueOf(color));
            }
            return v;
        }

        class Holder {
            ImageView imgCover;
            TextView tvTitle, tvTime, tvSpots, tvPlace;
            MaterialButton btnStatus;
        }
    }
}
