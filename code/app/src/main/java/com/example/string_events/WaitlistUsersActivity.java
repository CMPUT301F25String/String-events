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
    private final ArrayList<UserItem> userList = new ArrayList<>();
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

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        ListView listView = findViewById(R.id.listWaitlist);
        UserAdapter adapter = new UserAdapter(this, userList);;
        UserAdapterHelper adapterHelper = new UserAdapterHelper(adapter, userList, UserItem.Status.WAITLIST);
        listView.setAdapter(adapter);

        String eventId = getIntent().getStringExtra(OrganizerEventDetailScreen.EVENT_ID);
        if (eventId == null) {
            finish();
            return;
        }
        adapterHelper.loadUsers(eventId);

        Button openMap = findViewById(R.id.btn_open_waitlist_map);

        openMap.setOnClickListener(v -> {
            Intent i = new Intent(WaitlistUsersActivity.this, WaitlistMapActivity.class);
            i.putExtra("eventId", eventId);
            startActivity(i);
        });

        findViewById(R.id.btnSendWaitlist).setOnClickListener(v -> {
            Intent it = new Intent(this, EventMessageActivity.class);
            it.putExtra(OrganizerEventDetailScreen.EVENT_ID, eventId);
            it.putExtra("target_group", "waitlist");
            startActivity(it);
        });
    }
}