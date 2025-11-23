package com.example.string_events;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
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
 * Login screen supporting two roles (user/admin) and simple credential checks
 * against Firestore. Navigates to {@link MainActivity} on success.
 */
public class LoginScreen extends AppCompatActivity {

    /**
     * Supported roles for sign-in.
     */
    enum Role { USER, ADMIN }

    private FirebaseFirestore db;
    private Role selectedRole = Role.USER;

    private MaterialButton btnUser, btnAdmin, btnSignUp;
    private TextInputEditText etEmail, etPassword;
    private FrameLayout btnSignIn;

    /**
     * Initializes UI widgets, sets the default role, and wires up click listeners
     * for role switching, sign-up, and sign-in actions.
     *
     * @param savedInstanceState previously saved instance state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        btnUser = findViewById(R.id.btnUser);
        btnAdmin = findViewById(R.id.btnAdmin);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);

        setRole(Role.USER);

        btnUser.setOnClickListener(v -> setRole(Role.USER));
        btnAdmin.setOnClickListener(v -> setRole(Role.ADMIN));
        btnSignUp.setOnClickListener(v -> startActivity(new Intent(this, RegisterScreen.class)));
        btnSignIn.setOnClickListener(view -> signIn());
    }

    /**
     * Updates the selected role and button tints to reflect the current choice.
     *
     * @param role the role to select
     */
    private void setRole(Role role) {
        selectedRole = role;
        int on = Color.parseColor("#FFFFCC59");
        int off = Color.parseColor("#FFE0E0E0");
        btnUser.setBackgroundTintList(ColorStateList.valueOf(role == Role.USER ? on : off));
        btnAdmin.setBackgroundTintList(ColorStateList.valueOf(role == Role.ADMIN ? on : off));
    }

    /**
     * Enables or disables interactive controls during async operations.
     *
     * @param loading {@code true} to disable inputs while loading
     */
    private void setLoading(boolean loading) {
        btnSignIn.setEnabled(!loading);
        btnUser.setEnabled(!loading);
        btnAdmin.setEnabled(!loading);
        btnSignUp.setEnabled(!loading);
    }

    /**
     * Trims and lowercases a string using {@link Locale#US}.
     *
     * @param s raw input
     * @return lowercased string or empty string if {@code null}
     */
    private String lower(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.US);
    }

    /**
     * Attempts sign-in based on the selected role:
     * <ul>
     *   <li>ADMIN: checks {@code admins} collection by username.</li>
     *   <li>USER: checks {@code users} collection by email (exact then lowercase fallback) or username.</li>
     * </ul>
     * On success, navigates to {@link MainActivity}; otherwise shows an error toast.
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
                            openNextScreen("admin", null, null, null);
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
            boolean looksLikeEmail = enteredInput.contains("@");

            if (looksLikeEmail) {
                // === Try exact email first ===
                db.collection("users")
                        .whereEqualTo("email", enteredInput)
                        .limit(1)
                        .get()
                        .addOnSuccessListener(q -> {
                            if (!q.isEmpty()) {
                                handleLoginResult(q.getDocuments().get(0), pass);
                            } else {
                                // === Try lowercase fallback ===
                                db.collection("users")
                                        .whereEqualTo("email", idOrEmailLower)
                                        .limit(1)
                                        .get()
                                        .addOnSuccessListener(q2 -> {
                                            if (!q2.isEmpty()) {
                                                handleLoginResult(q2.getDocuments().get(0), pass);
                                            } else {
                                                setLoading(false);
                                                toast("no account go create one");
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
                // === Login using username ===
                db.collection("users")
                        .whereEqualTo("username", idOrEmailLower)
                        .limit(1)
                        .get()
                        .addOnSuccessListener(q -> {
                            if (q.isEmpty()) {
                                setLoading(false);
                                toast("No account found go create one");
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
     * Verifies the entered password against the stored hash (plain comparison here)
     * and, if valid, launches {@link MainActivity} with user extras.
     *
     * @param snapshot         Firestore document of the matched user
     * @param enteredPassword  password provided by the user
     */
    private void handleLoginResult(DocumentSnapshot snapshot, String enteredPassword) {
        String storedPw = snapshot.getString("password");

        if (storedPw != null && storedPw.equals(enteredPassword)) {
            String realUsername = snapshot.getString("username");
            String fullName = snapshot.getString("name");
            String userEmail = snapshot.getString("email");

            openNextScreen("user", realUsername, fullName, userEmail);
        } else {
            setLoading(false);
            toast("Wrong password");
        }
    }

    /**
     * Navigates to {@link MainActivity} and finishes this screen.
     *
     * @param role     role string to pass forward ("user" or "admin")
     * @param username username to include (nullable for admin)
     * @param fullName full name (nullable)
     * @param email    email (nullable)
     */
    private void openNextScreen(String role, String username, String fullName, String email) {
        setLoading(false);
        Intent i;
        // if logging in as a user, open the user events screen and put useful information in putExtra
        if (Objects.equals(role, "user")) {
            i = new Intent(this, MainActivity.class);
            i.putExtra("role", role);
            i.putExtra("user", username);
            i.putExtra("name", fullName);
            i.putExtra("email", email);
        }
        // if logging in as an admin, open the admin dashboard screen
        else {
            i = new Intent(this, AdminDashboardActivity.class);
        }
        startActivity(i);
        finish();
    }

    /**
     * Shows a short toast message.
     *
     * @param msg text to display
     */
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
