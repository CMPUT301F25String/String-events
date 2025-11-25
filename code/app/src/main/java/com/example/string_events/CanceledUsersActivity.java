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
 * Displays a list of users with CANCELED status and provides a demo action
 * to send a message to those users.
 */
public class CanceledUsersActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListView listView;
    private final List<UserItem> userList = new ArrayList<>();
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canceled_users);

        db = FirebaseFirestore.getInstance();

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        listView = findViewById(R.id.listCanceled);
        adapter = new UserAdapter(this, userList);
        listView.setAdapter(adapter);

        String eventId = getIntent().getStringExtra(OrganizerEventDetails.EVENT_ID);
        if (eventId == null) {
            finish();
            return;
        }

        loadCanceledUsers(eventId);

        findViewById(R.id.btnSendCanceled).setOnClickListener(v -> {
            Intent it = new Intent(this, EventMessageActivity.class);
            it.putExtra(OrganizerEventDetails.EVENT_ID, eventId);
            it.putExtra("target_group", "canceled");
            startActivity(it);
        });
    }

    private void loadCanceledUsers(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        finish();
                        return;
                    }

                    List<String> canceledList = (List<String>) doc.get("canceledusers");
                    if (canceledList == null || canceledList.isEmpty()) {
                        userList.clear();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    fetchUsers(canceledList);
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
                UserItem.Status.CANCELED
        ));

        adapter.notifyDataSetChanged();
    }
}
