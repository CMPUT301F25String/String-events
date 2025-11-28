package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dashboard);

        // Updated types from ImageButton to View to support MaterialCardView layout
        View browseEventsButton = findViewById(R.id.btnEvents);
        View browseImagesButton = findViewById(R.id.btnImages);
        View browseProfilesButton = findViewById(R.id.btnProfiles);
        View myprofile = findViewById(R.id.btnMyProfile);
        View notifLogButton = findViewById(R.id.btnNotifLog);

        browseEventsButton.setOnClickListener(view ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminEventManagementActivity.class)));
        browseImagesButton.setOnClickListener(view ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminImageManagementActivity.class)));
        browseProfilesButton.setOnClickListener(view ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminProfileManagementActivity.class)));
        myprofile.setOnClickListener(view ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminProfileActivity.class)));
        notifLogButton.setOnClickListener(view ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminNotificationsActivity.class)));
    }
}