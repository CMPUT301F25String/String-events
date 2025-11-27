package com.example.string_events;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;


import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Objects;

/**
 * RecyclerView adapter that renders a user's related events inside the profile screen.
 * Loads a cover image (or a placeholder), name, and basic metadata for each event.
 */
public class ProfileEventsAdapter extends RecyclerView.Adapter<ProfileEventsAdapter.profileEventsViewHolder> {
    Context context;
    ArrayList<ProfileEvent> profileEventsList;
    SharedPreferences sharedPreferences;

    /**
     * Creates an adapter backed by a list of {@link ProfileEvent}.
     *
     * @param context Android context used for inflating rows and image loading
     * @param profileEventsList data set to display
     */
    public ProfileEventsAdapter(Context context, ArrayList<ProfileEvent> profileEventsList, SharedPreferences sharedPreferences) {
        this.context = context;
        this.profileEventsList = profileEventsList;
        this.sharedPreferences = sharedPreferences;
    }

    /**
     * Inflates {@code item_profile_event} and returns a new {@link profileEventsViewHolder}.
     */
    @NonNull
    @Override
    public profileEventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflating (creating) the item layout
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_profile_event, parent, false);
        return new ProfileEventsAdapter.profileEventsViewHolder(itemView);
    }

    /**
     * Binds a {@link ProfileEvent} to the row views: cover image, name, and location.
     * (Date/time fields are left for future database integration.)
     *
     * @param holder view holder to bind
     * @param position adapter position
     */
    @Override
    public void onBindViewHolder(@NonNull profileEventsViewHolder holder, int position) {
        // assigning values to the items in the recyclerView as they are being inflated
        ProfileEvent profileEvent = profileEventsList.get(position);
        String currentRole = sharedPreferences.getString("role", null);

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

        holder.itemLayout.setOnClickListener(view -> {
            // either open event details or event overview based on the user's current role (entrant or organizer)
            Intent intent;
            if (Objects.equals(currentRole, "entrant")) {
                intent = new Intent(context, EventDetailActivity.class);
            }
            else {
                intent = new Intent(context, OrganizerEventOverviewScreen.class);
            }
            intent.putExtra("event_id", profileEvent.getEventId());
            context.startActivity(intent);
        });
    }

    /**
     * @return number of profile events to display
     */
    @Override
    public int getItemCount() {
        return profileEventsList.size();
    }

    /**
     * ViewHolder that caches subviews for a profile event row.
     */
    public static class profileEventsViewHolder extends RecyclerView.ViewHolder {
        // taking the views from the item layout and assigning them to variables
        ConstraintLayout itemLayout;
        ShapeableImageView eventPhoto;
        TextView eventName;
        TextView eventDate;
        TextView eventTime;
        TextView eventLocation;

        /**
         * Binds subviews from {@code item_profile_event}.
         *
         * @param itemView root row view
         */
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
