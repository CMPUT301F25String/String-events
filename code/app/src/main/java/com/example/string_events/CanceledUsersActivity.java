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
 * Displays a list of users with CANCELED status and provides a demo action
 * to send a message to those users.
 */
public class CanceledUsersActivity extends AppCompatActivity {
    private final ArrayList<UserItem> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canceled_users);

        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        ListView listView = findViewById(R.id.listCanceled);
        UserAdapter adapter = new UserAdapter(this, userList);;
        UserAdapterHelper adapterHelper = new UserAdapterHelper(adapter, userList, UserItem.Status.CANCELED);
        listView.setAdapter(adapter);

        String eventId = getIntent().getStringExtra(OrganizerEventDetailScreen.EVENT_ID);
        if (eventId == null) {
            finish();
            return;
        }

        adapterHelper.loadUsers(eventId);

        findViewById(R.id.btnSendCanceled).setOnClickListener(v -> {
            Intent it = new Intent(this, EventMessageActivity.class);
            it.putExtra(OrganizerEventDetailScreen.EVENT_ID, eventId);
            it.putExtra("target_group", "canceled");
            startActivity(it);
        });
    }
}
