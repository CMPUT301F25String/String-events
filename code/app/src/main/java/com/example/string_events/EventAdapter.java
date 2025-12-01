package com.example.string_events;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * List adapter that binds {@link EventItem} objects to {@code item_event} rows.
 * <p>
 * Each row displays:
 * <ul>
 *     <li>Cover image (or a fallback image if none is available)</li>
 *     <li>Title and location</li>
 *     <li>Start time</li>
 *     <li>Number of spots left</li>
 *     <li>Status (Scheduled / In Progress / Finished) with color coding</li>
 * </ul>
 */
public class EventAdapter extends BaseAdapter {
    private final Context context;
    private final List<EventItem> items;
    /**
     * Date-time formatter for displaying event start time.
     * <p>
     * Example: {@code Nov 28, 2025 at 4:30 PM}
     */
    private final SimpleDateFormat dateTimeFmt = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());

    /**
     * Creates a new adapter with the given backing list.
     *
     * @param items list of events to display
     */
    EventAdapter(Context context, List<EventItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override public int getCount() { return items.size(); }
    @Override public EventItem getItem(int position) { return items.get(position); }
    @Override public long getItemId(int position) { return position; }

    /**
     * Inflates and binds a single event row.
     *
     * @param pos         position of the item in the list
     * @param convertView recycled view, if available
     * @param parent      parent view group
     * @return the populated row view
     */
    @Override
    public View getView(int pos, View convertView, android.view.ViewGroup parent) {
        View v = convertView;
        Holder h;
        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
            h = new Holder();
            h.imgCover = v.findViewById(R.id.img_cover);
            h.tvTitle = v.findViewById(R.id.tv_title);
            h.tvTime = v.findViewById(R.id.tv_time);
            h.tvSpots = v.findViewById(R.id.tv_spots);
            h.tvPlace = v.findViewById(R.id.tv_place);
            h.btnStatus = v.findViewById(R.id.btn_status);
            v.setTag(h);
        } else {
            h = (Holder) v.getTag();
        }

        EventItem e = getItem(pos);
        if (e.imageUrl == null) {
            h.imgCover.setImageResource(R.drawable.no_image_available);
        } else {
            Glide.with(context)
                    .load(e.imageUrl)
                    .into(h.imgCover);
        }
        if (h.tvTitle != null) h.tvTitle.setText(e.title == null ? "" : e.title);
        if (h.tvPlace != null) h.tvPlace.setText(e.location == null ? "" : e.location);

        if (h.tvTime != null) {
            String t = (e.startAt == null) ? "" : dateTimeFmt.format(e.startAt.toDate());
            h.tvTime.setText(t);
        }

        if (h.tvSpots != null) {
            int left = Math.max(0, e.maxAttendees - e.attendeesCount);
            h.tvSpots.setText(left + " Spots Left");
        }
        if (h.btnStatus != null) {
            long now = System.currentTimeMillis();
            long start = e.startAt == null ? Long.MAX_VALUE : e.startAt.toDate().getTime();
            long end = e.endAt == null ? Long.MAX_VALUE : e.endAt.toDate().getTime();
            CharSequence text;
            int color;
            if (now < start) {
                text = "Scheduled";
                color = 0xFF43C06B;
            } else if (now > end) {
                text = "Finished";
                color = 0xFFE45A5A;
            } else {
                text = "In Progress";
                color = 0xFFF1A428;
            }
            h.btnStatus.setText(text);
            h.btnStatus.setBackgroundTintList(ColorStateList.valueOf(color));
        }
        return v;
    }

    /**
     * ViewHolder pattern for efficient row view reuse.
     */
    static class Holder {
        ImageView imgCover;
        TextView tvTitle, tvTime, tvSpots, tvPlace;
        MaterialButton btnStatus;
    }
}