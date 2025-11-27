package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Admin landing screen for navigating to administrative features.
 * <p>
 * The layout {@code R.layout.admin_dashboard} is expected to define the UI components
 * (e.g., buttons) that link to other admin flows.
 *
 * @since 1.0
 */
public class AdminDashboardActivity extends AppCompatActivity {

    /**
     * Inflates the admin dashboard UI.
     *
     * @param savedInstanceState state bundle provided by Android, may be {@code null}
     */
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
