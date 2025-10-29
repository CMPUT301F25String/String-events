package com.example.string_events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProfileEventsAdapter extends RecyclerView.Adapter<ProfileEventsAdapter.profileEventsViewHolder> {
    Context context;
    ArrayList<ProfileEvent> profileEventsList;

    public ProfileEventsAdapter(Context context, ArrayList<ProfileEvent> profileEventsList) {
        this.context = context;
        this.profileEventsList = profileEventsList;
    }

    @NonNull
    @Override
    public profileEventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflating (creating) the item layout
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_profile_event, parent, false);
        return new ProfileEventsAdapter.profileEventsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull profileEventsViewHolder holder, int position) {
        // insert holder. items here
    }

    @Override
    public int getItemCount() {
        return profileEventsList.size();
    }

    public static class profileEventsViewHolder extends RecyclerView.ViewHolder {
        // Taking the views from the item layout and assigning them to variables

        public profileEventsViewHolder(@NonNull View itemView) {
            super(itemView);

            // insert findviewbyids here
        }
    }
}
