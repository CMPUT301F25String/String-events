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
 * Screen for organizers to view participants of an event.
 * <p>
 * Populates a {@link ListView} using {@link UserAdapter} and provides demo actions
 * to send messages or export the list.
 */
public class ParticipatingUsersActivity extends AppCompatActivity {

    /**
     * Inflates the layout, wires the back button, sets up the list adapter
     * with mock data, and binds demo click handlers.
     *
     * @param savedInstanceState previously saved instance state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participating_users);

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        ListView list = findViewById(R.id.listParticipating);
        list.setAdapter(new UserAdapter(this, mockUsers(UserItem.Status.PARTICIPATING)));

        findViewById(R.id.btnSendParticipating).setOnClickListener(v ->
                Toast.makeText(this, "Message sent to participating users (demo)", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.btnExportParticipating).setOnClickListener(v ->
                Toast.makeText(this, "Exported participating users (demo)", Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Provides sample participant entries for UI demonstration.
     *
     * @param status the {@link UserItem.Status} to apply to all mocked users
     * @return a list of mocked {@link UserItem}
     */
    private List<UserItem> mockUsers(UserItem.Status status) {
        List<UserItem> data = new ArrayList<>();
        data.add(new UserItem("Daisy", "daisy@mail.com", status));
        data.add(new UserItem("Edward", "edward@mail.com", status));
        data.add(new UserItem("Fiona", "fiona@mail.com", status));
        return data;
    }
}
