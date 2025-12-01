package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity that serves as the main dashboard for admin users.
 * <p>
 * From this screen, the admin can navigate to:
 * <ul>
 *     <li>Event management</li>
 *     <li>Image management</li>
 *     <li>User profile management</li>
 *     <li>Their own admin profile</li>
 *     <li>Notification log</li>
 * </ul>
 */
public class AdminDashboardActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * <p>
     * This method sets up the dashboard layout, finds the navigation buttons,
     * and attaches click listeners that launch the corresponding admin activities.
     *
     * @param savedInstanceState the previously saved state of this activity, or {@code null}
     *                           if the activity is being created for the first time
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dashboard);

        // Dashboard navigation buttons (MaterialCardView or other clickable views)
        View browseEventsButton = findViewById(R.id.btnEvents);
        View browseImagesButton = findViewById(R.id.btnImages);
        View browseProfilesButton = findViewById(R.id.btnProfiles);
        View myprofile = findViewById(R.id.btnMyProfile);
        View notifLogButton = findViewById(R.id.btnNotifLog);

        // Navigate to the admin event management screen
        browseEventsButton.setOnClickListener(view ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminEventManagementActivity.class)));

        // Navigate to the admin image management screen
        browseImagesButton.setOnClickListener(view ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminImageManagementActivity.class)));

        // Navigate to the admin user profile management screen
        browseProfilesButton.setOnClickListener(view ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminProfileManagementActivity.class)));

        // Navigate to the admin's own profile screen
        myprofile.setOnClickListener(view ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminProfileActivity.class)));

        // Navigate to the admin notification log screen
        notifLogButton.setOnClickListener(view ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminNotificationsActivity.class)));
    }
}
