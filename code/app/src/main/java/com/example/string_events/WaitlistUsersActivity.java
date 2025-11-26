package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen for organizers to view users on the waitlist.
 * <p>
 * Populates a {@link ListView} using {@link UserAdapter} with mock data and provides
 * a demo action to send messages to waitlisted users.
 */
public class WaitlistUsersActivity extends AppCompatActivity {
    private List<UserItem> data;
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


        data = mockUsers(UserItem.Status.WAITLIST);
        ListView list = findViewById(R.id.listWaitlist);
        list.setAdapter(new UserAdapter(this, data));

        Button openMap = findViewById(R.id.btn_open_waitlist_map);
        final String eventId = getIntent().getStringExtra("event_id");

        openMap.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent i = new Intent(WaitlistUsersActivity.this, WaitlistMapActivity.class);
                i.putExtra(WaitlistMapActivity.EXTRA_EVENT_ID, eventId);

                ArrayList<String> names = new ArrayList<>();
                for (UserItem u : data) {
                    names.add(u.getName());
                }
                i.putStringArrayListExtra(WaitlistMapActivity.EXTRA_WAITLIST_NAMES, names);

                startActivity(i);
            }
        });

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        list.setAdapter(new UserAdapter(this, mockUsers(UserItem.Status.WAITLIST)));

        findViewById(R.id.btnSendWaitlist).setOnClickListener(v ->
                Toast.makeText(this, "Message sent to waitlist users (demo)", Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Provides sample waitlist entries for UI demonstration.
     *
     * @param status the {@link UserItem.Status} to apply to all mocked users
     * @return a list of mocked {@link UserItem}
     */
    private List<UserItem> mockUsers(UserItem.Status status) {
        List<UserItem> data = new ArrayList<>();
        data.add(new UserItem("Grace", "grace@mail.com", status));
        data.add(new UserItem("Henry", "henry@mail.com", status));
        data.add(new UserItem("Ivy", "ivy@mail.com", status));
        return data;
    }

}
