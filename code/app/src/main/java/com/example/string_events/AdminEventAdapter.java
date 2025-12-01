package com.example.string_events;

import android.content.Context;
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

import com.google.firebase.Timestamp;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying a list of events in the admin interface.
 * <p>
 * Each card shows:
 * <ul>
 *     <li>Event title</li>
 *     <li>Location</li>
 *     <li>Organizer</li>
 *     <li>Start time (formatted)</li>
 *     <li>Event status (Scheduled, InProgress, Finished)</li>
 *     <li>Cover image loaded from a remote URL if available</li>
 * </ul>
 * When the admin taps an event card, they are navigated to the event detail screen.
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.ViewHolder> {

    /**
     * List of event items to be displayed.
     */
    private final ArrayList<AdminEventManagementActivity.EventItem> events;

    /**
     * Context used to inflate layouts and start activities.
     */
    private final Context context;

    /**
     * Creates an adapter for admin event cards.
     *
     * @param events  list of events to display
     * @param context context used for inflating views and launching activities
     */
    public AdminEventAdapter(ArrayList<AdminEventManagementActivity.EventItem> events, Context context) {
        this.events = events;
        this.context = context;
    }

    /**
     * Inflates the event card layout and creates a new {@link ViewHolder}.
     *
     * @param parent   the parent ViewGroup into which the new view will be added
     * @param viewType the view type of the new view (unused here)
     * @return a new {@link ViewHolder} for an event card
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_admin_event_card, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds event data to the given {@link ViewHolder}.
     * <p>
     * This method:
     * <ul>
     *     <li>Sets the title, location, organizer and formatted start time</li>
     *     <li>Loads the cover image asynchronously from a URL if available</li>
     *     <li>Determines and displays the event status based on current time</li>
     *     <li>Attaches a click listener to open the event detail activity</li>
     * </ul>
     *
     * @param holder   the {@link ViewHolder} to bind data to
     * @param position position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminEventManagementActivity.EventItem e = events.get(position);

        // Basic text fields with fallbacks
        holder.tvTitle.setText(e.title != null ? e.title : "(No Title)");
        holder.tvLocation.setText(e.location != null ? e.location : "(No Location)");
        holder.tvOrganizer.setText("Organizer: " + (e.creator != null ? e.creator : "Unknown"));

        // Format and display the start time if available, otherwise show a placeholder
        if (e.startAt != null) {
            // Example format: "Nov 28, 2025 at 4:30 PM"
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
            holder.tvTime.setText(sdf.format(e.startAt.toDate()));
        } else {
            holder.tvTime.setText("Date TBD");
        }

        // Load cover image from URL in a background thread
        if (e.imageUrl != null && !e.imageUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    URL url = new URL(e.imageUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream input = conn.getInputStream();
                    Bitmap bmp = BitmapFactory.decodeStream(input);
                    holder.imgCover.post(() -> holder.imgCover.setImageBitmap(bmp));
                } catch (Exception ex) {
                    holder.imgCover.post(() ->
                            holder.imgCover.setImageResource(android.R.drawable.ic_menu_report_image));
                }
            }).start();
        } else {
            holder.imgCover.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        // Determine and display event status based on current time
        long now = System.currentTimeMillis();
        long start = e.startAt != null ? e.startAt.toDate().getTime() : Long.MAX_VALUE;
        long end = e.endAt != null ? e.endAt.toDate().getTime() : Long.MAX_VALUE;

        if (now < start) {
            holder.tvStatus.setText("Scheduled");
            holder.tvStatus.setBackgroundColor(0xFF43C06B);
        } else if (now > end) {
            holder.tvStatus.setText("Finished");
            holder.tvStatus.setBackgroundColor(0xFFE45A5A);
        } else {
            holder.tvStatus.setText("InProgress");
            holder.tvStatus.setBackgroundColor(0xFFF1A428);
        }

        // Open event detail screen when the card is tapped
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminEventDetailActivity.class);
            intent.putExtra("event_id", e.id);
            context.startActivity(intent);
        });
    }

    /**
     * Returns the total number of events in the adapter.
     *
     * @return number of events
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * ViewHolder representing a single admin event card.
     * <p>
     * Holds references to:
     * <ul>
     *     <li>Cover image</li>
     *     <li>Title</li>
     *     <li>Time</li>
     *     <li>Location</li>
     *     <li>Organizer</li>
     *     <li>Status label</li>
     * </ul>
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvTitle, tvTime, tvLocation, tvOrganizer, tvStatus;

        /**
         * Creates a new ViewHolder and binds the UI components.
         *
         * @param itemView root view of the event card layout
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvOrganizer = itemView.findViewById(R.id.tvOrganizer);
            tvStatus = itemView.findViewById(R.id.chipStatus);
        }
    }
}
