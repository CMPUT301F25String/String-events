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

public class AdminProfileManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminProfileAdapter adapter;
    private final ArrayList<AdminProfiles> profiles = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        // Adapter + click listener
        adapter = new AdminProfileAdapter(profiles, profile -> {
            Intent intent = new Intent(this, AdminAllUserProfileActivity.class);
            intent.putExtra("name", profile.getName());
            intent.putExtra("email", profile.getEmail());
            intent.putExtra("password", profile.getPassword());
            intent.putExtra("docId", profile.getDocId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        loadProfiles();
    }

    private void loadProfiles() {
        db.collection("users")
                .whereEqualTo("role", "user")
                .get()
                .addOnSuccessListener(snapshots -> {
                    profiles.clear();

                    if (snapshots.isEmpty()) {
                        Log.d("ADMIN", "No user documents found");
                    } else {
                        Log.d("ADMIN", "Fetched " + snapshots.size() + " user profiles");
                    }

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        String password = doc.getString("password");

                        Log.d("ADMIN", "User -> " + name + " | " + email + " | " + password);
                        profiles.add(new AdminProfiles(name, email, password, doc.getId()));
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("ADMIN", "Error loading users", e));
    }
}
