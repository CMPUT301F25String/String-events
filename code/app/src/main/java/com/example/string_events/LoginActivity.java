package com.example.string_events;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    enum Role { USER, ADMIN }

    private FirebaseFirestore db;

    private Role selectedRole = Role.USER;

    private MaterialButton btnUser, btnAdmin, btnSignUp;
    private TextInputEditText etEmail, etPassword; // email Address is username for Admin
    private FrameLayout btnSignIn;

    @Override protected void onCreate(Bundle savedInstanceState) {
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
        btnSignUp.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
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

    private String lower(String s) { return s == null ? "" : s.trim().toLowerCase(Locale.US); }

    private void signIn() {
        String idOrEmail = lower(etEmail.getText() == null ? "" : etEmail.getText().toString());
        String pass      = etPassword.getText() == null ? "" : etPassword.getText().toString().trim();
        String username = idOrEmail.contains("@") ? idOrEmail.substring(0, idOrEmail.indexOf('@')) : idOrEmail;

        if (idOrEmail.isEmpty() || pass.isEmpty()) {
            toast(" enter username/email and password");
            return;
        }
        setLoading(true);

        if (selectedRole == Role.ADMIN) {
            db.collection("admins").whereEqualTo("username", username).limit(1).get()
                    .addOnSuccessListener(q -> {
                        if (q.isEmpty()) { setLoading(false); toast("not admin account"); return; }
                        String storedPw = q.getDocuments().get(0).getString("password");
                        if (storedPw != null && storedPw.equals(pass)) {
                            goHome("admin", null);
                        } else {
                            setLoading(false); toast("wrong admin password");
                        }
                    })
                    .addOnFailureListener(e -> { setLoading(false); toast("Login failed: " + e.getLocalizedMessage()); });

        } else {
            boolean looksLikeEmail = idOrEmail.contains("@");

            db.collection("users")
                    .whereEqualTo(looksLikeEmail ? "email" : "username", idOrEmail)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(q -> {
                        if (q.isEmpty()) { setLoading(false); toast("no account go create one"); return; }
                        String storedPw = q.getDocuments().get(0).getString("password"); // plain text
                        if (storedPw != null && storedPw.equals(pass)) {
                            // after a successful sign in, send the person's role and their username/email to the next screen as arguments
                            goHome("user", username);
                        } else {
                            setLoading(false); toast("Wrong password");
                        }
                    })
                    .addOnFailureListener(e -> { setLoading(false); toast("Login failed: " + e.getLocalizedMessage()); });
        }
    }

    private void goHome(String role, String username) {
        setLoading(false);
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("role", role);
        i.putExtra("user", username);
        startActivity(i);
        finish();
    }

    private void toast(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }
}
