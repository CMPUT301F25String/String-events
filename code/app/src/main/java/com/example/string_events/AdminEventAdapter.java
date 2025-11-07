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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.ViewHolder> {

    private final ArrayList<AdminEventManagementActivity.EventItem> events;
    private final Context context;

    public AdminEventAdapter(ArrayList<AdminEventManagementActivity.EventItem> events, Context context) {
        this.events = events;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_admin_event_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminEventManagementActivity.EventItem e = events.get(position);

        holder.tvTitle.setText(e.title != null ? e.title : "(No Title)");
        holder.tvLocation.setText(e.location != null ? e.location : "(No Location)");
        holder.tvOrganizer.setText("Organizer: XYZ");

        // Format start time
        if (e.startAt != null) {
            DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
            holder.tvTime.setText(df.format(e.startAt.toDate()));
        } else {
            holder.tvTime.setText("--:--");
        }

        // Load image from URL in background
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

        // Determine event status
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

        // 🔹 When you click on an event, open the detail screen
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminEventDetailActivity.class);
            intent.putExtra("event_id", e.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvTitle, tvTime, tvLocation, tvOrganizer, tvStatus;

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
