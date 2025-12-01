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
 * Screen that allows a new user to register for the app.
 * <p>
 * Collects basic information (name, email, password, phone), creates a
 * Firestore document in the {@code users} collection and then navigates
 * the user into {@link MainActivity} as an entrant.
 */
public class RegisterScreen extends AppCompatActivity {

    /**
     * Reference to the Firestore database used to create the user document.
     */
    private FirebaseFirestore db;

    /**
     * Input field for the user's full name.
     */
    private TextInputEditText etFullName;

    /**
     * Input field for the user's email address (also used as document id).
     */
    private TextInputEditText etEmail;

    /**
     * Input field for the user's password.
     */
    private TextInputEditText etPassword;

    /**
     * Input field for the user's phone number.
     */
    private TextInputEditText etPhone;

    /**
     * Tappable container that acts as the "Register" button.
     */
    private FrameLayout btnRegister;

    /**
     * Button that returns the user to the sign-in screen instead of registering.
     */
    private MaterialButton btnSignIn;

    /**
     * Initializes the registration screen UI and sets up click listeners.
     *
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        // Navigate back to the previous screen when the toolbar back arrow is pressed.
        toolbar.setNavigationOnClickListener(v -> finish());

        etFullName = findViewById(R.id.etFullName);
        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone    = findViewById(R.id.etPhone);
        btnRegister = findViewById(R.id.btnRegister);
        btnSignIn   = findViewById(R.id.btnSignIn);

        // Trigger the registration flow when the register button is pressed.
        btnRegister.setOnClickListener(v -> register());
        // Close this screen and return to sign-in when the sign-in button is pressed.
        btnSignIn.setOnClickListener(v -> finish());
    }

    /**
     * Enables or disables the register button while a network request is in progress.
     *
     * @param loading {@code true} to disable the button, {@code false} to enable it
     */
    private void setLoading(boolean loading) {
        btnRegister.setEnabled(!loading);
    }

    /**
     * Safely extracts trimmed text from a {@link TextInputEditText}.
     *
     * @param et the text field to read from
     * @return the trimmed text, or an empty string if the field is null or empty
     */
    private String safe(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    /**
     * Converts a string to a lower-case value using a fixed locale.
     *
     * @param s input string (may be {@code null})
     * @return lower-cased and trimmed string, or an empty string if {@code null}
     */
    private String lower(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.US);
    }

    /**
     * Validates the registration form and creates a new user document in Firestore.
     * <p>
     * This method:
     * <ol>
     *     <li>Reads and validates all input fields.</li>
     *     <li>Derives a simple username from the email before the {@code @} symbol.</li>
     *     <li>Builds a user document with role {@code "user"} and an empty profile image.</li>
     *     <li>Saves the document under {@code users/{email}} in Firestore.</li>
     *     <li>On success, navigates to {@link MainActivity} and finishes this screen.</li>
     * </ol>
     */
    private void register() {
        String name  = safe(etFullName);
        String email = lower(safe(etEmail));
        String pass  = safe(etPassword);
        String phone = safe(etPhone);

        // Basic empty-field validation to ensure all required fields are provided.
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || phone.isEmpty()) {
            toast("fill in all fields");
            return;
        }

        // Derive a simple username from the email (part before '@'); fallback to full email if needed.
        String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        // Use the normalized email as the Firestore document ID.
        final String docId = email;

        // Prepare the user document to store in Firestore.
        Map<String, Object> doc = new HashMap<>();
        doc.put("username", username);
        doc.put("password", pass);
        doc.put("email", email);
        doc.put("phone", phone);
        doc.put("name", name);
        doc.put("createdAt", FieldValue.serverTimestamp());
        doc.put("role", "user");
        // Initialize profile image with an empty string so the field is always present.
        doc.put("profileimg", "");

        setLoading(true);
        db.collection("users").document(docId).set(doc)
                .addOnSuccessListener(v -> {
                    // Account was created successfully; navigate to the main events screen.
                    toast("Account created");
                    Intent i = new Intent(RegisterScreen.this, MainActivity.class);
                    i.putExtra("role", "user");
                    i.putExtra("user", username);
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Show an error message and re-enable the register button.
                    setLoading(false);
                    toast("Failed to save profile: " + e.getLocalizedMessage());
                });
    }

    /**
     * Convenience helper to show a short {@link Toast} message.
     *
     * @param msg text to display in the toast
     */
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
