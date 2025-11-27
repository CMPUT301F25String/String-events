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

public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.Holder> {

    private final ArrayList<OrganizerEvent> items;
    private final DateFormat timeFmt =
            DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());

    OrganizerEventAdapter(ArrayList<OrganizerEvent> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_organizer, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        OrganizerEvent e = items.get(position);

        if (h.tvTitle != null) {
            h.tvTitle.setText(e.title == null ? "" : e.title);
        }

        if (h.tvPlace != null) {
            h.tvPlace.setText(e.location == null ? "" : e.location);
        }

        if (h.tvTime != null) {
            String t = (e.startAt == null) ? "" : timeFmt.format(e.startAt.toDate());
            h.tvTime.setText(t);
        }

        if (h.tvOrganizer != null) {
            h.tvOrganizer.setText(
                    e.creator == null || e.creator.isEmpty()
                            ? ""
                            : "Organizer: " + e.creator
            );
        }

        if (h.tvSpots != null) {
            int count = Math.max(0, e.attendeesCount);
            int left = Math.max(0, e.maxAttendees - count);
            h.tvSpots.setText(left + " Spots Left");
        }

        if (e.imageUrl != null && !e.imageUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    URL url = new URL(e.imageUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream input = conn.getInputStream();
                    Bitmap bmp = BitmapFactory.decodeStream(input);
                    h.imgCover.post(() -> h.imgCover.setImageBitmap(bmp));
                } catch (Exception ex) {
                    h.imgCover.post(() ->
                            h.imgCover.setImageResource(android.R.drawable.ic_menu_report_image));
                }
            }).start();
        } else {
            h.imgCover.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), OrganizerEventOverviewScreen.class);
            intent.putExtra("event_id", e.id);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvTitle, tvTime, tvSpots, tvPlace, tvOrganizer;

        public Holder(@NonNull View v) {
            super(v);
            imgCover    = v.findViewById(R.id.imgCover);
            tvTitle     = v.findViewById(R.id.tvTitle);
            tvTime      = v.findViewById(R.id.tvTime);
            tvPlace     = v.findViewById(R.id.tvLocation);
            tvOrganizer = v.findViewById(R.id.tvOrganizer);
            tvSpots     = v.findViewById(R.id.chipStatus);
        }
    }
}
