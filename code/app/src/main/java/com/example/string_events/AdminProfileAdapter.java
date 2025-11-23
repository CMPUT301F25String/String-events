package com.example.string_events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter that displays a list of admin profiles in a {@link RecyclerView}
 * and dispatches item click events via {@link OnProfileClickListener}.
 */
public class AdminProfileAdapter extends RecyclerView.Adapter<AdminProfileAdapter.ProfileViewHolder> {

    private final List<AdminProfiles> profiles;
    private final OnProfileClickListener listener;

    /**
     * Callback for profile item clicks.
     */
    public interface OnProfileClickListener {
        /**
         * Invoked when a profile row is clicked.
         *
         * @param profile the clicked {@link AdminProfiles} item
         */
        void onProfileClick(AdminProfiles profile);
    }

    /**
     * Creates a new adapter for admin profiles.
     *
     * @param profiles immutable list of profiles to display
     * @param listener optional click listener for item selection
     */
    public AdminProfileAdapter(List<AdminProfiles> profiles, OnProfileClickListener listener) {
        this.profiles = profiles;
        this.listener = listener;
    }

    /**
     * Inflates the item view and creates a new {@link ProfileViewHolder}.
     *
     * @param parent   the parent view group
     * @param viewType unused view type
     * @return a new {@link ProfileViewHolder}
     */
    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    /**
     * Binds the profile data to the holder and sets the click callback.
     *
     * @param holder   target view holder
     * @param position adapter position of the item
     */
    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        AdminProfiles profile = profiles.get(position);
        holder.name.setText("Name: " + profile.getName());
        holder.email.setText("Email: " + profile.getEmail());
        holder.password.setText("Password: " + profile.getPassword());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProfileClick(profile);
        });
    }

    /**
     * @return the total number of profiles
     */
    @Override
    public int getItemCount() {
        return profiles.size();
    }

    /**
     * Simple holder for admin profile item views.
     */
    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, password;

        /**
         * Creates a holder bound to the given item view.
         *
         * @param itemView the inflated profile row view
         */
        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.profile_name);
            email = itemView.findViewById(R.id.profile_email);
            password = itemView.findViewById(R.id.profile_password);
        }
    }
}
