package com.example.string_events;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.CheckBox;            // ADDED
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;
import java.util.Objects;

/**
 * Login screen that supports two roles: regular user and admin.
 * <p>
 * This activity:
 * <ul>
 *     <li>Allows switching between "User" and "Admin" login modes.</li>
 *     <li>Authenticates against the appropriate Firestore collection
 *         ({@code users} or {@code admins}).</li>
 *     <li>Supports "remember me" by writing basic user info into shared preferences.</li>
 *     <li>Navigates to {@link MainActivity} for users and
 *         {@link AdminDashboardActivity} for admins on successful login.</li>
 * </ul>
 */
public class LoginScreen extends AppCompatActivity {

    /**
     * Role type for login: user or admin.
     */
    enum Role { USER, ADMIN }

    /**
     * Firestore instance used for authentication lookups.
     */
    private FirebaseFirestore db;

    /**
     * Currently selected login role (user or admin).
     */
    private Role selectedRole = Role.USER;

    /**
     * Role selector buttons, sign-up button, and input fields.
     */
    private MaterialButton btnUser, btnAdmin, btnSignUp;
    private TextInputEditText etEmail, etPassword;

    /**
     * Sign-in button container (used as a clickable area).
     */
    private FrameLayout btnSignIn;

    /**
     * "Remember me" checkbox controlling whether login info is persisted.
     */
    private CheckBox chkRemember;

    /**
     * Called when the activity is first created.
     * <p>
     * This method:
     * <ul>
     *     <li>Initializes UI components.</li>
     *     <li>Configures the toolbar back navigation.</li>
     *     <li>Sets the default role to {@link Role#USER}.</li>
     *     <li>Attaches click listeners for role selection, sign-up, and sign-in.</li>
     * </ul>
     *
     * @param savedInstanceState previously saved state, or {@code null} if created fresh
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Intent i = new Intent(LoginScreen.this, WelcomeActivity.class);
            startActivity(i);
            finish();
        });

        btnUser = findViewById(R.id.btnUser);
        btnAdmin = findViewById(R.id.btnAdmin);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        chkRemember = findViewById(R.id.chkRemember);

        // Default to user role on first load
        setRole(Role.USER);

        // Role selection handlers
        btnUser.setOnClickListener(v -> setRole(Role.USER));
        btnAdmin.setOnClickListener(v -> setRole(Role.ADMIN));

        // Navigate to registration screen
        btnSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterScreen.class))
        );

        // Attempt login when sign-in is pressed
        btnSignIn.setOnClickListener(view -> signIn());
    }

    /**
     * Updates the current login role and refreshes the role button styles.
     *
     * @param role the role to select (user or admin)
     */
    private void setRole(Role role) {
        selectedRole = role;
        int on = Color.parseColor("#FFFFCC59");
        int off = Color.parseColor("#FFE0E0E0");
        btnUser.setBackgroundTintList(ColorStateList.valueOf(role == Role.USER ? on : off));
        btnAdmin.setBackgroundTintList(ColorStateList.valueOf(role == Role.ADMIN ? on : off));
    }

    /**
     * Enables or disables user input during an ongoing login attempt.
     *
     * @param loading {@code true} to disable buttons, {@code false} to enable them
     */
    private void setLoading(boolean loading) {
        btnSignIn.setEnabled(!loading);
        btnUser.setEnabled(!loading);
        btnAdmin.setEnabled(!loading);
        btnSignUp.setEnabled(!loading);
    }

    /**
     * Returns a lowercased, trimmed version of the given string.
     *
     * @param s the input string (may be {@code null})
     * @return a non-null, trimmed, lowercased string
     */
    private String lower(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.US);
    }

    /**
     * Handles the sign-in flow for both user and admin roles.
     * <p>
     * This method:
     * <ul>
     *     <li>Validates that both username/email and password are entered.</li>
     *     <li>Determines whether the input looks like an email.</li>
     *     <li>Queries the appropriate Firestore collection based on the selected role.</li>
     *     <li>Delegates password checking to {@link #handleLoginResult(DocumentSnapshot, String)}
     *         or performs admin password validation inline.</li>
     * </ul>
     */
    private void signIn() {
        String enteredInput = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
        String idOrEmailLower = lower(enteredInput);
        String pass = etPassword.getText() == null ? "" : etPassword.getText().toString().trim();

        if (enteredInput.isEmpty() || pass.isEmpty()) {
            toast(" enter username/email and password");
            return;
        }

        setLoading(true);

        if (selectedRole == Role.ADMIN) {
            // Admin login: match by username in "admins" collection
            db.collection("admins")
                    .whereEqualTo("username", idOrEmailLower)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(q -> {
                        if (q.isEmpty()) {
                            setLoading(false);
                            toast("not admin account");
                            return;
                        }
                        String storedPw = q.getDocuments().get(0).getString("password");
                        if (storedPw != null && storedPw.equals(pass)) {

                            DocumentSnapshot adminDoc = q.getDocuments().get(0);
                            String adminUsername = adminDoc.getString("username");
                            openAdminScreen(adminUsername);

                        } else {
                            setLoading(false);
                            toast("wrong admin password");
                        }
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        toast("Login failed: " + e.getLocalizedMessage());
                    });

        } else {
            // Regular user login: allow login by email or username
            boolean looksLikeEmail = enteredInput.contains("@");

            if (looksLikeEmail) {
                // First try exact email match
                db.collection("users")
                        .whereEqualTo("email", enteredInput)
                        .limit(1)
                        .get()
                        .addOnSuccessListener(q -> {
                            if (!q.isEmpty()) {
                                handleLoginResult(q.getDocuments().get(0), pass);
                            } else {
                                // Then try lowercased email
                                db.collection("users")
                                        .whereEqualTo("email", idOrEmailLower)
                                        .limit(1)
                                        .get()
                                        .addOnSuccessListener(q2 -> {
                                            if (!q2.isEmpty()) {
                                                handleLoginResult(q2.getDocuments().get(0), pass);
                                            } else {
                                                setLoading(false);
                                                toast("Incorrect login information.");
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            setLoading(false);
                                            toast("Login failed: " + e.getLocalizedMessage());
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            setLoading(false);
                            toast("Login failed: " + e.getLocalizedMessage());
                        });

            } else {
                // Login by username
                db.collection("users")
                        .whereEqualTo("username", idOrEmailLower)
                        .limit(1)
                        .get()
                        .addOnSuccessListener(q -> {
                            if (q.isEmpty()) {
                                setLoading(false);
                                toast("Incorrect login information.");
                                return;
                            }
                            handleLoginResult(q.getDocuments().get(0), pass);
                        })
                        .addOnFailureListener(e -> {
                            setLoading(false);
                            toast("Login failed: " + e.getLocalizedMessage());
                        });
            }
        }
    }

    /**
     * Validates the entered password against the stored password
     * and navigates forward on success.
     *
     * @param snapshot        Firestore document containing the user record
     * @param enteredPassword the password typed by the user
     */
    private void handleLoginResult(DocumentSnapshot snapshot, String enteredPassword) {
        String storedPw = snapshot.getString("password");

        if (storedPw != null && storedPw.equals(enteredPassword)) {
            String realUsername = snapshot.getString("username");
            String fullName = snapshot.getString("name");
            String userEmail = snapshot.getString("email");

            openNextScreen(realUsername, fullName, userEmail);
        } else {
            setLoading(false);
            toast("Wrong password");
        }
    }

    /**
     * Opens the main user screen after successful user login and
     * optionally persists login information if "remember me" is checked.
     *
     * @param username the user's username
     * @param fullName the user's full name
     * @param email    the user's email address
     */
    private void openNextScreen(String username, String fullName, String email) {
        setLoading(false);
        boolean rememberMe = chkRemember != null && chkRemember.isChecked();

        // Store user info for cross-activity access
        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        sp.edit()
                .putBoolean("remember", rememberMe)
                .putString("username", username)
                .putString("user", username)
                .putString("role", "entrant")
                .putString("name", fullName)
                .putString("email", email)
                .apply();

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    /**
     * Opens the admin dashboard after successful admin login and
     * optionally persists login information if "remember me" is checked.
     *
     * @param adminUsername the admin's username
     */
    private void openAdminScreen(String adminUsername) {

        setLoading(false);

        boolean rememberMe = chkRemember != null && chkRemember.isChecked();  // ADDED

        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        sp.edit()
                .putBoolean("remember", rememberMe)
                .putString("username", adminUsername)
                .putString("user", adminUsername)
                .putString("role", "admin")
                .putString("adminUsername", adminUsername)
                .apply();

        Intent i = new Intent(this, AdminDashboardActivity.class);
        i.putExtra("adminUsername", adminUsername);
        startActivity(i);
        finish();
    }

    /**
     * Convenience method for showing a short toast message.
     *
     * @param msg the message text to display
     */
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
