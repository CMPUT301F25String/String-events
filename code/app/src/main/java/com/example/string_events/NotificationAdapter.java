package com.example.string_events;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

/**
 * RecyclerView adapter for rendering event notifications and navigating
 * to {@link EventDetailActivity} when an item is tapped.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.notificationViewHolder> {
    Context context;
    ArrayList<Notification> notificationList;

    /**
     * Creates an adapter backed by a list of {@link Notification}.
     *
     * @param context Android context used to inflate views and start activities
     * @param notificationsList backing data for the adapter
     */
    public NotificationAdapter(Context context, ArrayList<Notification> notificationsList) {
        this.context = context;
        this.notificationList = notificationsList;
    }

    /**
     * Inflates {@code item_notification} row view.
     */
    @NonNull
    @Override
    public notificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflating (creating) the item layout
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationAdapter.notificationViewHolder(itemView);
    }

    /**
     * Binds a {@link Notification} to the row views: selection icon/message,
     * thumbnail, event name, and click action to open details.
     */
    @Override
    public void onBindViewHolder(@NonNull notificationViewHolder holder, int position) {
        // assigning values to the items in the recyclerView as they are being inflated
        Notification notification = notificationList.get(position);
        if (notification.getSelectedStatus()) {
            holder.notificationStatus.setImageResource(R.drawable.selected_status);
            holder.notificationMessage.setText("Congratulations,\nyou were selected for:");
        }
        else {
            holder.notificationStatus.setImageResource(R.drawable.not_selected_status);
            holder.notificationMessage.setText("Unfortunately,\nyou weren't selected for:");
        }

//        holder.notificationPhoto.setImageURI(notification.getEventPhoto()); // TODO use this line once URIs are setup
        holder.notificationPhoto.setImageResource(R.drawable.event_image);
        holder.notificationEventName.setText(notification.getEventName());

        // TODO remove the notification from the notification list once clicked to solve some issues
        holder.notificationItemLayout.setOnClickListener(view -> {
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("source", "notification");
            intent.putExtra("selectedStatus", String.valueOf(notification.getSelectedStatus()));
            intent.putExtra("event_id", notification.getEventId());
            context.startActivity(intent);
        });
    }

    /**
     * @return number of notifications to display
     */
    @Override
    public int getItemCount() {
        // get the number of items to be displayed
        return notificationList.size();
    }

    /**
     * ViewHolder that caches row subviews for a notification item.
     */
    public static class notificationViewHolder extends RecyclerView.ViewHolder {
        // taking the views from the item layout and assigning them to variables
        ConstraintLayout notificationItemLayout;
        ImageView notificationStatus;
        ShapeableImageView notificationPhoto;
        TextView notificationMessage;
        TextView notificationEventName;
        ImageButton notificationExpandButton;

        /**
         * Binds subviews from {@code item_notification}.
         *
         * @param itemView root row view
         */
        public notificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationItemLayout = itemView.findViewById(R.id.item_layout);
            notificationStatus = itemView.findViewById(R.id.imgStatus);
            notificationPhoto = itemView.findViewById(R.id.imgThumb);
            notificationMessage = itemView.findViewById(R.id.tvMessage);
            notificationEventName = itemView.findViewById(R.id.tvEventName);
            notificationExpandButton = itemView.findViewById(R.id.imgOpen);
        }
    }
}
