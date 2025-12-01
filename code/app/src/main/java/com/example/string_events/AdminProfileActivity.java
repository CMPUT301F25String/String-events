package com.example.string_events;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

/**
 * Activity that displays and manages the admin's profile.
 * <p>
 * This screen allows the admin to:
 * <ul>
 *     <li>View their name, email, and password.</li>
 *     <li>View and update their profile image.</li>
 *     <li>Navigate back to the admin dashboard.</li>
 *     <li>Log out and clear stored user information.</li>
 * </ul>
 */
public class AdminProfileActivity extends AppCompatActivity {

    /**
     * TextView showing the admin's name.
     */
    private TextView nameValue;

    /**
     * TextView showing the admin's email.
     */
    private TextView emailValue;

    /**
     * TextView showing the admin's password.
     */
    private TextView passwordValue;

    /**
     * ImageView displaying the admin's profile photo.
     */
    private ImageView profileImageView;

    /**
     * Firestore instance used to retrieve and update admin profile data.
     */
    private FirebaseFirestore firestore;

    /**
     * Firebase Storage instance used to upload profile images.
     */
    private FirebaseStorage storage;

    /**
     * Firestore document ID of the current admin profile, used for updates.
     */
    private String docId;

    /**
     * Launcher used to handle the result of the image picker intent.
     */
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    /**
     * Called when the activity is first created.
     * <p>
     * This method:
     * <ul>
     *     <li>Initializes UI components and Firebase instances.</li>
     *     <li>Resolves the admin username from intent extras or shared preferences.</li>
     *     <li>Loads the admin profile from Firestore.</li>
     *     <li>Sets up navigation (back) and logout actions.</li>
     *     <li>Initializes an image picker for updating the profile photo.</li>
     * </ul>
     *
     * @param savedInstanceState previously saved state, or {@code null} if created fresh
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adminprofile);

        nameValue = findViewById(R.id.nameValue);
        emailValue = findViewById(R.id.emailValue);
        passwordValue = findViewById(R.id.passwordValue);
        profileImageView = findViewById(R.id.profileImageView);
        // Button or clickable view that triggers adding/updating a profile photo
        View addPhotoBtn = findViewById(R.id.btn_add_profile_photo);

        ImageView backButton = findViewById(R.id.backButton);
        TextView logOutTextView = findViewById(R.id.logOutTextView);

        // Navigate back to the admin dashboard
        backButton.setOnClickListener(v -> {
            Intent i = new Intent(this, AdminDashboardActivity.class);
            startActivity(i);
            finish();
        });

        // Log out and clear stored user info, then navigate to the welcome screen
        logOutTextView.setOnClickListener(v -> {
            SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
            sp.edit().clear().apply();
            Intent i = new Intent(this, WelcomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Resolve admin username from intent extras or shared preferences
        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        String adminUsername = getIntent().getStringExtra("adminUsername");
        if (adminUsername == null || adminUsername.isEmpty()) {
            adminUsername = sp.getString("adminUsername", null);
        }

        if (adminUsername == null || adminUsername.isEmpty()) {
            Toast.makeText(this, "Missing admin username", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalAdminUsername = adminUsername;

        // Query Firestore for the admin profile document
        firestore.collection("admins")
                .whereEqualTo("username", finalAdminUsername)
                .limit(1)
                .get()
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) {
                        Toast.makeText(this, "Admin profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    DocumentSnapshot doc = q.getDocuments().get(0);
                    docId = doc.getId(); // Store docId for later updates
                    fillUI(doc);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
                );

        // Initialize Image Picker
        setupImagePicker();

        // Set click listener for adding or changing the profile photo
        if (addPhotoBtn != null) {
            addPhotoBtn.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                imagePickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));
            });
        }
    }

    /**
     * Populates the UI fields with data from the given Firestore document.
     *
     * @param doc the Firestore document containing admin profile data
     */
    private void fillUI(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "Admin profile not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = doc.getString("name");
        String email = doc.getString("email");
        String password = doc.getString("password");
        String profileImg = doc.getString("profileimg");

        nameValue.setText(name != null ? name : "");
        emailValue.setText(email != null ? email : "");
        passwordValue.setText(password != null ? password : "");

        if (profileImg != null && !profileImg.isEmpty()) {
            // Use Glide to load the profile image efficiently with caching support
            Glide.with(this).load(profileImg).into(profileImageView);
        } else {
            profileImageView.setImageResource(android.R.color.darker_gray);
        }
    }

    /**
     * Sets up the image picker launcher used to select a new profile image.
     * <p>
     * When the user selects an image, it is displayed locally and then uploaded
     * to Firebase Storage.
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
                            } else if (intent.getClipData() != null && intent.getClipData().getItemCount() > 0) {
                                ClipData clipData = intent.getClipData();
                                selectedImageUri = clipData.getItemAt(0).getUri();
                            }

                            if (selectedImageUri != null) {
                                // Display the selected image immediately
                                profileImageView.setImageURI(selectedImageUri);
                                // Upload the image to Firebase Storage
                                uploadImageToStorage(selectedImageUri);
                            }
                        }
                    }
                });
    }

    /**
     * Uploads the selected image to Firebase Storage and updates Firestore with the download URL.
     *
     * @param imageUri the URI of the selected image
     */
    private void uploadImageToStorage(Uri imageUri) {
        if (docId == null) {
            Toast.makeText(this, "Profile not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        String filename = "admin_profile_" + UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("profile_images/" + filename);

        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        updateProfileImageInFirestore(downloadUrl);
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Updates the {@code profileimg} field of the admin document in Firestore
     * with the new image download URL.
     *
     * @param downloadUrl the Firebase Storage download URL of the uploaded profile image
     */
    private void updateProfileImageInFirestore(String downloadUrl) {
        if (docId == null) return;

        firestore.collection("admins").document(docId)
                .update("profileimg", downloadUrl)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Profile image updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update database", Toast.LENGTH_SHORT).show());
    }
}
