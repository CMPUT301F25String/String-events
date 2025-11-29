package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Screen for organizers to view participants of an event.
 * <p>
 * Loads participating users from Firestore based on attendee list.
 */
public class ParticipatingUsersActivity extends AppCompatActivity {
    private final ArrayList<UserItem> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participating_users);

        String eventId = getIntent().getStringExtra(OrganizerEventDetailScreen.EVENT_ID);
        assert eventId != null;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventDocRef = db.collection("events").document(eventId);
        // has to be a final array of length 1 to prevent errors with access in an inner class (in onItemClick)
        final UserItem[] selectedUser = {null};

        ImageButton back = findViewById(R.id.btnBack);
        Button sendMessageButton = findViewById(R.id.btnSendParticipating);
        Button exportList = findViewById(R.id.btnExportParticipating);
        Button deleteUserButton = findViewById(R.id.btnDeleteUser);

        ListView listView = findViewById(R.id.listParticipating);
        UserAdapter adapter = new UserAdapter(this, userList);;
        UserAdapterHelper adapterHelper = new UserAdapterHelper(adapter, userList, UserItem.Status.PARTICIPATING);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        adapterHelper.loadUsers(eventId);

        back.setOnClickListener(v -> finish());

        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            // if a listview user has been clicked and the is the same user as the one already selected
            // unhighlight the user (since clicking on it again should deselect it)
            if (selectedUser[0] != null && selectedUser[0].equals(userList.get(position))) {
                listView.setItemChecked(position, false);
                selectedUser[0] = null;
                sendMessageButton.setVisibility(View.VISIBLE);
                exportList.setVisibility(View.VISIBLE);
                deleteUserButton.setVisibility(View.GONE);
            }
            else { // otherwise, highlight the clicked listview user
                listView.setItemChecked(position, true);
                selectedUser[0] = userList.get(position);
                sendMessageButton.setVisibility(View.GONE);
                exportList.setVisibility(View.GONE);
                deleteUserButton.setVisibility(View.VISIBLE);
            }
        });

        sendMessageButton.setOnClickListener(v -> {
            Intent it = new Intent(this, EventMessageActivity.class);
            it.putExtra(OrganizerEventDetailScreen.EVENT_ID, eventId);
            it.putExtra("target_group", "participating");
            startActivity(it);
        });

        exportList.setOnClickListener(v ->
                Toast.makeText(this, "Exported participating users (demo)", Toast.LENGTH_SHORT).show()
        );

        deleteUserButton.setOnClickListener(view -> {
            listView.setItemChecked(userList.indexOf(selectedUser[0]), false);
            userList.remove(selectedUser[0]);
            adapter.notifyDataSetChanged();
            eventDocRef.update("attendees", FieldValue.arrayRemove(selectedUser[0].getUsername()));
            selectedUser[0] = null;
            sendMessageButton.setVisibility(View.VISIBLE);
            exportList.setVisibility(View.VISIBLE);
            deleteUserButton.setVisibility(View.GONE);
        });
    }
}
