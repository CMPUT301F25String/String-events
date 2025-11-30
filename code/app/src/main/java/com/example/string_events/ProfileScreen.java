package com.example.string_events;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Profile screen for entrants/organizers.
 * <p>
 * Shows basic user info, allows switching roles, logging out, and navigating to
 * notifications and edit-information screens. Also lists events in which the
 * current user is on the waitlist.
 */
public class ProfileScreen extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ShapeableImageView profileImageView;
    private String currentUsername;

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
        profileImageView = findViewById(R.id.profile_imageView);
        ImageButton switchRolesButton = findViewById(R.id.switch_roles_button);
        TextView nameTextView = findViewById(R.id.name_textView);
        TextView emailTextView = findViewById(R.id.email_textView);
        ConstraintLayout lotteryInfoLayout = findViewById(R.id.lottery_info_layout);
        TextView profileEventsTextView = findViewById(R.id.profile_events_textView);
        View addProfilePhotoButton = findViewById(R.id.btn_add_profile_photo);

        // bottom bar buttons
        ImageButton entrantHomeButton = findViewById(R.id.btnHome);
        ImageButton orgHomeButton = findViewById(R.id.btnHome2);
        ImageButton cameraButton = findViewById(R.id.btnCamera);
        ImageButton notificationsButton = findViewById(R.id.btnNotification);
        LinearLayout entrantBottomBar = findViewById(R.id.entrantBottomBar);
        LinearLayout organizerBottomBar = findViewById(R.id.organizerBottomBar);

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        // get the user's role, username, full name, and email to be displayed
        String currentRole = sharedPreferences.getString("role", null);
        currentUsername = sharedPreferences.getString("user", null);
        String fullName = sharedPreferences.getString("name", null);
        String email = sharedPreferences.getString("email", null);

        // display name and email immediately
        nameTextView.setText("Name: " + (fullName != null ? fullName : ""));
        emailTextView.setText("Email: " + (email != null ? email : ""));

        // Fetch and load profile image using bitmapping on a background thread
        if (currentUsername != null) {
            db.collection("users")
                    .whereEqualTo("username", currentUsername)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            String profileImgUrl = query.getDocuments().get(0).getString("profileimg");
                            if (profileImgUrl != null && !profileImgUrl.isEmpty()) {
                                Glide.with(this).load(profileImgUrl).into(profileImageView);
//                                new Thread(() -> {
//                                    try {
//                                        java.net.URL url = new java.net.URL(profileImgUrl);
//                                        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
//                                        connection.setDoInput(true);
//                                        connection.connect();
//                                        java.io.InputStream input = connection.getInputStream();
//                                        final android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(input);
//
//                                        profileImageView.post(() -> profileImageView.setImageBitmap(bitmap));
//                                    } catch (Exception e) {
//                                        Log.e("ProfileScreen", "Error loading profile image", e);
//                                    }
//                                }).start();
                            }
                        }
                    });
        }

        // Initialize Image Picker
        setupImagePicker();

        // Set listener for the add photo button
        addProfilePhotoButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));
        });

        logOutTextView.setOnClickListener(view -> {
            SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
            sp.edit().clear().apply();
            Intent i = new Intent(this, WelcomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finishAffinity();
            overridePendingTransition(0, 0);
        });

        // check if the user is currently an event entrant or an event organizer
        if (currentRole != null && currentRole.equals("entrant")) {
            switchRolesButton.setBackgroundResource(R.drawable.switch_to_organizer_button);
            profileEventsTextView.setText("Joined Events:");
            entrantBottomBar.setVisibility(View.VISIBLE);
            organizerBottomBar.setVisibility(View.GONE);
        } else {
            switchRolesButton.setBackgroundResource(R.drawable.switch_to_entrant_button);
            profileEventsTextView.setText("Created Events:");
            entrantBottomBar.setVisibility(View.GONE);
            organizerBottomBar.setVisibility(View.VISIBLE);
        }

        entrantHomeButton.setOnClickListener(view -> openEntrantEventScreen());

        orgHomeButton.setOnClickListener(view -> openOrganizerEventScreen());

        cameraButton.setOnClickListener(view -> openCameraScreen());

        notificationsButton.setOnClickListener(view -> openNotificationsScreen());

        editProfileTextView.setOnClickListener(view -> openEditInformationScreen(currentUsername));

        lotteryInfoLayout.setOnClickListener(view -> openLotteryInformationScreen());

        switchRolesButton.setOnClickListener(view -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (currentRole != null && currentRole.equals("entrant")) {
                editor.putString("role", "organizer");
                editor.apply();
                openOrganizerEventScreen();
            } else {
                editor.putString("role", "entrant");
                editor.apply();
                openEntrantEventScreen();
            }
        });

        // setup recyclerview in profile screen with either joined events or created events
        if (currentUsername != null && !currentUsername.isEmpty()) {
            ArrayList<ProfileEvent> profileEventsList = new ArrayList<>();
            if (currentRole != null && currentRole.equals("entrant")) {
                db.collection("events")
                        // gets an event if a user is in any of those lists
                        .where(Filter.or(
                                Filter.arrayContains("waitlist", currentUsername),
                                Filter.arrayContains("invited", currentUsername),
                                Filter.arrayContains("attendees", currentUsername),
                                Filter.arrayContains("cancelled", currentUsername)))
                        .get()
                        .addOnSuccessListener(snap -> {
                            if (snap.isEmpty()) {
                                Log.d("Firestore", "no events found with waitlists that contain user:" + currentUsername);
                            }
                            for (QueryDocumentSnapshot d : snap) {
                                String eventId = d.getId();
                                String imageUrl = d.getString("imageUrl");
                                String name = d.getString("title");
                                String location = d.getString("location");
                                ProfileEvent profileEvent = new ProfileEvent(eventId, imageUrl, name, null, null, location);
                                profileEventsList.add(profileEvent);
                            }
                            setupRecyclerView(profileEventsList, sharedPreferences);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "error getting waitlisted events", e);
                        });
            } else {
                db.collection("events")
                        .whereEqualTo("creator", currentUsername)
                        .get()
                        .addOnSuccessListener(snap -> {
                            if (snap.isEmpty()) {
                                Log.d("Firestore", "no events found with creator:" + currentUsername);
                            }
                            for (QueryDocumentSnapshot d : snap) {
                                String eventId = d.getId();
                                String imageUrl = d.getString("imageUrl");
                                String name = d.getString("title");
                                String location = d.getString("location");
                                ProfileEvent profileEvent = new ProfileEvent(eventId, imageUrl, name, null, null, location);
                                profileEventsList.add(profileEvent);
                            }
                            setupRecyclerView(profileEventsList, sharedPreferences);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "error getting waitlisted events", e);
                        });
            }
        }
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        Uri selectedImageUri = null;
                        if (intent != null) {
                            if (intent.getData() != null) {
                                selectedImageUri = intent.getData();
                            } else if (intent.getClipData() != null && intent.getClipData().getItemCount() > 0) {
                                ClipData clipData = intent.getClipData();
                                selectedImageUri = clipData.getItemAt(0).getUri();
                            }

                            if (selectedImageUri != null) {
                                // Display locally immediately
                                profileImageView.setImageURI(selectedImageUri);
                                // Upload to Firebase Storage
                                uploadImageToStorage(selectedImageUri);
                            }
                        }
                    }
                });
    }

    private void uploadImageToStorage(Uri imageUri) {
        if (currentUsername == null) return;

        // Create a reference to "profile_images/[filename]"
        String filename = "profile_" + UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("profile_images/" + filename);

        Toast.makeText(this, "wait uploading", Toast.LENGTH_SHORT).show();

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        updateProfileImageInFirestore(downloadUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileScreen.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfileImageInFirestore(String downloadUrl) {
        db.collection("users")
                .whereEqualTo("username", currentUsername)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String docId = query.getDocuments().get(0).getId();
                        db.collection("users").document(docId)
                                .update("profileimg", downloadUrl)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(ProfileScreen.this, " image updated!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(ProfileScreen.this, "Failed to update", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    /**
     * Opens the notifications screen.
     */
    public void openCameraScreen() {
        Intent myIntent = new Intent(ProfileScreen.this, QrScanActivity.class);
        startActivity(myIntent);
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
    private void setupRecyclerView(ArrayList<ProfileEvent> profileEventsList, SharedPreferences sharedPreferences) {
        RecyclerView profileEventRecyclerview = findViewById(R.id.profile_events_recyclerView);
        ProfileEventsAdapter profileEventsAdapter = new ProfileEventsAdapter(this, profileEventsList, sharedPreferences);
        profileEventRecyclerview.setAdapter(profileEventsAdapter);
        profileEventRecyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
    }
}