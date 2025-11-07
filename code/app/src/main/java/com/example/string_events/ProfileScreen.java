package com.example.string_events;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;

public class ProfileScreen extends AppCompatActivity {

    ArrayList<ProfileEvent> profileEventsList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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
        ConstraintLayout lotteryInfoLayout = findViewById(R.id.lottery_info_layout);
        ImageButton deleteProfileImageButton = findViewById(R.id.delete_profile_button);

        // bottom bar buttons
        ImageButton homeButton = findViewById(R.id.btnHome);
        ImageButton cameraButton = findViewById(R.id.btnCamera);
        ImageButton notificationsButton = findViewById(R.id.btnNotification);

        // get the user's username, full name, and email to be displayed
        Intent intent = getIntent();
        String username = intent.getStringExtra("user");
        String fullName = intent.getStringExtra("fullName");
        String email = intent.getStringExtra("email");

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        // get the current role of the user and switch it to either entrant or organizer
        String currentRole = sharedPreferences.getString("role", null);
        assert currentRole != null;

        // display name and email immediately
        nameTextView.setText("Name: " + (fullName != null ? fullName : ""));
        emailTextView.setText("Email: " + (email != null ? email : ""));

        logOutTextView.setOnClickListener(view -> {
            SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
            sp.edit().clear().apply();
            Intent i = new Intent(ProfileScreen.this, WelcomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finishAffinity();
            overridePendingTransition(0, 0);
        });

        homeButton.setOnClickListener(view -> {
            if (currentRole.equals("entrant")) {
                openEntrantEventScreen();
            } else {
                openOrganizerEventScreen();
            }
        });

        notificationsButton.setOnClickListener(view -> openNotificationsScreen());

        editProfileTextView.setOnClickListener(view -> openEditInformationScreen(username));

        lotteryInfoLayout.setOnClickListener(view -> openLotteryInformationScreen());

        switchRolesButton.setOnClickListener(view -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (currentRole.equals("entrant")) {
                editor.putString("role", "organizer");
                editor.apply();
                openOrganizerEventScreen();
            } else {
                editor.putString("role", "organizer");
                editor.apply();
                openEntrantEventScreen();
            }
        });

        deleteProfileImageButton.setOnClickListener(v -> {
            if (username == null || username.isEmpty()) {
                Toast.makeText(ProfileScreen.this, "No user to delete", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users")
                    .whereEqualTo("username", username)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            String docId = query.getDocuments().get(0).getId();
                            db.collection("users").document(docId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(ProfileScreen.this, "Profile deleted", Toast.LENGTH_SHORT).show();
                                        Intent i = new Intent(ProfileScreen.this, WelcomeActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        ProfileScreen.this.startActivity(i);
                                        ProfileScreen.this.finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(ProfileScreen.this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(ProfileScreen.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(ProfileScreen.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

    }

    public void openNotificationsScreen() {
        Intent myIntent = new Intent(ProfileScreen.this, NotificationScreen.class);
        startActivity(myIntent);
    }

    public void openEditInformationScreen(String username) {
        Intent intent = new Intent(ProfileScreen.this, EditInformationActivity.class);
        intent.putExtra("user", username);
        startActivity(intent);
    }

    public void openLotteryInformationScreen() {
        Intent intent = new Intent(ProfileScreen.this, LotteryInformationActivity.class);
        startActivity(intent);
    }

    public void openOrganizerEventScreen() {
        Intent myIntent = new Intent(ProfileScreen.this, OrganizerEventScreen.class);
        startActivity(myIntent);
    }

    public void openEntrantEventScreen() {
        Intent myIntent = new Intent(ProfileScreen.this, MainActivity.class);
        startActivity(myIntent);
    }
}
