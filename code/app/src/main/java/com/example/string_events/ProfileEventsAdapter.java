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

/**
 * RecyclerView adapter used to display a list of events on the user's profile screen.
 * <p>
 * Each item shows:
 * <ul>
 *     <li>Event image</li>
 *     <li>Event name</li>
 *     <li>Date and time</li>
 *     <li>Location</li>
 * </ul>
 * Tapping an item opens:
 * <ul>
 *     <li>{@link EventDetailActivity} when the current role is {@code "entrant"}</li>
 *     <li>{@link OrganizerEventOverviewScreen} for other roles (e.g., organizer/admin)</li>
 * </ul>
 */
public class ProfileEventsAdapter extends RecyclerView.Adapter<ProfileEventsAdapter.profileEventsViewHolder> {

    /**
     * Context used for inflating views and starting activities.
     */
    Context context;

    /**
     * List of profile events to be displayed.
     */
    ArrayList<ProfileEvent> profileEventsList;

    /**
     * Shared preferences used to read the current user role.
     */
    SharedPreferences sharedPreferences;

    /**
     * Date format used to display the event date (e.g. "Mon, Nov 3").
     */
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("EEE, MMM d", Locale.US);

    /**
     * Time format used to display the event time (e.g. "5:00 PM").
     */
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("h:mm a", Locale.US);

    /**
     * Creates a new adapter for profile events.
     *
     * @param context            {@link Context} for inflating layouts and launching activities
     * @param profileEventsList  list of {@link ProfileEvent} objects to render
     * @param sharedPreferences  shared preferences to read user role (e.g., "entrant" or organizer)
     */
    public ProfileEventsAdapter(Context context,
                                ArrayList<ProfileEvent> profileEventsList,
                                SharedPreferences sharedPreferences) {
        this.context = context;
        this.profileEventsList = profileEventsList;
        this.sharedPreferences = sharedPreferences;
    }

    /**
     * Inflates a single profile event item view and wraps it in a view holder.
     *
     * @param parent   parent {@link ViewGroup}
     * @param viewType not used (single view type)
     * @return a new {@link profileEventsViewHolder}
     */
    @NonNull
    @Override
    public profileEventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_profile_event, parent, false);
        return new profileEventsViewHolder(itemView);
    }

    /**
     * Binds a {@link ProfileEvent} to the given view holder.
     * <p>
     * This method:
     * <ul>
     *     <li>Loads the event image (or a default if missing).</li>
     *     <li>Sets event name, location, date, and time.</li>
     *     <li>Sets an item click listener that opens the proper detail screen
     *     depending on the stored user role.</li>
     * </ul>
     *
     * @param holder   view holder to bind
     * @param position adapter position of the item
     */
    @Override
    public void onBindViewHolder(@NonNull profileEventsViewHolder holder, int position) {
        ProfileEvent profileEvent = profileEventsList.get(position);
        String currentRole = sharedPreferences.getString("role", null);

        // Load event image or a placeholder if there is no image URL
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

        // Display formatted date/time if start is available, otherwise show "TBD"
        if (start != null) {
            holder.eventDate.setText(dateFormat.format(start));
            holder.eventTime.setText(timeFormat.format(start));
        } else {
            holder.eventDate.setText("TBD");
            holder.eventTime.setText("");
        }

        // Open appropriate detail screen based on the current user's role
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

    /**
     * @return the total number of profile events in the list
     */
    @Override
    public int getItemCount() {
        return profileEventsList.size();
    }

    /**
     * View holder for an individual profile event item row.
     * <p>
     * Holds references to:
     * <ul>
     *     <li>Event container layout</li>
     *     <li>Event image</li>
     *     <li>Event name</li>
     *     <li>Event date and time</li>
     *     <li>Event location</li>
     * </ul>
     */
    public static class profileEventsViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout itemLayout;
        ShapeableImageView eventPhoto;
        TextView eventName, eventDate, eventTime, eventLocation;

        /**
         * Creates a new view holder for the profile event item.
         *
         * @param itemView the inflated item view
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
