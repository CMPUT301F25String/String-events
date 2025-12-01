package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

/**
 * Screen that lets a user edit their profile information stored in Firestore.
 * <p>
 * Looks up the user by {@code username} passed via intent extras and updates
 * name, email, password, and username (derived from the new email if provided).
 */
public class EditInformationActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etPassword;
    private FirebaseFirestore db;
    private String username;

    /**
     * Initializes the UI, retrieves the target username from the intent,
     * and wires the submit action to update Firestore with merged values
     * (new input overrides existing fields; empty input keeps old values).
     *
     * @param savedInstanceState previously saved instance state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_information_screen);

        etName = findViewById(R.id.et_new_name);
        etEmail = findViewById(R.id.et_new_email);
        etPhone = findViewById(R.id.et_new_phone);
        etPassword = findViewById(R.id.et_new_password);
        ImageButton deleteProfileButton = findViewById(R.id.delete_profile_button);
        MaterialButton btnDone = findViewById(R.id.btn_done);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        username = getIntent().getStringExtra("user");

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "No username provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        deleteProfileButton.setOnClickListener(v -> {
            if (username == null || username.isEmpty()) {
                Toast.makeText(EditInformationActivity.this, "No user to delete", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(EditInformationActivity.this, "Profile deleted", Toast.LENGTH_SHORT).show();
                                        Intent i = new Intent(EditInformationActivity.this, WelcomeActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(EditInformationActivity.this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(EditInformationActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(EditInformationActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        btnDone.setOnClickListener(v -> {
            String newName = etName.getText() != null ? etName.getText().toString().trim() : "";
            String newEmail = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String newPhone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
            String newPassword = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            db.collection("users")
                    .whereEqualTo("username", username)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (query.isEmpty()) {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DocumentSnapshot snapshot = query.getDocuments().get(0);

                        String currentName = snapshot.getString("name");
                        String currentEmail = snapshot.getString("email");
                        String currentPhone = snapshot.getString("phone");
                        String currentPassword = snapshot.getString("password");

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("name", TextUtils.isEmpty(newName) ? currentName : newName);
                        updates.put("email", TextUtils.isEmpty(newEmail) ? currentEmail : newEmail);
                        updates.put("phone", TextUtils.isEmpty(newPhone) ? currentPhone : newPhone);
                        updates.put("password", TextUtils.isEmpty(newPassword) ? currentPassword : newPassword);

                        db.collection("users").document(snapshot.getId())
                                .update(updates)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Information updated successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(EditInformationActivity.this, ProfileScreen.class));
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}
