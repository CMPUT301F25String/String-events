package com.example.string_events;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ProfileEventsAdapter extends RecyclerView.Adapter<ProfileEventsAdapter.profileEventsViewHolder> {
    Context context;
    ArrayList<ProfileEvent> profileEventsList;
    SharedPreferences sharedPreferences;


    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d", Locale.US);

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);

    public ProfileEventsAdapter(Context context, ArrayList<ProfileEvent> profileEventsList, SharedPreferences sharedPreferences) {
        this.context = context;
        this.profileEventsList = profileEventsList;
        this.sharedPreferences = sharedPreferences;
    }

    @NonNull
    @Override
    public profileEventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_profile_event, parent, false);
        return new profileEventsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull profileEventsViewHolder holder, int position) {
        ProfileEvent profileEvent = profileEventsList.get(position);
        String currentRole = sharedPreferences.getString("role", null);


        if (profileEvent.getProfileEventPhotoUrl() == null) {
            holder.eventPhoto.setImageResource(R.drawable.no_image_available);
        } else {
            Glide.with(context)
                    .load(profileEvent.getProfileEventPhotoUrl())
                    .into(holder.eventPhoto);
        }

        holder.eventName.setText(profileEvent.getProfileEventName());
        holder.eventLocation.setText(profileEvent.getProfileEventLocation());


        Date start = profileEvent.getProfileEventStartDateTime();

        if (start != null) {

            holder.eventDate.setText(dateFormat.format(start));


            holder.eventTime.setText(timeFormat.format(start));
        } else {
            holder.eventDate.setText("TBD");
            holder.eventTime.setText("");
        }


        holder.itemLayout.setOnClickListener(view -> {
            Intent intent;
            if (Objects.equals(currentRole, "entrant")) {
                intent = new Intent(context, EventDetailActivity.class);
            } else {
                intent = new Intent(context, OrganizerEventOverviewScreen.class);
            }
            intent.putExtra("event_id", profileEvent.getEventId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return profileEventsList.size(); }

    public static class profileEventsViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout itemLayout;
        ShapeableImageView eventPhoto;
        TextView eventName, eventDate, eventTime, eventLocation;

        public profileEventsViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLayout = itemView.findViewById(R.id.item_layout);
            eventPhoto = itemView.findViewById(R.id.event_image);
            eventName = itemView.findViewById(R.id.event_name);
            eventDate = itemView.findViewById(R.id.event_date);
            eventTime = itemView.findViewById(R.id.event_time);
            eventLocation = itemView.findViewById(R.id.event_location);
        }
    }
}