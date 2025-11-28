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

public class AdminProfileActivity extends AppCompatActivity {

    private TextView nameValue, emailValue, passwordValue;
    private ImageView profileImageView;

    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private String docId; // To store the document ID for updating
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adminprofile);

        nameValue = findViewById(R.id.nameValue);
        emailValue = findViewById(R.id.emailValue);
        passwordValue = findViewById(R.id.passwordValue);
        profileImageView = findViewById(R.id.profileImageView);
        // Assuming you added this button to your XML based on previous context
        View addPhotoBtn = findViewById(R.id.btn_add_profile_photo);

        ImageView backButton = findViewById(R.id.backButton);
        TextView logOutTextView = findViewById(R.id.logOutTextView);

        backButton.setOnClickListener(v -> {
            Intent i = new Intent(this, AdminDashboardActivity.class);
            startActivity(i);
            finish();
        });

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

        // Set click listener for adding photo
        if (addPhotoBtn != null) {
            addPhotoBtn.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                imagePickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));
            });
        }
    }

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
            // Using Glide as in your original code, which handles caching/bitmapping efficiently
            Glide.with(this).load(profileImg).into(profileImageView);
        } else {
            profileImageView.setImageResource(android.R.color.darker_gray);
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
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

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