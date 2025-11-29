package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.switchmaterial.SwitchMaterial;
/**
 * Screen for organizers to view users on the waitlist.
 * <p>
 * Populates a {@link ListView} using {@link UserAdapter} with mock data and provides
 * a demo action to send messages to waitlisted users.
 */
public class WaitlistUsersActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private UserAdapter adapter;
    private final List<UserItem> userList = new ArrayList<>();

    private String currentEventId;

    /**
     * Inflates the layout, wires the back button, sets up the list adapter
     * with mock waitlist data, and binds a demo click handler.
     *
     * @param savedInstanceState previously saved instance state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waitlist_users);

        db = FirebaseFirestore.getInstance();

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        ListView listView = findViewById(R.id.listWaitlist);
        adapter = new UserAdapter(this, userList);
        listView.setAdapter(adapter);

        String eventId = getIntent().getStringExtra(OrganizerEventDetailScreen.EVENT_ID);
        if (eventId == null) {
            finish();
            return;
        }
        loadWaitlistUsers(eventId);

        Button openMap = findViewById(R.id.btn_open_waitlist_map);

        openMap.setOnClickListener(v -> {
            Intent i = new Intent(WaitlistUsersActivity.this, WaitlistMapActivity.class);
            i.putExtra(WaitlistMapActivity.EXTRA_EVENT_ID, eventId);

            ArrayList<String> names = new ArrayList<>();
            for (UserItem u : userList) {
                names.add(u.getName());
            }
            i.putStringArrayListExtra(WaitlistMapActivity.EXTRA_WAITLIST_NAMES, names);

            startActivity(i);
        });

        currentEventId = getIntent().getStringExtra(OrganizerEventDetailScreen.EVENT_ID);
        if (currentEventId == null) {
            currentEventId = getIntent().getStringExtra("EVENT_ID");
        }

        SwitchMaterial preciseSwitch = findViewById(R.id.switch_precise);

        Intent i = new Intent(this, WaitlistMapActivity.class);
        i.putExtra(WaitlistMapActivity.EXTRA_EVENT_ID, currentEventId);         // 你已有
        i.putExtra(WaitlistMapActivity.EXTRA_REQUIRE_PRECISE, preciseSwitch.isChecked()); // 新增
        startActivity(i);

        findViewById(R.id.btnSendWaitlist).setOnClickListener(v -> {
            Intent it = new Intent(this, EventMessageActivity.class);
            it.putExtra(OrganizerEventDetailScreen.EVENT_ID, eventId);
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

        String username = doc.getString("username");
        String name = doc.getString("name");
        String email = doc.getString("email");

        userList.add(new UserItem(
                username != null ? username : "",
                name != null ? name : "",
                email != null ? email : "",
                UserItem.Status.WAITLIST
        ));

        adapter.notifyDataSetChanged();
    }
}