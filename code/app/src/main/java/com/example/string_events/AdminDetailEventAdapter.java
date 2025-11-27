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
 * RecyclerView adapter for rendering a list of admin-visible event cards.
 * <p>
 * Each row shows title, location, organizer label, a human-readable start time,
 * a status "chip" (Scheduled / In Progress / Finished), and an optional cover image
 * fetched from a Firebase Storage URL.
 * Tapping a card opens {@link AdminEventDetailActivity} for the selected event.
 *
 * @since 1.0
 */
public class AdminDetailEventAdapter extends RecyclerView.Adapter<AdminDetailEventAdapter.EventViewHolder> {

    private final ArrayList<AdminEventManagementActivity.EventItem> events;
    private final AdminEventManagementActivity context;
    private final DateFormat timeFmt = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());

    /**
     * Creates an adapter for admin event details.
     *
     * @param events  data source of events to display (non-null)
     * @param context hosting activity used for inflating views and starting intents
     */
    public AdminDetailEventAdapter(ArrayList<AdminEventManagementActivity.EventItem> events, AdminEventManagementActivity context) {
        this.events = events;
        this.context = context;
    }

    /**
     * Inflates the event card layout.
     *
     * @param parent   RecyclerView parent
     * @param viewType unused single view type
     * @return a new {@link EventViewHolder}
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_admin_event_card, parent, false);
        return new EventViewHolder(v);
    }

    /**
     * Binds an event item to the row views:
     * <ul>
     *   <li>Sets title, location, organizer label, and formatted time</li>
     *   <li>Computes and shows a status label based on current time vs. start/end</li>
     *   <li>Loads the cover image from {@code imageUrl} on a background thread</li>
     *   <li>Sets a click listener to open {@link AdminEventDetailActivity}</li>
     * </ul>
     *
     * @param holder   view holder for the row
     * @param position adapter position
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        AdminEventManagementActivity.EventItem e = events.get(position);

        holder.tvTitle.setText(e.title != null ? e.title : "Untitled");
        holder.tvLocation.setText(e.location != null ? e.location : "Unknown");
        holder.tvOrganizer.setText("Organizer: XYZ");
        if (e.startAt != null) holder.tvTime.setText(timeFmt.format(e.startAt.toDate()));

        // ---- Status logic ----
        long now = System.currentTimeMillis();
        long start = e.startAt != null ? e.startAt.toDate().getTime() : Long.MAX_VALUE;
        long end = e.endAt != null ? e.endAt.toDate().getTime() : Long.MAX_VALUE;

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

        // ---- Load Firebase image manually ----
        if (e.imageUrl != null && !e.imageUrl.isEmpty()) {
            holder.imgCover.setImageResource(android.R.drawable.ic_menu_gallery);
            new Thread(() -> {
                try {
                    URL url = new URL(e.imageUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    try (InputStream input = conn.getInputStream()) {
                        final Bitmap bmp = BitmapFactory.decodeStream(input);
                        holder.imgCover.post(() -> {
                            if (bmp != null)
                                holder.imgCover.setImageBitmap(bmp);
                            else
                                holder.imgCover.setImageResource(android.R.drawable.ic_menu_report_image);
                        });
                    }
                } catch (Exception ex) {
                    holder.imgCover.post(() -> holder.imgCover.setImageResource(android.R.drawable.ic_menu_report_image));
                }
            }).start();
        } else {
            holder.imgCover.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        // ---- Click to open AdminEventDetailActivity ----
        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, AdminEventDetailActivity.class);
            i.putExtra("event_id", e.id);
            context.startActivity(i);
        });
    }

    /**
     * @return the number of events to render
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * View holder for a single admin event card row.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvTitle, tvTime, tvLocation, tvOrganizer, chipStatus;

        /**
         * Binds row view references.
         *
         * @param itemView the inflated row view
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
