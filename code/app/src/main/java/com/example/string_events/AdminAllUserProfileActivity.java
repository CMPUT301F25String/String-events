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
 * Admin screen for viewing a single user's profile and deleting that profile from Firestore.
 * <p>
 * Expects the launching {@link android.content.Intent} to include the following extras:
 * <ul>
 *   <li><b>"name"</b> — user's display name</li>
 *   <li><b>"email"</b> — user's email address</li>
 *   <li><b>"password"</b> — user's password (as currently stored)</li>
 *   <li><b>"role"</b> — user's role label (optional, currently unused)</li>
 *   <li><b>"docId"</b> — Firestore document ID under the {@code users} collection</li>
 * </ul>
 * The delete action removes the document {@code users/{docId}} and finishes the Activity on success.
 *
 * @author group
 * @since 1.0
 */
public class AdminAllUserProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String docId;

    /**
     * Inflates the UI, binds views, reads Intent extras, and wires back/delete actions.
     *
     * @param savedInstanceState optional saved state provided by Android; may be {@code null}
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_all_userprofile);

        db = FirebaseFirestore.getInstance();

        TextView name = findViewById(R.id.profile_name);
        TextView email = findViewById(R.id.profile_email);
        TextView password = findViewById(R.id.profile_password);
        ImageView profilePhoto = findViewById(R.id.profile_photo);
        Button deleteButton = findViewById(R.id.btnDeleteProfile);
        ImageButton backButton = findViewById(R.id.btnBack);

        // Get intent extras
        String nameStr = getIntent().getStringExtra("name");
        String emailStr = getIntent().getStringExtra("email");
        String passwordStr = getIntent().getStringExtra("password");
        String roleStr = getIntent().getStringExtra("role");
        docId = getIntent().getStringExtra("docId");

        name.setText("Name: " + nameStr);
        email.setText("Email: " + emailStr);
        password.setText("Password: " + passwordStr);

        backButton.setOnClickListener(v -> onBackPressed());

        deleteButton.setOnClickListener(v -> {
            if (docId != null && !docId.isEmpty()) {
                db.collection("users").document(docId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Profile deleted successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error deleting profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            } else {
                Toast.makeText(this, "Invalid document ID", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
