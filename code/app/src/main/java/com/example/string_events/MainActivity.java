package com.example.string_events;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Entry activity that routes users to different app sections depending on role.
 * <p>
 * Supported screens:
 * <ul>
 *   <li>ADMIN_HOME – admin dashboard</li>
 *   <li>USER_HOME – public events list</li>
 *   <li>NOTIFICATIONS – notifications screen</li>
 *   <li>PROFILE – profile screen</li>
 * </ul>
 */
public class MainActivity extends AppCompatActivity {
    /** App sections this activity can display. */
    private enum Screen { ADMIN_HOME, USER_HOME, NOTIFICATIONS, PROFILE }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final HashSet<String> selectedTags = new HashSet<>();
    private ZonedDateTime filterStart = null, filterEnd = null;

    private TextView tvFilterHint;

    private final ActivityResultLauncher<Intent> filterLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    android.content.Intent data = result.getData();

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

                    if (tvFilterHint != null) {
                        tvFilterHint.setText(selectedTags.isEmpty() && filterStart == null && filterEnd == null
                                ? " " : "showing filtered results");
                        tvFilterHint.setVisibility(android.view.View.VISIBLE);
                    } else {
                        android.widget.Toast.makeText(this, "Filters applied", android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
                createFilterQuery(selectedTags, filterStart, filterEnd);
            });

    /**
     * Reads role/user extras, persists basic user info to {@code SharedPreferences},
     * and navigates to admin or user home accordingly.
     *
     * @param savedInstanceState previously saved state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_screen);
        wireCommon();

        tvFilterHint = findViewById(R.id.tv_filter_hint);
        TextView btnOpenFilter = findViewById(R.id.btn_open_filter);

        btnOpenFilter.setOnClickListener(v -> {
            Intent it = new Intent(this, EventFilterActivity.class);
            it.putExtra(EventFilterActivity.EXTRA_TAGS, selectedTags.toArray(new String[0]));
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

    /**
     * Switches the UI to the requested section and wires section-specific handlers.
     *
     * @param s target screen
     */
    private void show(Screen s) {
        switch (s) {
//            case USER_HOME:
//                setContentView(R.layout.events_screen);
//                wireCommon();
//                onClick(R.id.nav_bell, () -> show(Screen.NOTIFICATIONS));
//                onClick(R.id.nav_person, () -> show(Screen.PROFILE));
//                loadEventsIntoList();
//                break;

            case NOTIFICATIONS:
                // open notifications screen as a separate activity
                wireCommon();
                startActivity(new Intent(this, NotificationScreen.class));
                break;

            case PROFILE:
                wireCommon();
                // open profile screen as a separate activity
                Intent profileIntent = new Intent(this, ProfileScreen.class);
                startActivity(profileIntent);
                break;

//            case ADMIN_HOME:
//                setContentView(R.layout.admin_dashboard);
//                wireCommon();
//
//                onClick(R.id.btnEvents, () -> {
//                    Intent intent = new Intent(this, AdminEventManagementActivity.class);
//                    startActivity(intent);
//                });
//
//                onClick(R.id.btnImages, () -> {
//                    Intent intent = new Intent(this, AdminImageManagementActivity.class);
//                    startActivity(intent);
//                });
//
//                onClick(R.id.btnProfiles, () -> {
//                    startActivity(new Intent(this, AdminProfileManagementActivity.class));
//                });
//
//                break;
        }
    }

    /**
     * Wires common actions shared across screens (e.g. logout).
     */
    private void wireCommon() {
        onClick(R.id.btnLogout, this::logoutAndGoToSignIn);
    }

    /**
     * Helper to set an onClick listener if the view exists.
     *
     * @param viewId resource ID of the view
     * @param action runnable to execute on click
     */
    private void onClick(int viewId, Runnable action) {
        if (viewId == 0) return;
        View v = findViewById(viewId);
        if (v != null) v.setOnClickListener(_v -> action.run());
    }

    /**
     * Clears auth state and navigates back to the welcome/sign-in screen.
     */
    private void logoutAndGoToSignIn() {
        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        sp.edit().clear().apply();
        Intent i = new Intent(this, WelcomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finishAffinity();
        overridePendingTransition(0, 0);
    }

    /**
     * Loads events from Firestore, binds them to the ListView, and opens
     * {@link EventDetailActivity} on item click.
     */
    private void loadEventsIntoList(Query query) {
        final ListView lv = findViewById(R.id.list);
        if (lv == null) return;
        final ArrayList<EventItem> data = new ArrayList<>();
        final EventAdapter adapter = new EventAdapter(data);
        lv.setAdapter(adapter);

        query.orderBy("startAt", Query.Direction.ASCENDING).get()
                .addOnSuccessListener(snap -> {
                    data.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        EventItem e = new EventItem();
                        String imageUrl = d.getString("imageUrl");
                        if (imageUrl != null) {
                            e.imageUrl = imageUrl;
                        }
                        e.id = d.getId();
                        e.title = d.getString("title");
                        e.location = d.getString("location");
                        e.startAt = d.getTimestamp("startAt");
                        e.endAt = d.getTimestamp("endAt");
                        e.maxAttendees = Integer.parseInt(String.valueOf(d.get("maxAttendees")));
                        e.attendeesCount = Integer.parseInt(String.valueOf(d.get("attendeesCount")));
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
            query = query.whereArrayContainsAny("tags", tagsList);
        }
        if (start != null) {
            query = query.whereGreaterThanOrEqualTo("startAt", new Timestamp(Date.from(start.toInstant())));
        }
        if (end != null) {
            query = query.whereLessThanOrEqualTo("endAt", new Timestamp(Date.from(end.toInstant())));
        }
        loadEventsIntoList(query);
    }

    /**
     * Lightweight container used to present event summaries in the list.
     */
    private static class EventItem {
        String imageUrl;
        String id;
        String title;
        String location;
        Timestamp startAt, endAt;
        int maxAttendees, attendeesCount;
    }

    /**
     * Adapter that renders {@link EventItem} rows for the events list.
     */
    private class EventAdapter extends BaseAdapter {
        private final List<EventItem> items;
        private final DateFormat timeFmt = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());

        /**
         * Creates an adapter over a mutable list of items.
         *
         * @param items backing data list
         */
        EventAdapter(List<EventItem> items) {
            this.items = items;
        }

        /** @return number of rows. */
        @Override public int getCount() { return items.size(); }
        /** @return item at position. */
        @Override public EventItem getItem(int position) { return items.get(position); }
        /** @return stable ID (here: position). */
        @Override public long getItemId(int position) { return position; }

        /**
         * Inflates/binds an event row view with cover image, title, place, time,
         * remaining spots, and a status chip (Scheduled/In Progress/Finished).
         */
        @Override
        public View getView(int pos, View convertView, android.view.ViewGroup parent) {
            View v = convertView;
            Holder h;
            if (v == null) {
                v = getLayoutInflater().inflate(R.layout.item_event, parent, false);
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
            // no image uploaded for event
            if (e.imageUrl == null) {
                h.imgCover.setImageResource(R.drawable.no_image_available);
            }
            // image uploaded for event and retrieved from database successfully
            else {
                Glide.with(MainActivity.this)
                        .load(e.imageUrl)
                        .into(h.imgCover);
            }
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

        /** View holder for an event row. */
        class Holder {
            ImageView imgCover;
            TextView tvTitle, tvTime, tvSpots, tvPlace;
            MaterialButton btnStatus;
        }
    }
}
