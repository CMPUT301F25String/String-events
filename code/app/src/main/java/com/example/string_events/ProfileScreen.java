package com.example.string_events;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProfileScreen extends AppCompatActivity {

    ArrayList<ProfileEvent> profileEventsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_screen);

        // screen buttons
        TextView editProfileTextView = findViewById(R.id.edit_textView);
        TextView logOutTextView = findViewById(R.id.logOut_textView);
        SwitchCompat notificationSwitch = findViewById(R.id.notification_switch);
        ImageView profileImageView = findViewById(R.id.profile_imageView);
        ImageButton switchRolesButton = findViewById(R.id.switch_roles_button);
        TextView nameTextView = findViewById(R.id.name_textView);
        TextView emailTextView = findViewById(R.id.email_textView);
        ImageView infoImageView = findViewById(R.id.info_imageButton);
        ImageButton deleteProfileImageButton = findViewById(R.id.delete_profile_button);

        // bottom bar buttons
        ImageButton homeImageButton = findViewById(R.id.btnHome);
        ImageButton cameraImageButton = findViewById(R.id.btnCamera);
        ImageButton notificationImageButton = findViewById(R.id.btnNotification);
        ImageButton profileImageButton = findViewById(R.id.btnProfile);

        switchRolesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
                // get the current role of the user and switch it to either entrant or organizer
                String currentRole = sharedPreferences.getString("role", null);
                assert currentRole != null;

                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (currentRole.equals("entrant")) {
                    editor.putString("role", "organizer");
                    editor.apply();
                    openOrganizerEventScreen();
                }
                else {
                    editor.putString("role", "organizer");
                    editor.apply();
                    openEntrantEventScreen();
                }
            }
        });

    }

    private void setupRecyclerView() {
        RecyclerView profileEventsRecyclerview = findViewById(R.id.profile_events_recyclerView);
        ProfileEventsAdapter profileEventsAdapter = new ProfileEventsAdapter(this, profileEventsList);
        profileEventsRecyclerview.setAdapter(profileEventsAdapter);
        profileEventsRecyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }

    public void openOrganizerEventScreen() {
        Context context = ProfileScreen.this;
        Intent myIntent = new Intent(context, OrganizerEventScreen.class);
        context.startActivity(myIntent);
    }

    public void openEntrantEventScreen() {
        Context context = ProfileScreen.this;
        Intent myIntent = new Intent(context, MainActivity.class);
        context.startActivity(myIntent);
    }
}