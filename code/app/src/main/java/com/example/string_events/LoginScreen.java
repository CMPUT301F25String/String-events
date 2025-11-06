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

public class LoginScreen extends AppCompatActivity {

    enum Role { USER, ADMIN }

    private FirebaseFirestore db;
    private Role selectedRole = Role.USER;

    private MaterialButton btnUser, btnAdmin, btnSignUp;
    private TextInputEditText etEmail, etPassword;
    private FrameLayout btnSignIn;

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

    private void setRole(Role role) {
        selectedRole = role;
        int on = Color.parseColor("#FFFFCC59");
        int off = Color.parseColor("#FFE0E0E0");
        btnUser.setBackgroundTintList(ColorStateList.valueOf(role == Role.USER ? on : off));
        btnAdmin.setBackgroundTintList(ColorStateList.valueOf(role == Role.ADMIN ? on : off));
    }

    private void setLoading(boolean loading) {
        btnSignIn.setEnabled(!loading);
        btnUser.setEnabled(!loading);
        btnAdmin.setEnabled(!loading);
        btnSignUp.setEnabled(!loading);
    }

    private String lower(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.US);
    }

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
                            goHome("admin", null, null, null);
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
                                toast("No account found — create one");
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

    private void handleLoginResult(DocumentSnapshot snapshot, String enteredPassword) {
        String storedPw = snapshot.getString("password");

        if (storedPw != null && storedPw.equals(enteredPassword)) {
            String realUsername = snapshot.getString("username");
            String fullName = snapshot.getString("name");
            String userEmail = snapshot.getString("email");

            goHome("user", realUsername, fullName, userEmail);
        } else {
            setLoading(false);
            toast("Wrong password");
        }
    }

    private void goHome(String role, String username, String fullName, String email) {
        setLoading(false);
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("role", role);
        i.putExtra("user", username);
        i.putExtra("name", fullName);
        i.putExtra("email", email);
        startActivity(i);
        finish();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
