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

public class UserAdapter extends ArrayAdapter<UserItem> {

    public UserAdapter(@NonNull Context context, @NonNull List<UserItem> data) {
        super(context, 0, data);
    }

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


