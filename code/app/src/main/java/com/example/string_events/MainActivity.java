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

/**
 * Main screen for entrants showing the list of available events.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Displays a list of upcoming public events.</li>
 *     <li>Allows users to apply filters by tags and date range.</li>
 *     <li>Handles navigation to notifications, profile, QR scanner, and event details.</li>
 *     <li>Starts/stops the background {@link NotificationService} based on login status.</li>
 *     <li>Handles deep links that open a specific event.</li>
 * </ul>
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Available navigation targets from this screen.
     */
    private enum Screen { ADMIN_HOME, USER_HOME, NOTIFICATIONS, PROFILE }

    /**
     * Firestore instance used to load events and related data.
     */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Currently selected category tags used for filtering events.
     */
    private final HashSet<String> selectedTags = new HashSet<>();

    /**
     * Optional start of the date-time filter range (inclusive).
     */
    private ZonedDateTime filterStart = null;

    /**
     * Optional end of the date-time filter range (inclusive).
     */
    private ZonedDateTime filterEnd = null;

    /**
     * TextView that displays a summary of the currently applied filters,
     * such as selected tags and date range.
     */
    private TextView tvFilterHint;

    /**
     * Activity result launcher used to receive filters from {@link EventFilterActivity}.
     * <p>
     * When the filter activity returns:
     * <ul>
     *     <li>Updates {@link #selectedTags}, {@link #filterStart}, and {@link #filterEnd}.</li>
     *     <li>Updates the filter hint TextView visibility and text.</li>
     *     <li>Rebuilds and executes a Firestore query via {@link #createFilterQuery(HashSet, ZonedDateTime, ZonedDateTime)}.</li>
     * </ul>
     */
    private final ActivityResultLauncher<Intent> filterLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    selectedTags.clear();
                    String[] arr = data.getStringArrayExtra(EventFilterActivity.EXTRA_TAGS);
                    if (arr != null) java.util.Collections.addAll(selectedTags, arr);

                    filterStart = null;
                    filterEnd = null;
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
                // Rebuild query with updated filters after returning from filter screen
                createFilterQuery(selectedTags, filterStart, filterEnd);
            });

    /**
     * Called when the activity is first created.
     * <p>
     * This method:
     * <ul>
     *     <li>Sets up the initial layout and common navigation handlers.</li>
     *     <li>Starts the {@link NotificationService} if the user opted in.</li>
     *     <li>Configures the filter button and result launcher.</li>
     *     <li>Handles deep link events that may open a specific event detail screen.</li>
     *     <li>Sets up navigation to notifications, profile, and QR scanner.</li>
     *     <li>Loads visible events into the list view.</li>
     * </ul>
     *
     * @param savedInstanceState previously saved state, or {@code null} if created fresh
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_screen);
        wireCommon();

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String username = sharedPreferences.getString("user", null);

        // Notification service toggle based on stored preference
        boolean isNotifEnabled = sharedPreferences.getBoolean("notifications_enabled", true);

        if (username != null && isNotifEnabled) {
            Intent serviceIntent = new Intent(this, NotificationService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }

        tvFilterHint = findViewById(R.id.tv_filter_hint);
        TextView btnOpenFilter = findViewById(R.id.btn_open_filter);

        // Open filter screen to adjust tags and date range
        btnOpenFilter.setOnClickListener(v -> {
            Intent it = new Intent(this, EventFilterActivity.class);
            it.putExtra(EventFilterActivity.EXTRA_TAGS, selectedTags.toArray(new String[0]));
            it.putExtra("action", "filter");
            filterLauncher.launch(it);
        });

        // Handle direct deep-link to an event (e.g. from QR code URL)
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

        // Bottom navigation actions
        onClick(R.id.nav_bell, () -> show(Screen.NOTIFICATIONS));
        onClick(R.id.nav_person, () -> show(Screen.PROFILE));
        onClick(R.id.btnCamera, () -> {
            Intent intent = new Intent(MainActivity.this, QrScanActivity.class);
            startActivity(intent);
        });

        // Initial load of events with no filters applied
        loadEventsIntoList(db.collection("events"));
    }

    /**
     * Handles navigation between different high-level screens.
     *
     * @param s target screen to display
     */
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
            default:
                // Other cases (ADMIN_HOME, USER_HOME) are not used from this activity
                break;
        }
    }

    /**
     * Wires common UI actions that should be available on this screen,
     * such as the logout button.
     */
    private void wireCommon() {
        onClick(R.id.btnLogout, this::logoutAndGoToSignIn);
    }

    /**
     * Utility method to attach a click listener to a view by ID, if present.
     *
     * @param viewId ID of the view in the current layout
     * @param action action to run when the view is clicked
     */
    private void onClick(int viewId, Runnable action) {
        if (viewId == 0) return;
        View v = findViewById(viewId);
        if (v != null) v.setOnClickListener(_v -> action.run());
    }

    /**
     * Clears user session data, stops the notification service, and navigates
     * back to the welcome/sign-in screen.
     */
    private void logoutAndGoToSignIn() {
        // Stop the service when logging out
        Intent serviceIntent = new Intent(this, NotificationService.class);
        stopService(serviceIntent);

        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        sp.edit().clear().apply();

        Intent i = new Intent(this, WelcomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finishAffinity();
        overridePendingTransition(0, 0);
    }

    /**
     * Loads events from Firestore into the list view based on the given query.
     * <p>
     * This method:
     * <ul>
     *     <li>Creates an {@link EventAdapter} and attaches it to the {@link ListView}.</li>
     *     <li>Orders results by {@code startAt} ascending.</li>
     *     <li>Filters out events that are not visible or have already ended.</li>
     *     <li>Maps Firestore documents into {@link EventItem} instances.</li>
     *     <li>Handles item clicks by opening {@link EventDetailActivity}.</li>
     * </ul>
     *
     * @param query base Firestore query to fetch events
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
                    Timestamp now = Timestamp.now();

                    for (QueryDocumentSnapshot d : snap) {
                        Boolean visibility = d.getBoolean("visibility");
                        if (Boolean.FALSE.equals(visibility)) continue;

                        Timestamp endAt = d.getTimestamp("endAt");
                        // Skip events that finished in the past
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

    /**
     * Builds and applies a Firestore query based on the given filters.
     * <p>
     * Filters include:
     * <ul>
     *     <li>Categories (tags) using {@code whereArrayContainsAny("categories", tagsList)}</li>
     *     <li>Start time greater than or equal to {@code start}</li>
     *     <li>End time less than or equal to {@code end}</li>
     * </ul>
     * After building the query, it calls {@link #loadEventsIntoList(Query)}.
     *
     * @param tags  set of category tags to filter on (may be empty)
     * @param start optional start date-time bound, or {@code null} for no bound
     * @param end   optional end date-time bound, or {@code null} for no bound
     */
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

    /**
     * Simple data holder for an event row displayed in the list view.
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
     * List adapter that binds {@link EventItem} objects to {@code item_event} rows.
     * <p>
     * Each row displays:
     * <ul>
     *     <li>Cover image (or a fallback image if none is available)</li>
     *     <li>Title and location</li>
     *     <li>Start time</li>
     *     <li>Number of spots left</li>
     *     <li>Status (Scheduled / In Progress / Finished) with color coding</li>
     * </ul>
     */
    private class EventAdapter extends BaseAdapter {
        private final List<EventItem> items;

        /**
         * Date-time formatter for displaying event start time.
         * <p>
         * Example: {@code Nov 28, 2025 at 4:30 PM}
         */
        private final SimpleDateFormat dateTimeFmt =
                new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());

        /**
         * Creates a new adapter with the given backing list.
         *
         * @param items list of events to display
         */
        EventAdapter(List<EventItem> items) {
            this.items = items;
        }

        @Override
        public int getCount() { return items.size(); }

        @Override
        public EventItem getItem(int position) { return items.get(position); }

        @Override
        public long getItemId(int position) { return position; }

        /**
         * Inflates and binds a single event row.
         *
         * @param pos         position of the item in the list
         * @param convertView recycled view, if available
         * @param parent      parent view group
         * @return the populated row view
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

            // Cover image
            if (e.imageUrl == null) {
                h.imgCover.setImageResource(R.drawable.no_image_available);
            } else {
                Glide.with(MainActivity.this)
                        .load(e.imageUrl)
                        .into(h.imgCover);
            }

            // Title and place
            if (h.tvTitle != null) h.tvTitle.setText(e.title == null ? "" : e.title);
            if (h.tvPlace != null) h.tvPlace.setText(e.location == null ? "" : e.location);

            // Start time
            if (h.tvTime != null) {
                String t = (e.startAt == null) ? "" : dateTimeFmt.format(e.startAt.toDate());
                h.tvTime.setText(t);
            }

            // Spots left
            if (h.tvSpots != null) {
                int left = Math.max(0, e.maxAttendees - e.attendeesCount);
                h.tvSpots.setText(left + " Spots Left");
            }

            // Status label with color coding
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

        /**
         * ViewHolder pattern for efficient row view reuse.
         */
        class Holder {
            ImageView imgCover;
            TextView tvTitle, tvTime, tvSpots, tvPlace;
            MaterialButton btnStatus;
        }
    }
}
