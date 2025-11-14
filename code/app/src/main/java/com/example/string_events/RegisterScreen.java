package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Registration screen for creating a new user account.
 * <p>
 * Collects basic profile fields, validates them, and persists a user document
 * to Firestore under the {@code users} collection. On success, navigates to
 * {@link MainActivity}.
 */
public class RegisterScreen extends AppCompatActivity {
    private FirebaseFirestore db;
    private TextInputEditText etFullName, etEmail, etPassword, etPhone;
    private FrameLayout btnRegister;
    private MaterialButton btnSignIn;

    /**
     * Inflates the registration UI, wires buttons, and initializes Firestore.
     *
     * @param savedInstanceState previously saved state, or {@code null}
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });

        etFullName = findViewById(R.id.etFullName);
        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone    = findViewById(R.id.etPhone);
        btnRegister = findViewById(R.id.btnRegister);
        btnSignIn   = findViewById(R.id.btnSignIn);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { register(); }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
    }

    /**
     * Enables/disables UI to reflect a loading state.
     *
     * @param loading {@code true} to disable inputs while saving
     */
    private void setLoading(boolean loading) {
        btnRegister.setEnabled(!loading);
    }

    /**
     * Safely extracts trimmed text from a {@link TextInputEditText}.
     *
     * @param et input field
     * @return trimmed text or an empty string if null
     */
    private String safe(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    /**
     * Lowercases and trims a string using {@link Locale#US}.
     *
     * @param s input string
     * @return lowercased string or empty string if null
     */
    private String lower(String s) { return s == null ? "" : s.trim().toLowerCase(Locale.US); }

    /**
     * Validates inputs, constructs a user document, writes it to Firestore,
     * and routes to {@link MainActivity} on success.
     * Shows a toast on validation or network errors.
     */
    private void register() {
        String name  = safe(etFullName);
        String email = lower(safe(etEmail));
        String pass  = safe(etPassword);
        String phone = safe(etPhone);

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || phone.isEmpty()) {
            toast("fill in all fields");
            return;
        }

        String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        final String docId = email;

        Map<String, Object> doc = new HashMap<>();
        doc.put("username", username);
        doc.put("password", pass);
        doc.put("email", email);
        doc.put("phone", phone);
        doc.put("name", name);
        doc.put("createdAt", FieldValue.serverTimestamp());
        doc.put("role", "user");

        setLoading(true);
        db.collection("users").document(docId).set(doc)
                .addOnSuccessListener(v -> {
                    toast("Account created");
                    Intent i = new Intent(RegisterScreen.this, MainActivity.class);
                    i.putExtra("role", "user");
                    i.putExtra("user", username);
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    toast("Failed to save profile: " + e.getLocalizedMessage());
                });
    }

    /**
     * Shows a short {@link Toast} message.
     *
     * @param msg message text
     */
    private void toast(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }
}
