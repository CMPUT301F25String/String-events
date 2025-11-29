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

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SELECTION = 0;
    private static final int VIEW_TYPE_MESSAGE = 1;

    Context context;
    ArrayList<Notification> notificationList;

    public NotificationAdapter(Context context, ArrayList<Notification> notificationsList) {
        this.context = context;
        this.notificationList = notificationsList;
    }

    @Override
    public int getItemViewType(int position) {
        Notification n = notificationList.get(position);
        return n.isMessage() ? VIEW_TYPE_MESSAGE : VIEW_TYPE_SELECTION;
    }

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

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Notification notification = notificationList.get(position);

        if (holder instanceof MessageViewHolder) {
            bindMessage((MessageViewHolder) holder, notification);
        } else if (holder instanceof SelectionViewHolder) {
            bindSelection((SelectionViewHolder) holder, notification);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

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


        holder.notificationItemLayout.setOnClickListener(view -> {
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("event_id", notification.getEventId());
            context.startActivity(intent);
        });


//        holder.notificationExpandButton.setOnClickListener(view -> {
//            NotificationHelper.showNotification(context, notification);
//        });
    }

    private void bindMessage(MessageViewHolder holder, Notification notification) {

        holder.notificationStatus.setImageResource(R.drawable.selected_status);
        holder.notificationMessage.setText("Message Regarding:");
        holder.notificationEventName.setText(notification.getEventName());

        if (notification.getEventPhoto() != null) {
            loadBitmapIntoView(notification.getEventPhoto(), holder.notificationPhoto);
        } else {
            holder.notificationPhoto.setImageResource(R.drawable.event_image);
        }


        holder.notificationItemLayout.setOnClickListener(view -> {
            Intent intent = new Intent(context, NotificationMessageDetailActivity.class);
            intent.putExtra("eventId", notification.getEventId());
            intent.putExtra("eventName", notification.getEventName());
            intent.putExtra("imageUrl", notification.getEventPhoto() != null ? notification.getEventPhoto() : "");
            intent.putExtra("messageText", notification.getMessageText());
            context.startActivity(intent);
        });
    }

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

    public static class SelectionViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout notificationItemLayout;
        ImageView notificationStatus;
        ShapeableImageView notificationPhoto;
        TextView notificationMessage;
        TextView notificationEventName;
        ImageButton notificationExpandButton;

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

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout notificationItemLayout;
        ImageView notificationStatus;
        ShapeableImageView notificationPhoto;
        TextView notificationMessage;
        TextView notificationEventName;

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
