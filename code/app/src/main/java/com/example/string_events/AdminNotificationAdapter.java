package com.example.string_events;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView adapter for displaying a list of notifications in the admin interface.
 * <p>
 * Each row shows:
 * <ul>
 *     <li>A title indicating the type of notification (lottery message, message, or generic notification).</li>
 *     <li>The related event name.</li>
 * </ul>
 * Tapping on a row opens {@link AdminNotificationDetailActivity} with the full notification details.
 */
public class AdminNotificationAdapter extends RecyclerView.Adapter<AdminNotificationAdapter.ViewHolder> {

    /**
     * List of notifications to be displayed.
     */
    private final List<Notification> list;

    /**
     * Creates a new adapter for admin notifications.
     *
     * @param list list of {@link Notification} objects to render in the RecyclerView
     */
    public AdminNotificationAdapter(List<Notification> list) {
        this.list = list;
    }

    /**
     * Inflates the notification item layout and returns a new {@link ViewHolder}.
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type of the new view (unused here)
     * @return a new {@link ViewHolder} instance
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_notification, parent, false);
        return new ViewHolder(v);
    }

    /**
     * Binds a {@link Notification} item to the given {@link ViewHolder}.
     * <p>
     * This method:
     * <ul>
     *     <li>Determines the title based on notification type (lottery, message, or generic).</li>
     *     <li>Displays the related event name.</li>
     *     <li>Sets a click listener to open the detail screen.</li>
     * </ul>
     *
     * @param h        the {@link ViewHolder} to bind data to
     * @param position position of the item within the adapter data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Notification n = list.get(position);

        if (n.getSelectedStatus()) {
            h.tvTitle.setText("Lottery Message");
        } else if (n.isMessage()) {
            h.tvTitle.setText("Message");
        } else {
            h.tvTitle.setText("Notification");
        }

        h.tvEventName.setText(n.getEventName());

        // Click handler: open notification detail screen
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), AdminNotificationDetailActivity.class);
            i.putExtra("username", n.getUsername());
            i.putExtra("eventId", n.getEventId());
            i.putExtra("selectedStatus", n.getSelectedStatus());
            i.putExtra("isMessage", n.isMessage());
            i.putExtra("messageText", n.getMessageText());
            v.getContext().startActivity(i);
        });
    }

    /**
     * Returns the number of notifications in the list.
     *
     * @return size of the notifications list
     */
    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * ViewHolder representing a single admin notification item.
     * <p>
     * Holds references to:
     * <ul>
     *     <li>Notification title</li>
     *     <li>Event name</li>
     *     <li>Arrow icon indicating navigation</li>
     * </ul>
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvEventName;
        ImageView ivArrow;

        /**
         * Creates a new ViewHolder and binds view references.
         *
         * @param v the root view of the notification item layout
         */
        ViewHolder(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvEventName = v.findViewById(R.id.tvEventName);
            ivArrow = v.findViewById(R.id.ivArrow);
        }
    }
}
