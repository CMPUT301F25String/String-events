package com.example.string_events;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.string_events.UserAdapter;
import com.example.string_events.UserItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of users with CANCELED status and provides a demo action
 * to send a message to those users.
 */
public class CanceledUsersActivity extends AppCompatActivity {

    /**
     * Sets up UI components, wires the back button, binds the list view with a demo adapter,
     * and attaches a click listener to trigger a sample toast.
     *
     * @param savedInstanceState previously saved instance state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canceled_users);

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        ListView list = findViewById(R.id.listCanceled);
        list.setAdapter(new UserAdapter(this, mockUsers(UserItem.Status.CANCELED)));

        findViewById(R.id.btnSendCanceled).setOnClickListener(v ->
                Toast.makeText(this, "Message sent to canceled users (demo)", Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Builds a demo list of {@link UserItem} entries with the given status.
     *
     * @param status the status to assign to each mocked user
     * @return a list of sample users for display purposes
     */
    private List<UserItem> mockUsers(UserItem.Status status) {
        List<UserItem> data = new ArrayList<>();
        data.add(new UserItem("Alice", "alice@mail.com", status));
        data.add(new UserItem("Bob", "bob@mail.com", status));
        data.add(new UserItem("Charlie", "charlie@mail.com", status));
        return data;
    }
}
