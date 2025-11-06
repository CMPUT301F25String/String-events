package com.example.string_events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * ListView 版本的适配器（注意：不是 RecyclerView）
 */
public class AdminEventAdapter extends ArrayAdapter<AdminEvent> {

    private final LayoutInflater inflater;

    public AdminEventAdapter(@NonNull Context context, @NonNull List<AdminEvent> data) {
        super(context, 0, data);
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_admin_event_card, parent, false);
            h = new ViewHolder(convertView);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        AdminEvent e = getItem(position);
        if (e != null) {
            h.imgCover.setImageResource(e.getCoverResId());
            h.tvTitle.setText(e.getTitle());
            h.tvTime.setText(e.getTime());
            h.imgLocationLogo.setImageResource(e.getLocationLogoResId());
            h.tvLocation.setText(e.getLocation());
            h.tvOrganizer.setText(getContext().getString(R.string.sample_organizer));

            // 状态样式
            switch (e.getStatus()) {
                case IN_PROGRESS:
                    h.chipStatus.setText(R.string.status_in_progress);
                    h.chipStatus.setBackgroundResource(R.drawable.bg_status_inprogress);
                    break;
                case SCHEDULED:
                    h.chipStatus.setText(R.string.status_scheduled);
                    h.chipStatus.setBackgroundResource(R.drawable.bg_status_scheduled);
                    break;
                case FINISHED:
                    h.chipStatus.setText(R.string.status_finished);
                    h.chipStatus.setBackgroundResource(R.drawable.bg_status_finished);
                    break;
            }
        }
        return convertView;
    }

    static class ViewHolder {
        ImageView imgCover, imgLocationLogo;
        TextView tvTitle, tvTime, tvLocation, tvOrganizer, chipStatus;

        ViewHolder(View v) {
            imgCover = v.findViewById(R.id.imgCover);
            imgLocationLogo = v.findViewById(R.id.imgLocationLogo);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvTime = v.findViewById(R.id.tvTime);
            tvLocation = v.findViewById(R.id.tvLocation);
            tvOrganizer = v.findViewById(R.id.tvOrganizer);
            chipStatus = v.findViewById(R.id.chipStatus);
        }
    }
}
