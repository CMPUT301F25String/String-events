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
 * Screen for organizers to view users on the waitlist.
 * <p>
 * Loads waitlist users from Firestore based on the event's waitlist array.
 */
public class WaitlistUsersActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListView listView;
    private UserAdapter adapter;
    private final List<UserItem> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waitlist_users);

        db = FirebaseFirestore.getInstance();

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        listView = findViewById(R.id.listWaitlist);
        adapter = new UserAdapter(this, userList);
        listView.setAdapter(adapter);

        String eventId = getIntent().getStringExtra(OrganizerEventDetails.EVENT_ID);
        if (eventId == null) {
            finish();
            return;
        }

        loadWaitlistUsers(eventId);


        findViewById(R.id.btnSendWaitlist).setOnClickListener(v -> {
            Intent it = new Intent(this, EventMessageActivity.class);
            it.putExtra(OrganizerEventDetails.EVENT_ID, eventId);
            it.putExtra("target_group", "waitlist");
            startActivity(it);
        });
    }

    private void loadWaitlistUsers(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        finish();
                        return;
                    }

                    List<String> waitlist = (List<String>) doc.get("waitlist");
                    if (waitlist == null || waitlist.isEmpty()) {
                        userList.clear();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    fetchUsers(waitlist);
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
                UserItem.Status.WAITLIST
        ));

        adapter.notifyDataSetChanged();
    }
}
