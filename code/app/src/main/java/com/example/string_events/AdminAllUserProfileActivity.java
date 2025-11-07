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

public class AdminAllUserProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String docId;

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
