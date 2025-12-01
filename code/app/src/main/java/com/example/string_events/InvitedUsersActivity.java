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
 * Displays a list of users with {@link UserItem.Status#INVITED} status
 * <p>
 * The invited users are loaded via {@link UserAdapterHelper#loadUsers(String)}
 * using the event ID passed from {@link OrganizerEventDetailScreen}.
 * A button at the bottom opens {@link EventMessageActivity} targeting the
 * "invited" group for the same event.
 */
public class InvitedUsersActivity extends AppCompatActivity {

    /**
     * Backing list of invited users displayed in the {@link ListView}.
     */
    private final ArrayList<UserItem> userList = new ArrayList<>();

    /**
     * Called when the activity is first created.
     * <p>
     * This method:
     * <ul>
     *     <li>Inflates the layout containing the invited users list.</li>
     *     <li>Initializes the back button to close this screen.</li>
     *     <li>Configures a {@link UserAdapter} and {@link UserAdapterHelper} for the list.</li>
     *     <li>Retrieves the event ID from the launching intent.</li>
     *     <li>Loads invited users for that event.</li>
     * </ul>
     *
     * @param savedInstanceState previously saved state, or {@code null} if created fresh
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invited_users);

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        ListView listView = findViewById(R.id.listInvited);
        UserAdapter adapter = new UserAdapter(this, userList);;
        UserAdapterHelper adapterHelper = new UserAdapterHelper(adapter, userList, UserItem.Status.INVITED);
        listView.setAdapter(adapter);

        String eventId = getIntent().getStringExtra(OrganizerEventDetailScreen.EVENT_ID);
        if (eventId == null) {
            // No event ID means we cannot load invited users; close the screen.
            finish();
            return;
        }

        // Load invited users for the given event ID
        adapterHelper.loadUsers(eventId);
    }
}
