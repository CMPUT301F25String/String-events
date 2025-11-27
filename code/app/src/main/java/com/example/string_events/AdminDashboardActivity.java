package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dashboard);

        ImageButton browseEventsButton = findViewById(R.id.btnEvents);
        ImageButton browseImagesButton = findViewById(R.id.btnImages);
        ImageButton browseProfilesButton = findViewById(R.id.btnProfiles);
        ImageButton myprofile = findViewById(R.id.btnMyProfile);
        ImageButton notifLogButton = findViewById(R.id.btnNotifLog);

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
