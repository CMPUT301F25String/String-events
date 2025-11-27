package com.example.string_events;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminProfileActivity extends AppCompatActivity {

    private TextView nameValue, emailValue, passwordValue;
    private ImageView profileImageView;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adminprofile);

        nameValue = findViewById(R.id.nameValue);
        emailValue = findViewById(R.id.emailValue);
        passwordValue = findViewById(R.id.passwordValue);
        profileImageView = findViewById(R.id.profileImageView);

        ImageView backButton = findViewById(R.id.backButton);
        TextView logOutTextView = findViewById(R.id.logOutTextView);

        backButton.setOnClickListener(v -> {
            Intent i = new Intent(this, AdminDashboardActivity.class);
            startActivity(i);
            finish();
        });

        logOutTextView.setOnClickListener(v -> {
            SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
            sp.edit().clear().apply();
            Intent i = new Intent(this, WelcomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        firestore = FirebaseFirestore.getInstance();

        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        String adminUsername = getIntent().getStringExtra("adminUsername");
        if (adminUsername == null || adminUsername.isEmpty()) {
            adminUsername = sp.getString("adminUsername", null);
        }

        if (adminUsername == null || adminUsername.isEmpty()) {
            Toast.makeText(this, "Missing admin username", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalAdminUsername = adminUsername;

        firestore.collection("admins")
                .whereEqualTo("username", finalAdminUsername)
                .limit(1)
                .get()
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) {
                        Toast.makeText(this, "Admin profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    fillUI(q.getDocuments().get(0));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
                );
    }

    private void fillUI(DocumentSnapshot doc) {

        if (!doc.exists()) {
            Toast.makeText(this, "Admin profile not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = doc.getString("name");
        String email = doc.getString("email");
        String password = doc.getString("password");
        String profileImg = doc.getString("profileimg");

        nameValue.setText(name != null ? name : "");
        emailValue.setText(email != null ? email : "");
        passwordValue.setText(password != null ? password : "");

        if (profileImg != null && !profileImg.isEmpty()) {
            Glide.with(this).load(profileImg).into(profileImageView);
        } else {
            profileImageView.setImageResource(android.R.color.darker_gray);
        }
    }
}
