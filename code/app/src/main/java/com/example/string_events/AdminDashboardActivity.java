package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

        // each button in the admin dashboard opens its respective screen
        browseEventsButton.setOnClickListener(view ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminEventManagementActivity.class)));
        browseImagesButton.setOnClickListener(view ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminImageManagementActivity.class)));
        browseProfilesButton.setOnClickListener(view ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminProfileManagementActivity.class)));

    }
}
