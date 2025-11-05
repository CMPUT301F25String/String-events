package com.example.eventspage;

// EventAdapter.java
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class EventAdapter extends ArrayAdapter<Event> {

    public EventAdapter(Context context, List<Event> events) {
        super(context, 0, events);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Event event = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_event_card, parent, false);
        }

        TextView tvTitle = convertView.findViewById(R.id.tv_title);
        TextView tvTime = convertView.findViewById(R.id.tv_time);
        TextView tvSpots = convertView.findViewById(R.id.tv_spots);
        TextView tvPlace = convertView.findViewById(R.id.tv_place);
        MaterialButton btnStatus = convertView.findViewById(R.id.btn_status);

        tvTitle.setText(event.getTitle());
        tvTime.setText(event.getTime());
        tvSpots.setText(event.getSpots());
        tvPlace.setText(event.getPlace());
        btnStatus.setText(event.getStatus());

        switch (event.getStatus()) {
            case "Scheduled":
                btnStatus.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.status_scheduled));
                break;
            case "Canceled":
                btnStatus.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.status_canceled));
                break;
            case "Full":
                btnStatus.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.status_full));
                break;
        }

        return convertView;
    }
}
