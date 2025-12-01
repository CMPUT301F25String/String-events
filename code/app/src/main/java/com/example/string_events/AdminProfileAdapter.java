package com.example.string_events;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * RecyclerView adapter for displaying a list of admin profiles.
 * <p>
 * Each item shows:
 * <ul>
 *     <li>Profile photo</li>
 *     <li>Name</li>
 *     <li>Email</li>
 *     <li>Password (as plain text label)</li>
 * </ul>
 * Clicking an item triggers a callback to {@link OnProfileClickListener}
 * with the selected {@link AdminProfiles} instance.
 */
public class AdminProfileAdapter extends RecyclerView.Adapter<AdminProfileAdapter.ViewHolder> {

    /**
     * Listener interface to notify when a profile item is clicked.
     */
    public interface OnProfileClickListener {
        /**
         * Called when a profile item is clicked.
         *
         * @param profile the {@link AdminProfiles} instance associated with the clicked item
         */
        void onProfileClick(AdminProfiles profile);
    }

    /**
     * List of admin profiles to display.
     */
    private final ArrayList<AdminProfiles> profiles;

    /**
     * Listener for profile click events.
     */
    private final OnProfileClickListener listener;

    /**
     * Creates a new adapter for admin profiles.
     *
     * @param profiles list of profiles to display
     * @param listener callback invoked when a profile item is clicked
     */
    public AdminProfileAdapter(ArrayList<AdminProfiles> profiles, OnProfileClickListener listener) {
        this.profiles = profiles;
        this.listener = listener;
    }

    /**
     * Inflates the profile item layout and creates a new {@link ViewHolder}.
     *
     * @param parent   the parent view that the new view will be attached to
     * @param viewType the view type of the new view (unused here)
     * @return a new {@link ViewHolder} instance
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_profile, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds a profile item to the given {@link ViewHolder}.
     * <p>
     * This method:
     * <ul>
     *     <li>Sets the name, email, and password label.</li>
     *     <li>Loads the profile image from a URL if available, otherwise shows a default image.</li>
     *     <li>Registers a click listener to notify the {@link OnProfileClickListener}.</li>
     * </ul>
     *
     * @param holder   the {@link ViewHolder} to bind data to
     * @param position position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminProfiles profile = profiles.get(position);

        holder.name.setText(profile.getName());
        holder.email.setText(profile.getEmail());
        holder.password.setText("Password: " + profile.getPassword());

        String imgUrl = profile.getProfileImg();

        // Load profile image bitmap from URL if present, otherwise use a default placeholder
        if (imgUrl != null && !imgUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    URL url = new URL(imgUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream input = conn.getInputStream();
                    Bitmap bmp = BitmapFactory.decodeStream(input);

                    holder.photo.post(() -> holder.photo.setImageBitmap(bmp));
                } catch (Exception e) {
                    holder.photo.post(() ->
                            holder.photo.setImageResource(R.drawable.profile)
                    );
                }
            }).start();
        } else {
            holder.photo.setImageResource(R.drawable.profile);
        }

        holder.itemView.setOnClickListener(v -> listener.onProfileClick(profile));
    }

    /**
     * Returns the number of profile items in the adapter.
     *
     * @return size of the {@code profiles} list
     */
    @Override
    public int getItemCount() {
        return profiles.size();
    }

    /**
     * ViewHolder representing a single admin profile row.
     * <p>
     * Holds references to:
     * <ul>
     *     <li>Profile photo image view</li>
     *     <li>Name text view</li>
     *     <li>Email text view</li>
     *     <li>Password text view</li>
     * </ul>
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView photo;
        TextView name, email, password;

        /**
         * Creates a new ViewHolder and binds its view references.
         *
         * @param itemView the root view of the profile item layout
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            photo = itemView.findViewById(R.id.profile_photo);
            name = itemView.findViewById(R.id.profile_name);
            email = itemView.findViewById(R.id.profile_email);
            password = itemView.findViewById(R.id.profile_password);
        }
    }
}
