package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
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
    }
}
