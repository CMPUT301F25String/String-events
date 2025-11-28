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

public class AdminNotificationAdapter extends RecyclerView.Adapter<AdminNotificationAdapter.ViewHolder> {

    private final List<Notification> list;

    public AdminNotificationAdapter(List<Notification> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_notification, parent, false);
        return new ViewHolder(v);
    }

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

        // 🔥 CLICK HANDLER → open detail screen
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

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvEventName;
        ImageView ivArrow;

        ViewHolder(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvEventName = v.findViewById(R.id.tvEventName);
            ivArrow = v.findViewById(R.id.ivArrow);
        }
    }
}
