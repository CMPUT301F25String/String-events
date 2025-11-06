package com.example.string_events;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditInformationActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword;
    private FirebaseFirestore db;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_information_screen);

        etName = findViewById(R.id.et_new_name);
        etEmail = findViewById(R.id.et_new_email);
        etPassword = findViewById(R.id.et_new_password);
        MaterialButton btnDone = findViewById(R.id.btn_done);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        username = getIntent().getStringExtra("user");

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "No username provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnDone.setOnClickListener(v -> {
            String newName = etName.getText() != null ? etName.getText().toString().trim() : "";
            String newEmail = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
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
                        String currentPassword = snapshot.getString("password");
                        String currentUsername = snapshot.getString("username");

                        // compute new username automatically when email changes
                        String updatedEmail = TextUtils.isEmpty(newEmail) ? currentEmail : newEmail;
                        String updatedUsername = currentUsername;

                        if (!TextUtils.isEmpty(newEmail)) {
                            if (newEmail.contains("@")) {
                                updatedUsername = newEmail.substring(0, newEmail.indexOf("@"));
                            } else {
                                updatedUsername = newEmail;
                            }
                        }

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("name", TextUtils.isEmpty(newName) ? currentName : newName);
                        updates.put("email", updatedEmail);
                        updates.put("password", TextUtils.isEmpty(newPassword) ? currentPassword : newPassword);
                        updates.put("username", updatedUsername);

                        db.collection("users").document(snapshot.getId())
                                .update(updates)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Information updated successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}
