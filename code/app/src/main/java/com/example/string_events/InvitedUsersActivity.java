package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of users with INVITED status and provides a demo action
 * to send a message to those users.
 */
public class InvitedUsersActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private final List<UserItem> userList = new ArrayList<>();
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invited_users);

        db = FirebaseFirestore.getInstance();

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        ListView listView = findViewById(R.id.listInvited);
        adapter = new UserAdapter(this, userList);
        listView.setAdapter(adapter);

        String eventId = getIntent().getStringExtra(OrganizerEventDetailScreen.EVENT_ID);
        if (eventId == null) {
            finish();
            return;
        }

        loadInvitedUsers(eventId);

        findViewById(R.id.btnSendInvited).setOnClickListener(v -> {
            Intent it = new Intent(this, EventMessageActivity.class);
            it.putExtra(OrganizerEventDetailScreen.EVENT_ID, eventId);
            it.putExtra("target_group", "invited");
            startActivity(it);
        });
    }

    private void loadInvitedUsers(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        finish();
                        return;
                    }

                    List<String> invitedList = (List<String>) doc.get("invited");
                    if (invitedList == null || invitedList.isEmpty()) {
                        userList.clear();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    fetchUsers(invitedList);
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

        String username = doc.getString("username");
        String name = doc.getString("name");
        String email = doc.getString("email");

        userList.add(new UserItem(
                username != null ? username : "",
                name != null ? name : "",
                email != null ? email : "",
                UserItem.Status.INVITED
        ));

        adapter.notifyDataSetChanged();
    }
}