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
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Profile screen for an entrant or organizer user.
 * <p>
 * This activity is responsible for:
 * <ul>
 *     <li>Displaying basic user information (name, email, profile photo).</li>
 *     <li>Toggling push notification settings and starting/stopping {@link NotificationService}.</li>
 *     <li>Switching between entrant and organizer roles.</li>
 *     <li>Uploading and updating the user's profile photo.</li>
 *     <li>Displaying joined (entrant) or created (organizer) events in a horizontal list.</li>
 *     <li>Navigating to related screens (edit profile, lottery info, home, camera, notifications).</li>
 * </ul>
 */
public class ProfileScreen extends AppCompatActivity {

    /**
     * Firestore instance used for loading and updating user and event data.
     */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Firebase Storage instance used for profile image uploads.
     */
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    /**
     * Launcher used for picking an image from the gallery.
     */
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    /**
     * ImageView used to display the user's profile photo.
     */
    private ShapeableImageView profileImageView;

    /**
     * Currently logged-in username loaded from shared preferences.
     */
    private String currentUsername;

    /**
     * Called when the activity is first created.
     * <p>
     * This method:
     * <ul>
     *     <li>Initializes the UI components.</li>
     *     <li>Loads the user's name, email, and profile image.</li>
     *     <li>Restores and handles the notification toggle state.</li>
     *     <li>Configures the profile photo picker and upload flow.</li>
     *     <li>Configures logout, role switching, and navigation buttons.</li>
     *     <li>Loads the user's events (joined or created) into a horizontal RecyclerView.</li>
     * </ul>
     *
     * @param savedInstanceState previously saved instance state, or {@code null} if first creation
     */
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

        // Load basic user info from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String currentRole = sharedPreferences.getString("role", null);
        currentUsername = sharedPreferences.getString("user", null);
        String fullName = sharedPreferences.getString("name", null);
        String email = sharedPreferences.getString("email", null);

        nameTextView.setText("Name: " + (fullName != null ? fullName : ""));
        emailTextView.setText("Email: " + (email != null ? email : ""));

        // 1. Load saved notification switch state (default to true)
        boolean isNotifEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        notificationSwitch.setChecked(isNotifEnabled);

        // 2. Handle notification switch changes
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();

            if (isChecked) {
                // User turned notifications ON -> check permission (Android 13+) then start service
                checkPermissionAndStartService();
                Toast.makeText(this, "Notifications Enabled", Toast.LENGTH_SHORT).show();
            } else {
                // User turned notifications OFF -> stop the background notification service
                stopService(new Intent(this, NotificationService.class));
                Toast.makeText(this, "Push Notifications Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Load profile image from Firestore if available
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
                            }
                        }
                    });
        }

        // Initialize image picker launcher and upload handler
        setupImagePicker();

        // Trigger gallery picker when the "add photo" button is pressed
        addProfilePhotoButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));
        });

        // Handle logout: stop notification service, clear preferences, and go back to welcome screen
        logOutTextView.setOnClickListener(view -> {
            stopService(new Intent(this, NotificationService.class));

            SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
            sp.edit().clear().apply();
            Intent i = new Intent(this, WelcomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finishAffinity();
            overridePendingTransition(0, 0);
        });

        // Configure role-dependent UI labels and bottom navigation visibility
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

        // Bottom bar navigation handlers
        entrantHomeButton.setOnClickListener(view -> openEntrantEventScreen());
        orgHomeButton.setOnClickListener(view -> openOrganizerEventScreen());
        cameraButton.setOnClickListener(view -> openCameraScreen());
        notificationsButton.setOnClickListener(view -> openNotificationsScreen());
        editProfileTextView.setOnClickListener(view -> openEditInformationScreen(currentUsername));
        lotteryInfoLayout.setOnClickListener(view -> openLotteryInformationScreen());

        // Switch roles between entrant and organizer and navigate accordingly
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

        // Load the relevant event list for this profile (joined vs created)
        setupEventsList(currentRole, sharedPreferences);
    }

    /**
     * Checks POST_NOTIFICATIONS permission on Android 13+ before starting the notification service.
     * <p>
     * If the permission is not granted, a permission request is launched. The user will need to
     * toggle the switch again after granting permission for the service to start.
     */
    private void checkPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
                return;
            }
        }
        startServiceLogic();
    }

    /**
     * Starts the {@link NotificationService} as a foreground service on Android O+,
     * or as a normal service on older versions.
     */
    private void startServiceLogic() {
        Intent serviceIntent = new Intent(this, NotificationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    /**
     * Sets up the image picker launcher to obtain a profile image from the gallery.
     * <p>
     * When an image is selected:
     * <ul>
     *     <li>The selected image is immediately displayed locally.</li>
     *     <li>The image is uploaded to Firebase Storage.</li>
     *     <li>Upon upload success, the Firestore user document is updated with the image URL.</li>
     * </ul>
     */
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
                            } else if (intent.getClipData() != null
                                    && intent.getClipData().getItemCount() > 0) {
                                ClipData clipData = intent.getClipData();
                                selectedImageUri = clipData.getItemAt(0).getUri();
                            }

                            if (selectedImageUri != null) {
                                // Show the image locally
                                profileImageView.setImageURI(selectedImageUri);
                                // Upload to Firebase Storage
                                uploadImageToStorage(selectedImageUri);
                            }
                        }
                    }
                });
    }

    /**
     * Uploads the selected profile image to Firebase Storage and, on success,
     * triggers an update to the user's Firestore document.
     *
     * @param imageUri URI of the picked image
     */
    private void uploadImageToStorage(Uri imageUri) {
        if (currentUsername == null) return;

        String filename = "profile_" + UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("profile_images/" + filename);
        Toast.makeText(this, "wait uploading", Toast.LENGTH_SHORT).show();

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri ->
                                updateProfileImageInFirestore(uri.toString())
                        ))
                .addOnFailureListener(e ->
                        Toast.makeText(ProfileScreen.this,
                                "Upload failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    /**
     * Updates the "profileimg" field of the current user in Firestore with the given URL.
     *
     * @param downloadUrl public download URL of the uploaded profile image
     */
    private void updateProfileImageInFirestore(String downloadUrl) {
        db.collection("users")
                .whereEqualTo("username", currentUsername)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String docId = query.getDocuments().get(0).getId();
                        db.collection("users")
                                .document(docId)
                                .update("profileimg", downloadUrl)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(ProfileScreen.this,
                                                "Image updated!",
                                                Toast.LENGTH_SHORT).show());
                    }
                });
    }

    // Navigation Methods

    /**
     * Opens the QR scan screen for scanning event codes.
     */
    public void openCameraScreen() {
        startActivity(new Intent(ProfileScreen.this, QrScanActivity.class));
    }

    /**
     * Opens the notification center screen showing in-app notifications.
     */
    public void openNotificationsScreen() {
        startActivity(new Intent(ProfileScreen.this, NotificationScreen.class));
    }

    /**
     * Opens the edit information screen for the specified username.
     *
     * @param username the username whose profile is being edited
     */
    public void openEditInformationScreen(String username) {
        Intent intent = new Intent(ProfileScreen.this, EditInformationActivity.class);
        intent.putExtra("user", username);
        startActivity(intent);
    }

    /**
     * Opens the lottery information screen, which explains lottery mechanics to the user.
     */
    public void openLotteryInformationScreen() {
        startActivity(new Intent(ProfileScreen.this, LotteryInformationActivity.class));
    }

    /**
     * Opens the organizer event list screen and finishes the current profile activity.
     */
    public void openOrganizerEventScreen() {
        finish();
        startActivity(new Intent(ProfileScreen.this, OrganizerEventScreen.class));
    }

    /**
     * Opens the entrant main event list screen and finishes the current profile activity.
     */
    public void openEntrantEventScreen() {
        finish();
        startActivity(new Intent(ProfileScreen.this, MainActivity.class));
    }

    /**
     * Loads events for the current user and role, then passes them to the RecyclerView setup.
     * <p>
     * Behavior:
     * <ul>
     *     <li>If role is {@code "entrant"}:
     *         <ul>
     *             <li>Queries events where the user is in "waitlist", "invited", "attendees",
     *             or "cancelled" arrays.</li>
     *             <li>Creates {@link ProfileEvent} objects for those events.</li>
     *         </ul>
     *     </li>
     *     <li>Otherwise (organizer roles):
     *         <ul>
     *             <li>Queries events where the "creator" equals the current username.</li>
     *             <li>Creates {@link ProfileEvent} objects for events created by this user.</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param currentRole       role stored in shared preferences (e.g., "entrant" or "organizer")
     * @param sharedPreferences shared preferences used later in the adapter for role checks
     */
    private void setupEventsList(String currentRole, SharedPreferences sharedPreferences) {
        if (currentUsername == null || currentUsername.isEmpty()) return;
        ArrayList<ProfileEvent> profileEventsList = new ArrayList<>();

        // Logic for entrants: events where this user appears in participation-related arrays
        if (currentRole != null && currentRole.equals("entrant")) {
            db.collection("events")
                    .where(
                            Filter.or(
                                    Filter.arrayContains("waitlist", currentUsername),
                                    Filter.arrayContains("invited", currentUsername),
                                    Filter.arrayContains("attendees", currentUsername),
                                    Filter.arrayContains("cancelled", currentUsername)
                            )
                    )
                    .get()
                    .addOnSuccessListener(snap -> {
                        for (QueryDocumentSnapshot d : snap) {
                            // Extract timestamps and convert to Date
                            com.google.firebase.Timestamp startTs = d.getTimestamp("startAt");
                            com.google.firebase.Timestamp endTs = d.getTimestamp("endAt");

                            java.util.Date startDate = (startTs != null) ? startTs.toDate() : null;
                            java.util.Date endDate = (endTs != null) ? endTs.toDate() : null;

                            profileEventsList.add(new ProfileEvent(
                                    d.getId(),
                                    d.getString("imageUrl"),
                                    d.getString("title"),
                                    startDate,
                                    endDate,
                                    d.getString("location")
                            ));
                        }
                        setupRecyclerView(profileEventsList, sharedPreferences);
                    });
        }
        // Logic for organizers: events created by the current user
        else {
            db.collection("events")
                    .whereEqualTo("creator", currentUsername)
                    .get()
                    .addOnSuccessListener(snap -> {
                        for (QueryDocumentSnapshot d : snap) {
                            // Extract timestamps and convert to Date
                            com.google.firebase.Timestamp startTs = d.getTimestamp("startAt");
                            com.google.firebase.Timestamp endTs = d.getTimestamp("endAt");

                            java.util.Date startDate = (startTs != null) ? startTs.toDate() : null;
                            java.util.Date endDate = (endTs != null) ? endTs.toDate() : null;

                            profileEventsList.add(new ProfileEvent(
                                    d.getId(),
                                    d.getString("imageUrl"),
                                    d.getString("title"),
                                    startDate,
                                    endDate,
                                    d.getString("location")
                            ));
                        }
                        setupRecyclerView(profileEventsList, sharedPreferences);
                    });
        }
    }

    /**
     * Configures the horizontal RecyclerView used to display the user's profile events.
     *
     * @param profileEventsList list of {@link ProfileEvent} instances to display
     * @param sharedPreferences shared preferences containing the current user role
     */
    private void setupRecyclerView(ArrayList<ProfileEvent> profileEventsList,
                                   SharedPreferences sharedPreferences) {
        RecyclerView profileEventRecyclerview = findViewById(R.id.profile_events_recyclerView);
        ProfileEventsAdapter profileEventsAdapter =
                new ProfileEventsAdapter(this, profileEventsList, sharedPreferences);
        profileEventRecyclerview.setAdapter(profileEventsAdapter);
        profileEventRecyclerview.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
    }
}
