package com.example.string_events;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity that displays a user's profile details for an admin user.
 * <p>
 * The admin can:
 * <ul>
 *     <li>View the user's name, email, and password (as plain text).</li>
 *     <li>View the user's profile image.</li>
 *     <li>Delete the user's profile document from Firestore.</li>
 * </ul>
 * The user data is passed to this activity via Intent extras.
 */
public class AdminAllUserProfileActivity extends AppCompatActivity {

    /**
     * Firestore database instance used to access and modify user documents.
     */
    private FirebaseFirestore db;

    /**
     * Firestore document ID for the user whose profile is being displayed.
     */
    private String docId;

    /**
     * Called when the activity is first created.
     * <p>
     * This method:
     * <ul>
     *     <li>Initializes the Firestore instance.</li>
     *     <li>Binds UI views to their layout IDs.</li>
     *     <li>Retrieves user data from the Intent extras.</li>
     *     <li>Displays the user's profile information.</li>
     *     <li>Loads the profile image (if available) from a remote URL.</li>
     *     <li>Sets up click listeners for the Back and Delete buttons.</li>
     * </ul>
     *
     * @param savedInstanceState the previously saved state of this activity, or {@code null}
     *                           if the activity is being created for the first time
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_all_userprofile);

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();

        // Bind views
        TextView name = findViewById(R.id.profile_name);
        TextView email = findViewById(R.id.profile_email);
        TextView password = findViewById(R.id.profile_password);
        ImageView profilePhoto = findViewById(R.id.profile_photo);
        Button deleteButton = findViewById(R.id.btnDeleteProfile);
        ImageButton backButton = findViewById(R.id.btnBack);

        // Retrieve user details from the Intent extras
        String nameStr = getIntent().getStringExtra("name");
        String emailStr = getIntent().getStringExtra("email");
        String passwordStr = getIntent().getStringExtra("password");
        String profileImgUrl = getIntent().getStringExtra("profileimg");
        docId = getIntent().getStringExtra("docId");

        // Display user information in the corresponding TextViews
        name.setText("Name: " + nameStr);
        email.setText("Email: " + emailStr);
        password.setText("Password: " + passwordStr);

        // Load and display the profile image if a URL is provided,
        // otherwise use a default placeholder image.
        if (profileImgUrl != null && !profileImgUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL(profileImgUrl);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();

                    java.io.InputStream input = conn.getInputStream();
                    final android.graphics.Bitmap bmp =
                            android.graphics.BitmapFactory.decodeStream(input);

                    // Update the ImageView on the UI thread
                    profilePhoto.post(() -> profilePhoto.setImageBitmap(bmp));

                } catch (Exception e) {
                    // If loading fails, show a default image placeholder
                    profilePhoto.post(() ->
                            profilePhoto.setImageResource(android.R.drawable.ic_menu_report_image));
                }
            }).start();
        } else {
            // No image URL available, set default placeholder image
            profilePhoto.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        // Navigate back to the previous screen when the back button is pressed
        backButton.setOnClickListener(v -> onBackPressed());

        // Delete the user profile from Firestore when the delete button is pressed
        deleteButton.setOnClickListener(v -> {
            if (docId != null && !docId.isEmpty()) {
                db.collection("users").document(docId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Profile deleted successfully", Toast.LENGTH_SHORT).show();
                            // Close this activity after successful deletion
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error deleting profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                // Document ID is missing or invalid
                Toast.makeText(this, "Invalid document ID", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
