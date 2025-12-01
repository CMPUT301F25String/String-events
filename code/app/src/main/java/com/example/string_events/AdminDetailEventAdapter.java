package com.example.string_events;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * RecyclerView adapter used by the admin to display a list of event items.
 * <p>
 * Each event card includes:
 * <ul>
 *     <li>Event title</li>
 *     <li>Event location</li>
 *     <li>Event organizer</li>
 *     <li>Start time</li>
 *     <li>Status chip (Scheduled, In Progress, Finished)</li>
 *     <li>Event cover image (loaded from a URL if available)</li>
 * </ul>
 * When an event card is clicked, the admin is taken to the event detail screen.
 */
public class AdminDetailEventAdapter extends RecyclerView.Adapter<AdminDetailEventAdapter.EventViewHolder> {

    /**
     * List of event items to display.
     */
    private final ArrayList<AdminEventManagementActivity.EventItem> events;

    /**
     * Reference to the hosting AdminEventManagementActivity.
     * Used for launching new activities.
     */
    private final AdminEventManagementActivity context;

    /**
     * Formatter for displaying event start time.
     */
    private final DateFormat timeFmt = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());

    /**
     * Creates a new adapter for displaying admin event items.
     *
     * @param events  list of events to show in the RecyclerView
     * @param context activity context used to inflate layouts and launch intents
     */
    public AdminDetailEventAdapter(ArrayList<AdminEventManagementActivity.EventItem> events,
                                   AdminEventManagementActivity context) {
        this.events = events;
        this.context = context;
    }

    /**
     * Inflates the event card layout and returns a new ViewHolder.
     *
     * @param parent   the parent ViewGroup
     * @param viewType view type identifier (unused)
     * @return an EventViewHolder for the inflated layout
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_admin_event_card, parent, false);
        return new EventViewHolder(v);
    }

    /**
     * Binds event data to the views inside the ViewHolder.
     * <p>
     * This method:
     * <ul>
     *     <li>Displays title, location, organizer, and time</li>
     *     <li>Determines and displays event status (Scheduled, In Progress, Finished)</li>
     *     <li>Loads the event cover image asynchronously</li>
     *     <li>Sets a click listener to open the event detail activity</li>
     * </ul>
     *
     * @param holder   the ViewHolder to bind data to
     * @param position position of the event in the list
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        AdminEventManagementActivity.EventItem e = events.get(position);

        holder.tvTitle.setText(e.title != null ? e.title : "Untitled");
        holder.tvLocation.setText(e.location != null ? e.location : "Unknown");
        holder.tvOrganizer.setText("Organizer: " + (e.creator != null ? e.creator : "Unknown"));

        if (e.startAt != null) {
            holder.tvTime.setText(timeFmt.format(e.startAt.toDate()));
        }

        long now = System.currentTimeMillis();
        long start = e.startAt != null ? e.startAt.toDate().getTime() : Long.MAX_VALUE;
        long end = e.endAt != null ? e.endAt.toDate().getTime() : Long.MAX_VALUE;

        // Determine event status
        if (now < start) {
            holder.chipStatus.setText("Scheduled");
            holder.chipStatus.setBackgroundColor(0xFF43C06B);
        } else if (now > end) {
            holder.chipStatus.setText("Finished");
            holder.chipStatus.setBackgroundColor(0xFFE45A5A);
        } else {
            holder.chipStatus.setText("In Progress");
            holder.chipStatus.setBackgroundColor(0xFFF1A428);
        }

        // Load event cover image from URL if available
        if (e.imageUrl != null && !e.imageUrl.isEmpty()) {
            holder.imgCover.setImageResource(android.R.drawable.ic_menu_gallery);
            new Thread(() -> {
                try {
                    URL url = new URL(e.imageUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();

                    try (InputStream input = conn.getInputStream()) {
                        Bitmap bmp = BitmapFactory.decodeStream(input);

                        holder.imgCover.post(() -> {
                            if (bmp != null) {
                                holder.imgCover.setImageBitmap(bmp);
                            } else {
                                holder.imgCover.setImageResource(android.R.drawable.ic_menu_report_image);
                            }
                        });
                    }

                } catch (Exception ex) {
                    holder.imgCover.post(() ->
                            holder.imgCover.setImageResource(android.R.drawable.ic_menu_report_image));
                }
            }).start();
        } else {
            holder.imgCover.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        // Navigate to event detail screen on card tap
        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, AdminEventDetailActivity.class);
            i.putExtra("event_id", e.id);
            context.startActivity(i);
        });
    }

    /**
     * Returns the number of events in the adapter.
     *
     * @return the size of the events list
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * ViewHolder class representing the layout of each event card.
     * <p>
     * Holds references to:
     * <ul>
     *     <li>Cover image</li>
     *     <li>Event title</li>
     *     <li>Event time</li>
     *     <li>Location</li>
     *     <li>Organizer</li>
     *     <li>Status chip</li>
     * </ul>
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {

        ImageView imgCover;
        TextView tvTitle, tvTime, tvLocation, tvOrganizer, chipStatus;

        /**
         * Creates a ViewHolder and binds the view references.
         *
         * @param itemView the root view of the event card layout
         */
        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvOrganizer = itemView.findViewById(R.id.tvOrganizer);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }
    }
}
