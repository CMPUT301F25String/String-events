package com.example.string_events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.VH> {

    private final List<AdminEvent> data;

    public AdminEventAdapter(List<AdminEvent> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AdminEvent e = data.get(position);
        h.title.setText(e.getTitle());
        h.time.setText(e.getTime());
        h.location.setText(e.getLocation());
        h.organizer.setText(e.getOrganizer());
        h.cover.setImageResource(e.getCoverResId());
        h.locationLogo.setImageResource(e.getLocationLogoResId());

        // 状态 pill：文本 + 背景 (三色圆角)，这些 drawable 你项目里已经有
        switch (e.getStatus()) {
            case IN_PROGRESS:
                h.status.setText(R.string.status_in_progress);
                h.status.setBackgroundResource(R.drawable.bg_status_inprogress);
                break;
            case SCHEDULED:
                h.status.setText(R.string.status_scheduled);
                h.status.setBackgroundResource(R.drawable.bg_status_scheduled);
                break;
            case FINISHED:
                h.status.setText(R.string.status_finished);
                h.status.setBackgroundResource(R.drawable.bg_status_finished);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView cover, locationLogo;
        TextView title, time, location, organizer, status;

        VH(@NonNull View v) {
            super(v);
            // ↓↓↓ 这些 id 要与你的 item_admin_event_card.xml 对齐 ↓↓↓
            cover = v.findViewById(R.id.iv_cover);
            title = v.findViewById(R.id.tv_title);
            time = v.findViewById(R.id.tv_time);
            locationLogo = v.findViewById(R.id.iv_location_logo);
            location = v.findViewById(R.id.tv_location);
            organizer = v.findViewById(R.id.tv_organizer);
            status = v.findViewById(R.id.tv_status);
        }
    }
}
