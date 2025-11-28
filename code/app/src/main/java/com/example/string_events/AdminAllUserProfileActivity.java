package com.example.string_events;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class AdminAllUserProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String docId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_all_userprofile);

        db = FirebaseFirestore.getInstance();

        TextView name = findViewById(R.id.profile_name);
        TextView email = findViewById(R.id.profile_email);
        TextView password = findViewById(R.id.profile_password);
        ImageView profilePhoto = findViewById(R.id.profile_photo);
        Button deleteButton = findViewById(R.id.btnDeleteProfile);
        ImageButton backButton = findViewById(R.id.btnBack);


        String nameStr = getIntent().getStringExtra("name");
        String emailStr = getIntent().getStringExtra("email");
        String passwordStr = getIntent().getStringExtra("password");


        String profileImgUrl = getIntent().getStringExtra("profileimg");

        docId = getIntent().getStringExtra("docId");

        name.setText("Name: " + nameStr);
        email.setText("Email: " + emailStr);
        password.setText("Password: " + passwordStr);


        if (profileImgUrl != null && !profileImgUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL(profileImgUrl);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();

                    java.io.InputStream input = conn.getInputStream();
                    final android.graphics.Bitmap bmp =
                            android.graphics.BitmapFactory.decodeStream(input);

                    profilePhoto.post(() -> profilePhoto.setImageBitmap(bmp));

                } catch (Exception e) {
                    profilePhoto.post(() ->
                            profilePhoto.setImageResource(android.R.drawable.ic_menu_report_image));
                }
            }).start();
        } else {

            profilePhoto.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        backButton.setOnClickListener(v -> onBackPressed());

        deleteButton.setOnClickListener(v -> {
            if (docId != null && !docId.isEmpty()) {
                db.collection("users").document(docId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Profile deleted successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error deleting profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Invalid document ID", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
