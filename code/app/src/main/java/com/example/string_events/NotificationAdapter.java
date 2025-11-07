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

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.notificationViewHolder> {
    Context context;
    ArrayList<Notification> notificationList;

    public NotificationAdapter(Context context, ArrayList<Notification> notificationsList) {
        this.context = context;
        this.notificationList = notificationsList;
    }

    @NonNull
    @Override
    public notificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflating (creating) the item layout
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationAdapter.notificationViewHolder(itemView);
    }

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

        holder.notificationItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EventDetailActivity.class);
                intent.putExtra("selectedStatus", String.valueOf(notification.getSelectedStatus()));
                intent.putExtra("event_id", notification.getEventId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        // get the number of items to be displayed
        return notificationList.size();
    }

    public static class notificationViewHolder extends RecyclerView.ViewHolder {
        // taking the views from the item layout and assigning them to variables
        ConstraintLayout notificationItemLayout;
        ImageView notificationStatus;
        ShapeableImageView notificationPhoto;
        TextView notificationMessage;
        TextView notificationEventName;
        ImageButton notificationExpandButton;

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
