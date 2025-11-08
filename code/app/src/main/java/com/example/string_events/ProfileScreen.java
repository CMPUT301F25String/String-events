package com.example.string_events;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Profile screen for entrants/organizers.
 * <p>
 * Shows basic user info, allows switching roles, logging out, and navigating to
 * notifications and edit-information screens. Also lists events in which the
 * current user is on the waitlist.
 */
public class ProfileScreen extends AppCompatActivity {

    ArrayList<ProfileEvent> profileEventsList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Inflates the profile UI, binds buttons, loads user info from
     * {@code SharedPreferences}, sets up role switching and logout,
     * and queries waitlisted events to populate the horizontal carousel.
     *
     * @param savedInstanceState previously saved instance state, or {@code null}
     */
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

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        // get the user's role, username, full name, and email to be displayed
        String currentRole = sharedPreferences.getString("role", null);
        assert currentRole != null;
        String username = sharedPreferences.getString("user", null);
        String fullName = sharedPreferences.getString("fullName", null);
        String email = sharedPreferences.getString("email", null);

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
                editor.putString("role", "entrant");
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
        // setup recyclerview in profile screen with events
        if (username != null && !username.isEmpty()) {
            db.collection("events")
                    .whereArrayContains("waitlist", username)
                    // .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(snap -> {
                        if (snap.isEmpty()) {
                            Log.d("Firestore", "no events found with waitlists that contain user:" + username);
                        }
                        for (QueryDocumentSnapshot d : snap) {
                            String imageUrl = d.getString("imageUrl");
                            String name = d.getString("title");
                            // String startDateTime = d.getString("startAt");
                            // String endDateTime = d.getString("endAt");
                            String location = d.getString("location");
                            ProfileEvent profileEvent = new ProfileEvent(imageUrl, name, null, null, location);
                            profileEventsList.add(profileEvent);
                        }
                        setupRecyclerView(profileEventsList);
                    })
                    .addOnFailureListener(e -> {
                        // It's crucial to handle query failures
                        Log.e("Firestore", "error getting waitlisted events", e);
                    });
        }
    }

    /**
     * Opens the notifications screen.
     */
    public void openNotificationsScreen() {
        Intent myIntent = new Intent(ProfileScreen.this, NotificationScreen.class);
        startActivity(myIntent);
    }

    /**
     * Opens the edit-information screen for the current user.
     *
     * @param username username used to locate the user document
     */
    public void openEditInformationScreen(String username) {
        Intent intent = new Intent(ProfileScreen.this, EditInformationActivity.class);
        intent.putExtra("user", username);
        startActivity(intent);
    }

    /**
     * Opens the static lottery information screen.
     */
    public void openLotteryInformationScreen() {
        Intent intent = new Intent(ProfileScreen.this, LotteryInformationActivity.class);
        startActivity(intent);
    }

    /**
     * Switches to the organizer home flow.
     */
    public void openOrganizerEventScreen() {
        Intent myIntent = new Intent(ProfileScreen.this, OrganizerEventScreen.class);
        finish();
        startActivity(myIntent);
    }

    /**
     * Switches to the entrant (user) home flow.
     */
    public void openEntrantEventScreen() {
        Intent myIntent = new Intent(ProfileScreen.this, MainActivity.class);
        finish();
        startActivity(myIntent);
    }

    /**
     * Configures the profile events carousel with a horizontal {@link LinearLayoutManager}.
     *
     * @param profileEventsList list of events to render
     */
    private void setupRecyclerView(ArrayList<ProfileEvent> profileEventsList) {
        RecyclerView profileEventRecyclerview = findViewById(R.id.profile_events_recyclerView);
        ProfileEventsAdapter profileEventsAdapter = new ProfileEventsAdapter(this, profileEventsList);
        profileEventRecyclerview.setAdapter(profileEventsAdapter);
        profileEventRecyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
    }
}
