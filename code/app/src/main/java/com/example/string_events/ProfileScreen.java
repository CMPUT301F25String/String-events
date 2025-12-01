package com.example.string_events;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.UUID;

public class ProfileScreen extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ShapeableImageView profileImageView;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_screen);

        // UI Components
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

        ImageButton entrantHomeButton = findViewById(R.id.btnHome);
        ImageButton orgHomeButton = findViewById(R.id.btnHome2);
        ImageButton cameraButton = findViewById(R.id.btnCamera);
        ImageButton notificationsButton = findViewById(R.id.btnNotification);
        LinearLayout entrantBottomBar = findViewById(R.id.entrantBottomBar);
        LinearLayout organizerBottomBar = findViewById(R.id.organizerBottomBar);

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String currentRole = sharedPreferences.getString("role", null);
        currentUsername = sharedPreferences.getString("user", null);

        // 1. Load saved state (Default to TRUE)
        boolean isNotifEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        notificationSwitch.setChecked(isNotifEnabled);

        // 2. Set Listener
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();

            if (isChecked) {
                // User turned ON -> Check Permission -> Start Service
                checkPermissionAndStartService();
                Toast.makeText(this, "Notifications Enabled", Toast.LENGTH_SHORT).show();
            } else {
                // User turned OFF -> Stop Service
                stopService(new Intent(this, NotificationService.class));
                Toast.makeText(this, "Push Notifications Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Load Profile Info
        if (currentUsername != null) {
            db.collection("users").whereEqualTo("username", currentUsername).limit(1).get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            DocumentSnapshot user = query.getDocuments().get(0);
                            String profileImgUrl = user.getString("profileimg");
                            if (profileImgUrl != null && !profileImgUrl.isEmpty()) {
                                Glide.with(this).load(profileImgUrl).into(profileImageView);
                            }
                            String fullName = user.getString("name");
                            String email = user.getString("email");
                            nameTextView.setText("Name: " + (fullName != null ? fullName : ""));
                            emailTextView.setText("Email: " + (email != null ? email : ""));
                        }
                    });
        }

        setupImagePicker();

        addProfilePhotoButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));
        });

        logOutTextView.setOnClickListener(view -> {
            // STOP SERVICE ON LOGOUT
            stopService(new Intent(this, NotificationService.class));

            SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
            sp.edit().clear().apply();
            Intent i = new Intent(this, WelcomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finishAffinity();
            overridePendingTransition(0, 0);
        });

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

        setupEventsList(currentRole, sharedPreferences);
    }

    // Helper method to handle Android 13 permissions before starting service
    private void checkPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
                // The service will be started in onRequestPermissionsResult or user must toggle again
                return;
            }
        }
        startServiceLogic();
    }

    private void startServiceLogic() {
        Intent serviceIntent = new Intent(this, NotificationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
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
                                profileImageView.setImageURI(selectedImageUri);
                                uploadImageToStorage(selectedImageUri);
                            }
                        }
                    }
                });
    }

    private void uploadImageToStorage(Uri imageUri) {
        if (currentUsername == null) return;
        String filename = "profile_" + UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("profile_images/" + filename);
        Toast.makeText(this, "wait uploading", Toast.LENGTH_SHORT).show();

        ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                updateProfileImageInFirestore(uri.toString());
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(ProfileScreen.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateProfileImageInFirestore(String downloadUrl) {
        db.collection("users").whereEqualTo("username", currentUsername).limit(1).get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String docId = query.getDocuments().get(0).getId();
                        db.collection("users").document(docId).update("profileimg", downloadUrl)
                                .addOnSuccessListener(aVoid -> Toast.makeText(ProfileScreen.this, "Image updated!", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    // Navigation Methods
    public void openCameraScreen() { startActivity(new Intent(ProfileScreen.this, QrScanActivity.class)); }
    public void openNotificationsScreen() { startActivity(new Intent(ProfileScreen.this, NotificationScreen.class)); }
    public void openEditInformationScreen(String username) {
        Intent intent = new Intent(ProfileScreen.this, EditInformationActivity.class);
        intent.putExtra("user", username);
        startActivity(intent);
    }
    public void openLotteryInformationScreen() { startActivity(new Intent(ProfileScreen.this, LotteryInformationActivity.class)); }
    public void openOrganizerEventScreen() { finish(); startActivity(new Intent(ProfileScreen.this, OrganizerEventScreen.class)); }
    public void openEntrantEventScreen() { finish(); startActivity(new Intent(ProfileScreen.this, MainActivity.class)); }

    private void setupEventsList(String currentRole, SharedPreferences sharedPreferences) {
        if (currentUsername == null || currentUsername.isEmpty()) return;
        ArrayList<ProfileEvent> profileEventsList = new ArrayList<>();

        // Logic for Entrants
        if (currentRole != null && currentRole.equals("entrant")) {
            db.collection("events")
                    .where(Filter.or(
                            Filter.arrayContains("waitlist", currentUsername),
                            Filter.arrayContains("invited", currentUsername),
                            Filter.arrayContains("attendees", currentUsername),
                            Filter.arrayContains("cancelled", currentUsername)))
                    .get()
                    .addOnSuccessListener(snap -> {
                        for (QueryDocumentSnapshot d : snap) {
                            // NEW: Extract Timestamps and convert to Date
                            com.google.firebase.Timestamp startTs = d.getTimestamp("startAt");
                            com.google.firebase.Timestamp endTs = d.getTimestamp("endAt");

                            java.util.Date startDate = (startTs != null) ? startTs.toDate() : null;
                            java.util.Date endDate = (endTs != null) ? endTs.toDate() : null;

                            profileEventsList.add(new ProfileEvent(
                                    d.getId(),
                                    d.getString("imageUrl"),
                                    d.getString("title"),
                                    startDate, // Pass start date
                                    endDate,   // Pass end date
                                    d.getString("location")
                            ));
                        }
                        setupRecyclerView(profileEventsList, sharedPreferences);
                    });
        }
        // Logic for Organizers
        else {
            db.collection("events").whereEqualTo("creator", currentUsername).get()
                    .addOnSuccessListener(snap -> {
                        for (QueryDocumentSnapshot d : snap) {
                            // NEW: Extract Timestamps and convert to Date
                            com.google.firebase.Timestamp startTs = d.getTimestamp("startAt");
                            com.google.firebase.Timestamp endTs = d.getTimestamp("endAt");

                            java.util.Date startDate = (startTs != null) ? startTs.toDate() : null;
                            java.util.Date endDate = (endTs != null) ? endTs.toDate() : null;

                            profileEventsList.add(new ProfileEvent(
                                    d.getId(),
                                    d.getString("imageUrl"),
                                    d.getString("title"),
                                    startDate, // Pass start date
                                    endDate,   // Pass end date
                                    d.getString("location")
                            ));
                        }
                        setupRecyclerView(profileEventsList, sharedPreferences);
                    });
        }
    }

    private void setupRecyclerView(ArrayList<ProfileEvent> profileEventsList, SharedPreferences sharedPreferences) {
        RecyclerView profileEventRecyclerview = findViewById(R.id.profile_events_recyclerView);
        ProfileEventsAdapter profileEventsAdapter = new ProfileEventsAdapter(this, profileEventsList, sharedPreferences);
        profileEventRecyclerview.setAdapter(profileEventsAdapter);
        profileEventRecyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
    }
}