package com.example.string_events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * ArrayAdapter that renders {@link UserItem} rows showing name, email, and a status badge
 * (canceled/participating/waitlist/invited) when available.
 */
public class UserAdapter extends ArrayAdapter<UserItem> {

    /**
     * Creates an adapter backed by a list of {@link UserItem}.
     *
     * @param context host context used to inflate views
     * @param data    list of items to display
     */
    public UserAdapter(@NonNull Context context, @NonNull List<UserItem> data) {
        super(context, 0, data);
    }

    /**
     * Inflates/binds {@code item_user_row} and sets the row fields:
     * name, email, and a visible status badge when {@link UserItem.Status} is present.
     *
     * @param position    adapter position
     * @param convertView recycled view, if any
     * @param parent      parent view group
     * @return bound row {@link View}
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(
                    com.example.string_events.R.layout.item_user_row, parent, false);
        }

        UserItem item = getItem(position);
        if (item == null) return v;

        TextView tvName = v.findViewById(com.example.string_events.R.id.tvName);
        TextView tvEmail = v.findViewById(com.example.string_events.R.id.tvEmail);
        TextView tvBadge = v.findViewById(com.example.string_events.R.id.tvBadge);

        tvName.setText("Name: " + item.getName());
        tvEmail.setText("Email: " + item.getEmail());

        tvBadge.setVisibility(View.GONE);
        if (item.getStatus() != null) {
            switch (item.getStatus()) {
                case CANCELED:
                    tvBadge.setText("canceled");
                    tvBadge.setVisibility(View.VISIBLE);
                    break;
                case PARTICIPATING:
                    tvBadge.setText("participating");
                    tvBadge.setVisibility(View.VISIBLE);
                    break;
                case WAITLIST:
                    tvBadge.setText("waitlist");
                    tvBadge.setVisibility(View.VISIBLE);
                    break;
                case INVITED:
                    tvBadge.setText("invited");
                    tvBadge.setVisibility(View.VISIBLE);
                    break;
                default:
            }
        }
        return v;
    }
}
