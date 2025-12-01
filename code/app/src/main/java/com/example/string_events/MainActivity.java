package com.example.string_events;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private enum Screen { ADMIN_HOME, USER_HOME, NOTIFICATIONS, PROFILE }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final HashSet<String> selectedTags = new HashSet<>();
    private ZonedDateTime filterStart = null, filterEnd = null;
    private TextView tvFilterHint;

    private final ActivityResultLauncher<Intent> filterLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    selectedTags.clear();
                    String[] arr = data.getStringArrayExtra(EventFilterActivity.EXTRA_TAGS);
                    if (arr != null) java.util.Collections.addAll(selectedTags, arr);

                    filterStart = null; filterEnd = null;
                    if (data.hasExtra(EventFilterActivity.EXTRA_START_MS)) {
                        long ms = data.getLongExtra(EventFilterActivity.EXTRA_START_MS, 0L);
                        filterStart = java.time.ZonedDateTime.ofInstant(
                                java.time.Instant.ofEpochMilli(ms), java.time.ZoneId.systemDefault());
                    }
                    if (data.hasExtra(EventFilterActivity.EXTRA_END_MS)) {
                        long me = data.getLongExtra(EventFilterActivity.EXTRA_END_MS, 0L);
                        filterEnd = java.time.ZonedDateTime.ofInstant(
                                java.time.Instant.ofEpochMilli(me), java.time.ZoneId.systemDefault());
                    }

                    boolean hasFilters = !selectedTags.isEmpty() || filterStart != null || filterEnd != null;

                    if (tvFilterHint != null) {
                        if (hasFilters) {
                            StringBuilder displayText = new StringBuilder();
                            if (!selectedTags.isEmpty()) {
                                displayText.append(android.text.TextUtils.join(", ", selectedTags));
                            }
                            if (filterStart != null || filterEnd != null) {
                                if (displayText.length() > 0) displayText.append(" + ");
                                displayText.append("Date Range");
                            }
                            tvFilterHint.setText(displayText.toString());
                            tvFilterHint.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "Filters applied", Toast.LENGTH_SHORT).show();
                        } else {
                            tvFilterHint.setVisibility(View.GONE);
                            Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                createFilterQuery(selectedTags, filterStart, filterEnd);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_screen);
        wireCommon();


        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String username = sharedPreferences.getString("user", null);


        boolean isNotifEnabled = sharedPreferences.getBoolean("notifications_enabled", true);

        if (username != null && isNotifEnabled && !NotificationServiceHelper.isNotificationServiceRunning) {
            Intent serviceIntent = new Intent(this, NotificationService.class);
            startForegroundService(serviceIntent);
            // set the static flag to true so this won't run again
            NotificationServiceHelper.isNotificationServiceRunning = true;
        }

        tvFilterHint = findViewById(R.id.tv_filter_hint);
        TextView btnOpenFilter = findViewById(R.id.btn_open_filter);

        btnOpenFilter.setOnClickListener(v -> {
            Intent it = new Intent(this, EventFilterActivity.class);
            it.putExtra(EventFilterActivity.EXTRA_TAGS, selectedTags.toArray(new String[0]));
            it.putExtra("action", "filter");
            filterLauncher.launch(it);
        });

        Intent launchIntent = getIntent();
        if (Intent.ACTION_VIEW.equals(launchIntent.getAction()) && launchIntent.getData() != null) {
            String eventIdFromLink = launchIntent.getData().getLastPathSegment();
            if (eventIdFromLink == null || eventIdFromLink.isEmpty()) {
                return;
            }
            Intent detailIntent = new Intent(this, EventDetailActivity.class);
            detailIntent.putExtra("event_id", eventIdFromLink);
            startActivity(detailIntent);
            finish();
            return;
        }

        onClick(R.id.nav_bell, () -> show(Screen.NOTIFICATIONS));
        onClick(R.id.nav_person, () -> show(Screen.PROFILE));
        onClick(R.id.btnCamera, () -> {
            Intent intent = new Intent(MainActivity.this, QrScanActivity.class);
            startActivity(intent);
        });
        loadEventsIntoList(db.collection("events"));
    }

    private void show(Screen s) {
        switch (s) {
            case NOTIFICATIONS:
                wireCommon();
                startActivity(new Intent(this, NotificationScreen.class));
                break;
            case PROFILE:
                wireCommon();
                Intent profileIntent = new Intent(this, ProfileScreen.class);
                startActivity(profileIntent);
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

    private void logoutAndGoToSignIn() {
        // stop the service when logging out
        Intent serviceIntent = new Intent(this, NotificationService.class);
        stopService(serviceIntent);

        // reset the static flag on logout because it doesn't kill the app process
        NotificationServiceHelper.isNotificationServiceRunning = false;

        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        sp.edit().clear().apply();
        Intent i = new Intent(this, WelcomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finishAffinity();
        overridePendingTransition(0, 0);
    }

    private void loadEventsIntoList(Query query) {
        final ListView lv = findViewById(R.id.list);
        if (lv == null) return;
        final ArrayList<EventItem> data = new ArrayList<>();
        final EventAdapter adapter = new EventAdapter(MainActivity.this, data);
        lv.setAdapter(adapter);

        query.orderBy("startAt", Query.Direction.ASCENDING).get()
                .addOnSuccessListener(snap -> {
                    data.clear();
                    Timestamp now = Timestamp.now();

                    for (QueryDocumentSnapshot d : snap) {
                        Boolean visibility = d.getBoolean("visibility");
                        if (Boolean.FALSE.equals(visibility)) continue;

                        Timestamp endAt = d.getTimestamp("endAt");
                        if (endAt != null && endAt.compareTo(now) < 0) continue;

                        EventItem e = new EventItem();
                        String imageUrl = d.getString("imageUrl");
                        if (imageUrl != null) e.imageUrl = imageUrl;
                        e.id = d.getId();
                        e.title = d.getString("title");
                        e.location = d.getString("location");
                        e.startAt = d.getTimestamp("startAt");
                        e.endAt = endAt;
                        e.maxAttendees = Integer.parseInt(String.valueOf(d.get("maxAttendees")));
                        ArrayList<String> attendees = (ArrayList<String>) d.get("attendees");
                        assert attendees != null;
                        e.attendeesCount = attendees.size();
                        data.add(e);
                    }
                    adapter.notifyDataSetChanged();
                });

        lv.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            EventItem clicked = data.get(position);
            Intent i = new Intent(MainActivity.this, EventDetailActivity.class);
            i.putExtra("event_id", clicked.id);
            startActivity(i);
        });
    }

    private void createFilterQuery(HashSet<String> tags, ZonedDateTime start, ZonedDateTime end) {
        Query query = db.collection("events");
        ArrayList<String> tagsList = new ArrayList<>(tags);
        if (!tags.isEmpty()) {
            query = query.whereArrayContainsAny("categories", tagsList);
        }
        if (start != null) {
            query = query.whereGreaterThanOrEqualTo("startAt", new Timestamp(Date.from(start.toInstant())));
        }
        if (end != null) {
            query = query.whereLessThanOrEqualTo("endAt", new Timestamp(Date.from(end.toInstant())));
        }
        loadEventsIntoList(query);
    }
}