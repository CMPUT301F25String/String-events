package com.example.string_events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminProfileAdapter extends RecyclerView.Adapter<AdminProfileAdapter.ProfileViewHolder> {

    private final List<AdminProfiles> profiles;
    private final OnProfileClickListener listener;

    public interface OnProfileClickListener {
        void onProfileClick(AdminProfiles profile);
    }

    public AdminProfileAdapter(List<AdminProfiles> profiles, OnProfileClickListener listener) {
        this.profiles = profiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_profile, parent, false);
        return new ProfileViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, password;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.profile_name);
            email = itemView.findViewById(R.id.profile_email);
            password = itemView.findViewById(R.id.profile_password);
        }
    }
}
