package com.example.string_events;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileScreen {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void setupProfileScreen(Activity activity, String username, String fullName, String email) {
        TextView nameTextView = activity.findViewById(R.id.name_textView);
        TextView emailTextView = activity.findViewById(R.id.email_textView);
        ImageButton deleteProfileButton = activity.findViewById(R.id.delete_profile_button);

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
}
