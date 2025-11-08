package com.example.string_events;

import android.os.Bundle;
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

        ListView list = findViewById(R.id.listWaitlist);
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
