package com.example.string_events;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class ProfileScreen extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
        // screen buttons
        TextView editProfileTextView = findViewById(R.id.edit_textView);
        TextView logOutTextView = findViewById(R.id.logOut_textView);
        SwitchCompat notificationSwitch = findViewById(R.id.notification_switch);
        ImageView profileImageView = findViewById(R.id.profile_imageView);
        ImageButton switchRolesButton = findViewById(R.id.switch_roles_button);
        TextView nameTextView = findViewById(R.id.name_textView);
        TextView emailTextView = findViewById(R.id.email_textView);
        ImageView infoImageView = findViewById(R.id.info_imageButton);
        ImageButton deleteProfileImageButton = findViewById(R.id.delete_profile_button);

    public void setupProfileScreen(Activity activity, String username, String fullName, String email) {
        TextView nameTextView = activity.findViewById(R.id.name_textView);
        TextView emailTextView = activity.findViewById(R.id.email_textView);
        ImageButton deleteProfileButton = activity.findViewById(R.id.delete_profile_button);

        switchRolesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
                // get the current role of the user and switch it to either entrant or organizer
                String currentRole = sharedPreferences.getString("role", null);
                assert currentRole != null;

                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (currentRole.equals("entrant")) {
                    editor.putString("role", "organizer");
                    editor.apply();
                    openOrganizerEventScreen();
                }
                else {
                    editor.putString("role", "organizer");
                    editor.apply();
                    openEntrantEventScreen();
                }
            }
        });
        // Update text fields
        if (fullName != null) nameTextView.setText("Name: " + fullName);
        if (email != null) emailTextView.setText("Email: " + email);

        // Delete button logic
        deleteProfileButton.setOnClickListener(v -> {
            if (username == null || username.isEmpty()) {
                Toast.makeText(activity, "No user to delete", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users")
                    .whereEqualTo("username", username)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            String docId = query.getDocuments().get(0).getId();
                            db.collection("users").document(docId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(activity, "Profile deleted", Toast.LENGTH_SHORT).show();
                                        Intent i = new Intent(activity, WelcomeActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        activity.startActivity(i);
                                        activity.finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(activity, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(activity, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(activity, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    public void openOrganizerEventScreen() {
        Context context = ProfileScreen.this;
        Intent myIntent = new Intent(context, OrganizerEventScreen.class);
        context.startActivity(myIntent);
    }

    public void openEntrantEventScreen() {
        Context context = ProfileScreen.this;
        Intent myIntent = new Intent(context, MainActivity.class);
        context.startActivity(myIntent);
    }
}
