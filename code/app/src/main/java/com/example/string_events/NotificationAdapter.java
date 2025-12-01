package com.example.string_events;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * RecyclerView adapter for displaying entrant notifications.
 * <p>
 * There are two types of notifications rendered:
 * <ul>
 *     <li>Selection notifications (lottery result): accepted / not selected.</li>
 *     <li>Message notifications: free-form message from organizers.</li>
 * </ul>
 * The adapter chooses which layout to inflate based on {@link Notification#isMessage()}.
 */
public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * View type for selection/lottery result notifications.
     */
    private static final int VIEW_TYPE_SELECTION = 0;

    /**
     * View type for organizer message notifications.
     */
    private static final int VIEW_TYPE_MESSAGE = 1;

    /**
     * Context used to inflate layouts and start detail activities.
     */
    Context context;

    /**
     * List of notifications to be displayed.
     */
    ArrayList<Notification> notificationList;

    /**
     * Creates a new adapter instance.
     *
     * @param context          the context used for inflation and navigation
     * @param notificationsList list of notifications to display
     */
    public NotificationAdapter(Context context, ArrayList<Notification> notificationsList) {
        this.context = context;
        this.notificationList = notificationsList;
    }

    /**
     * Returns the view type for the given position based on whether the
     * notification is a message or a selection result.
     *
     * @param position position in the list
     * @return {@link #VIEW_TYPE_MESSAGE} if {@link Notification#isMessage()} is true,
     * otherwise {@link #VIEW_TYPE_SELECTION}
     */
    @Override
    public int getItemViewType(int position) {
        Notification n = notificationList.get(position);
        return n.isMessage() ? VIEW_TYPE_MESSAGE : VIEW_TYPE_SELECTION;
    }

    /**
     * Inflates the appropriate view holder depending on the notification type.
     *
     * @param parent   parent view group
     * @param viewType view type as returned from {@link #getItemViewType(int)}
     * @return a new {@link RecyclerView.ViewHolder} instance
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == VIEW_TYPE_MESSAGE) {
            View itemView = inflater.inflate(R.layout.item_notification_message, parent, false);
            return new MessageViewHolder(itemView);
        }

        View itemView = inflater.inflate(R.layout.item_notification, parent, false);
        return new SelectionViewHolder(itemView);
    }

    /**
     * Binds the appropriate data to the provided view holder for the given position.
     *
     * @param holder   view holder to bind data into
     * @param position adapter position of the item
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Notification notification = notificationList.get(position);

        if (holder instanceof MessageViewHolder) {
            bindMessage((MessageViewHolder) holder, notification);
        } else if (holder instanceof SelectionViewHolder) {
            bindSelection((SelectionViewHolder) holder, notification);
        }
    }

    /**
     * @return the total number of notifications in the list
     */
    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    /**
     * Binds a selection/lottery notification to its view holder.
     * <p>
     * Sets the status icon, message text, event name, thumbnail, and click listeners.
     *
     * @param holder       the selection view holder
     * @param notification the notification data
     */
    private void bindSelection(SelectionViewHolder holder, Notification notification) {

        if (notification.getSelectedStatus()) {
            holder.notificationStatus.setImageResource(R.drawable.selected_status);
            holder.notificationMessage.setText("Congratulations,\nyou were selected for:");
        } else {
            holder.notificationStatus.setImageResource(R.drawable.not_selected_status);
            holder.notificationMessage.setText("Unfortunately,\nyou weren't selected for:");
        }

        if (notification.getEventPhoto() != null) {
            loadBitmapIntoView(notification.getEventPhoto(), holder.notificationPhoto);
        } else {
            holder.notificationPhoto.setImageResource(R.drawable.event_image);
        }

        holder.notificationEventName.setText(notification.getEventName());

        // Open event detail screen when tapping the notification card
        holder.notificationItemLayout.setOnClickListener(view -> {
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("event_id", notification.getEventId());
            context.startActivity(intent);
        });

        // Show an expanded dialog or additional info via NotificationHelper
        holder.notificationExpandButton.setOnClickListener(view ->
                NotificationHelper.showNotification(context, notification)
        );
    }

    /**
     * Binds a message notification to its view holder.
     * <p>
     * Sets the card as "Message Regarding:", event name, thumbnail, and
     * click navigation to {@link NotificationMessageDetailActivity}.
     *
     * @param holder       the message view holder
     * @param notification the notification data
     */
    private void bindMessage(MessageViewHolder holder, Notification notification) {

        holder.notificationStatus.setImageResource(R.drawable.selected_status);
        holder.notificationMessage.setText("Message Regarding:");
        holder.notificationEventName.setText(notification.getEventName());

        if (notification.getEventPhoto() != null) {
            loadBitmapIntoView(notification.getEventPhoto(), holder.notificationPhoto);
        } else {
            holder.notificationPhoto.setImageResource(R.drawable.event_image);
        }

        // Open message detail screen when tapping the notification card
        holder.notificationItemLayout.setOnClickListener(view -> {
            Intent intent = new Intent(context, NotificationMessageDetailActivity.class);
            intent.putExtra("eventId", notification.getEventId());
            intent.putExtra("eventName", notification.getEventName());
            intent.putExtra("imageUrl", notification.getEventPhoto() != null ? notification.getEventPhoto() : "");
            intent.putExtra("messageText", notification.getMessageText());
            context.startActivity(intent);
        });
    }

    /**
     * Loads a bitmap from the given URL on a background thread and sets it into
     * the provided {@link ImageView}. If loading fails, a fallback image is used.
     *
     * @param urlString URL of the image to load
     * @param imageView target ImageView to display the bitmap
     */
    private void loadBitmapIntoView(String urlString, ImageView imageView) {
        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.connect();

                InputStream input = conn.getInputStream();
                Bitmap bmp = BitmapFactory.decodeStream(input);

                imageView.post(() -> imageView.setImageBitmap(bmp));

            } catch (Exception e) {
                imageView.post(() -> imageView.setImageResource(R.drawable.event_image));
            }
        }).start();
    }

    /**
     * ViewHolder for selection/lottery notifications.
     * <p>
     * Holds references to the UI elements of {@code item_notification} layout.
     */
    public static class SelectionViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout notificationItemLayout;
        ImageView notificationStatus;
        ShapeableImageView notificationPhoto;
        TextView notificationMessage;
        TextView notificationEventName;
        ImageButton notificationExpandButton;

        /**
         * Creates a new SelectionViewHolder instance.
         *
         * @param itemView the inflated item view
         */
        public SelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationItemLayout = itemView.findViewById(R.id.item_layout);
            notificationStatus = itemView.findViewById(R.id.imgStatus);
            notificationPhoto = itemView.findViewById(R.id.imgThumb);
            notificationMessage = itemView.findViewById(R.id.tvMessage);
            notificationEventName = itemView.findViewById(R.id.tvEventName);
            notificationExpandButton = itemView.findViewById(R.id.imgOpen);
        }
    }

    /**
     * ViewHolder for message-style notifications.
     * <p>
     * Holds references to the UI elements of {@code item_notification_message} layout.
     */
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout notificationItemLayout;
        ImageView notificationStatus;
        ShapeableImageView notificationPhoto;
        TextView notificationMessage;
        TextView notificationEventName;

        /**
         * Creates a new MessageViewHolder instance.
         *
         * @param itemView the inflated item view
         */
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationItemLayout = itemView.findViewById(R.id.notification_item);
            notificationStatus = itemView.findViewById(R.id.imgStatus);
            notificationPhoto = itemView.findViewById(R.id.imgThumb);
            notificationMessage = itemView.findViewById(R.id.tvMessage);
            notificationEventName = itemView.findViewById(R.id.tvEventName);
        }
    }
}
