package com.example.string_events;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * RecyclerView adapter used by organizers to display a list of their events.
 * <p>
 * Each row shows:
 * <ul>
 *     <li>Event cover image</li>
 *     <li>Title and location</li>
 *     <li>Start date and time</li>
 *     <li>Organizer name</li>
 *     <li>Current status (Scheduled / In Progress / Finished)</li>
 * </ul>
 * Tapping an item opens {@link OrganizerEventOverviewScreen} for that event.
 */
public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.Holder> {

    /**
     * Events to be rendered in the RecyclerView.
     */
    private final ArrayList<OrganizerEvent> items;

    /**
     * Date/time formatter for event start time, e.g. "Nov 28, 2025 at 5:00 PM".
     */
    private final SimpleDateFormat dateTimeFmt =
            new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());

    /**
     * Creates an adapter for a given list of organizer events.
     *
     * @param items list of {@link OrganizerEvent} instances to display
     */
    OrganizerEventAdapter(ArrayList<OrganizerEvent> items) {
        this.items = items;
    }

    /**
     * Inflates the view for a single organizer event card.
     *
     * @param parent   parent view group
     * @param viewType not used (single view type)
     * @return a new {@link Holder} wrapping the inflated view
     */
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_organizer, parent, false);
        return new Holder(v);
    }

    /**
     * Binds an {@link OrganizerEvent} to the given view holder.
     * <p>
     * This method sets:
     * <ul>
     *     <li>Title and location text</li>
     *     <li>Formatted start time (or "Date TBD")</li>
     *     <li>"Organizer: &lt;name&gt;" label if available</li>
     *     <li>Status chip text and color (Scheduled / In Progress / Finished)</li>
     *     <li>Cover image loaded from a URL (or a fallback image)</li>
     * </ul>
     * It also attaches a click listener to open the event overview screen.
     *
     * @param h        view holder for the row
     * @param position adapter position of the item
     */
    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        OrganizerEvent e = items.get(position);

        if (h.tvTitle != null) {
            h.tvTitle.setText(e.title == null ? "" : e.title);
        }

        if (h.tvPlace != null) {
            h.tvPlace.setText(e.location == null ? "" : e.location);
        }

        // Display full start date and time, or "Date TBD" if missing
        if (h.tvTime != null) {
            String t = (e.startAt == null) ? "Date TBD" : dateTimeFmt.format(e.startAt.toDate());
            h.tvTime.setText(t);
        }

        // Show organizer label if creator is available
        if (h.tvOrganizer != null) {
            h.tvOrganizer.setText(
                    e.creator == null || e.creator.isEmpty()
                            ? ""
                            : "Organizer: " + e.creator
            );
        }

        // Status Logic (Scheduled / In Progress / Finished)
        // Reuse tvSpots (mapped to R.id.chipStatus) as a status chip
        if (h.tvSpots != null) {
            long now = System.currentTimeMillis();
            long start = e.startAt != null ? e.startAt.toDate().getTime() : Long.MAX_VALUE;
            long end = e.endAt != null ? e.endAt.toDate().getTime() : Long.MAX_VALUE;

            String statusText;
            int color;

            if (now < start) {
                statusText = "Scheduled";
                color = 0xFF43C06B; // Green
            } else if (now > end) {
                statusText = "Finished";
                color = 0xFFE45A5A; // Red
            } else {
                statusText = "In Progress";
                color = 0xFFF1A428; // Orange
            }

            h.tvSpots.setText(statusText);
            h.tvSpots.setBackgroundColor(color);
            h.tvSpots.setTextColor(Color.WHITE);
        }

        // Load cover image from URL on a background thread, fall back to a default icon if needed
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

        // Navigate to organizer overview screen when the card is tapped
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), OrganizerEventOverviewScreen.class);
            intent.putExtra("event_id", e.id);
            v.getContext().startActivity(intent);
        });
    }

    /**
     * @return the total number of events in the list
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * View holder for an organizer event row.
     * <p>
     * Holds references to:
     * <ul>
     *     <li>Cover image</li>
     *     <li>Title, time, and location text</li>
     *     <li>Organizer label</li>
     *     <li>Status chip (reusing tvSpots)</li>
     * </ul>
     */
    public static class Holder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvTitle, tvTime, tvSpots, tvPlace, tvOrganizer;

        /**
         * Creates a new holder for the event row.
         *
         * @param v inflated row view
         */
        public Holder(@NonNull View v) {
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
