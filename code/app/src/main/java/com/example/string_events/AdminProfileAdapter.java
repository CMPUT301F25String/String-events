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

public class AdminProfileAdapter extends RecyclerView.Adapter<AdminProfileAdapter.ViewHolder> {

    public interface OnProfileClickListener {
        void onProfileClick(AdminProfiles profile);
    }

    private final ArrayList<AdminProfiles> profiles;
    private final OnProfileClickListener listener;

    public AdminProfileAdapter(ArrayList<AdminProfiles> profiles, OnProfileClickListener listener) {
        this.profiles = profiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminProfiles profile = profiles.get(position);

        holder.name.setText(profile.getName());
        holder.email.setText(profile.getEmail());
        holder.password.setText("Password: " + profile.getPassword());

        String imgUrl = profile.getProfileImg();

        // bitmap loader
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

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView photo;
        TextView name, email, password;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            photo = itemView.findViewById(R.id.profile_photo);
            name = itemView.findViewById(R.id.profile_name);
            email = itemView.findViewById(R.id.profile_email);
            password = itemView.findViewById(R.id.profile_password);
        }
    }
}
