package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private TextInputEditText etFullName, etEmail, etPassword, etPhone;
    private FrameLayout btnRegister;
    private MaterialButton btnSignIn;

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

    private void setLoading(boolean loading) {
        btnRegister.setEnabled(!loading);
    }

    private String safe(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private String lower(String s) { return s == null ? "" : s.trim().toLowerCase(Locale.US); }

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
                    Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                    i.putExtra("role", "user");
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    toast("Failed to save profile: " + e.getLocalizedMessage());
                });
    }


    private void toast(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }
}
