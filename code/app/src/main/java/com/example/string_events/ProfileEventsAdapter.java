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

import com.bumptech.glide.Glide;
import com.example.string_events.R;

import org.w3c.dom.Text;

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
        // assigning values to the items in the recyclerView as they are being inflated
        ProfileEvent profileEvent = profileEventsList.get(position);

        // no image uploaded for event
        if (profileEvent.profileEventPhotoUrl == null) {
            holder.eventPhoto.setImageResource(R.drawable.no_image_available);
        }
        // image uploaded for event and retrieved from database successfully
        else {
            Glide.with(context)
                    .load(profileEvent.getProfileEventPhotoUrl())
                    .into(holder.eventPhoto);
        }
        holder.eventName.setText(profileEvent.getProfileEventName());
        // TODO database integration and formatting
//        holder.eventDate.setText(profileEvent.getProfileEventStartDateTime());
//        holder.eventTime.setText(profileEvent.getProfileEventStartDateTime());
        holder.eventLocation.setText(profileEvent.getProfileEventLocation());

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO open new fragment of event detail
            }
        });
    }

    @Override
    public int getItemCount() {
        return profileEventsList.size();
    }

    public static class profileEventsViewHolder extends RecyclerView.ViewHolder {
        // taking the views from the item layout and assigning them to variables
        ConstraintLayout itemLayout;
        ImageView eventPhoto;
        TextView eventName;
        TextView eventDate;
        TextView eventTime;
        TextView eventLocation;

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
