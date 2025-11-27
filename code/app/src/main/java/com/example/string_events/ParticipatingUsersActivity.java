package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen for organizers to view participants of an event.
 * <p>
 * Loads participating users from Firestore based on attendee list.
 */
public class ParticipatingUsersActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListView listView;
    private UserAdapter adapter;
    private final List<UserItem> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participating_users);

        db = FirebaseFirestore.getInstance();

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        listView = findViewById(R.id.listParticipating);
        adapter = new UserAdapter(this, userList);
        listView.setAdapter(adapter);

        String eventId = getIntent().getStringExtra(OrganizerEventDetailScreen.EVENT_ID);
        if (eventId == null) {
            finish();
            return;
        }

        loadParticipatingUsers(eventId);


        findViewById(R.id.btnSendParticipating).setOnClickListener(v -> {
            Intent it = new Intent(this, EventMessageActivity.class);
            it.putExtra(OrganizerEventDetailScreen.EVENT_ID, eventId);
            it.putExtra("target_group", "participating");
            startActivity(it);
        });

        findViewById(R.id.btnExportParticipating).setOnClickListener(v ->
                Toast.makeText(this, "Exported participating users (demo)", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadParticipatingUsers(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        finish();
                        return;
                    }

                    List<String> attendingList = (List<String>) doc.get("attendees");
                    if (attendingList == null || attendingList.isEmpty()) {
                        userList.clear();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    fetchUsers(attendingList);
                })
                .addOnFailureListener(e -> finish());
    }

    private void fetchUsers(List<String> usernames) {
        userList.clear();

        for (String username : usernames) {
            db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            addUserToList(query.getDocuments().get(0));
                        }
                    });
        }
    }

    private void addUserToList(DocumentSnapshot doc) {
        if (!doc.exists()) return;

        String name = doc.getString("name");
        String email = doc.getString("email");

        userList.add(new UserItem(
                name != null ? name : "",
                email != null ? email : "",
                UserItem.Status.PARTICIPATING
        ));

        adapter.notifyDataSetChanged();
    }
}
