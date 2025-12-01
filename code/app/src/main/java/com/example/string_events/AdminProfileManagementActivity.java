package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Activity that allows admins to view and manage all user profiles.
 * <p>
 * This screen:
 * <ul>
 *     <li>Shows a list of user profiles retrieved from Firestore.</li>
 *     <li>Lets the admin tap a profile to open a detailed profile view.</li>
 *     <li>Provides a back button to return to the admin dashboard.</li>
 * </ul>
 */
public class AdminProfileManagementActivity extends AppCompatActivity {

    /**
     * RecyclerView that displays the list of user profiles.
     */
    private RecyclerView recyclerView;

    /**
     * Adapter that binds {@link AdminProfiles} data to the RecyclerView.
     */
    private AdminProfileAdapter adapter;

    /**
     * In-memory list of user profiles loaded from Firestore.
     */
    private final ArrayList<AdminProfiles> profiles = new ArrayList<>();

    /**
     * Firestore database instance used to load user profile documents.
     */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Called when the activity is first created.
     * <p>
     * This method:
     * <ul>
     *     <li>Initializes the layout and RecyclerView.</li>
     *     <li>Configures the back button to return to the dashboard.</li>
     *     <li>Creates and sets the profile adapter.</li>
     *     <li>Triggers loading user profiles from Firestore.</li>
     * </ul>
     *
     * @param savedInstanceState previously saved state, or {@code null} if created fresh
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_profile_management_screen);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ImageButton backButton = findViewById(R.id.btnBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminDashboardActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // Adapter that opens the full user profile screen when an item is clicked
        adapter = new AdminProfileAdapter(profiles, profile -> {
            Intent intent = new Intent(this, AdminAllUserProfileActivity.class);
            intent.putExtra("name", profile.getName());
            intent.putExtra("email", profile.getEmail());
            intent.putExtra("password", profile.getPassword());
            intent.putExtra("docId", profile.getDocId());
            intent.putExtra("profileimg", profile.getProfileImg());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        loadProfiles();
    }

    /**
     * Loads user profiles from the Firestore {@code users} collection.
     * <p>
     * Only documents with {@code role = "user"} are included.
     * For each document, an {@link AdminProfiles} object is constructed and added to the list.
     * After loading, the adapter is notified to refresh the UI.
     * Any failure is logged using {@link Log#e(String, String, Throwable)}.
     */
    private void loadProfiles() {
        db.collection("users")
                .whereEqualTo("role", "user")
                .get()
                .addOnSuccessListener(snapshots -> {
                    profiles.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {

                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        String password = doc.getString("password");
                        // Firestore field storing the profile image URL
                        String profileImg = doc.getString("profileimg");

                        profiles.add(new AdminProfiles(
                                name,
                                email,
                                password,
                                doc.getId(),
                                profileImg
                        ));
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("ADMIN", "Error loading users", e));
    }
}
