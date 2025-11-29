package com.example.string_events;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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

        exportList.setOnClickListener(v -> exportParticipatingUsersToCsv(eventId));

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

    private void exportParticipatingUsersToCsv(String eventId) {
        if (userList.isEmpty()) {
            Toast.makeText(this, "No users to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();

        // Add Header Row for better formatting
        sb.append("Name,Username,Email\n");

        for (UserItem user : userList) {
            // escapeCsv handles nulls and wraps items in quotes if they contain commas
            sb.append(escapeCsv(user.getName())).append(",");
            sb.append(escapeCsv(user.getUsername())).append(",");
            sb.append(escapeCsv(user.getEmail())).append("\n");
        }

        String fileName = "participating_users_" + eventId + ".csv";
        exportCsvToDownloads(fileName, sb.toString());
    }

    /**
     * Helper to sanitise strings for CSV.
     * If data contains a comma (e.g. "Smith, John"), it must be wrapped in quotes.
     */
    private String escapeCsv(String data) {
        if (data == null) return "";
        String escapedData = data.replaceAll("\"", "\"\"");
        if (data.contains(",") || data.contains("\n") || data.contains("\"")) {
            return "\"" + escapedData + "\"";
        }
        return data;
    }

    private void exportCsvToDownloads(String fileName, String csvContent) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        ContentResolver resolver = getContentResolver();
        Uri downloadUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        Uri uri = resolver.insert(downloadUri, values);

        try {
            if (uri != null) {
                OutputStream outputStream = resolver.openOutputStream(uri);
                if (outputStream != null) {
                    outputStream.write(csvContent.getBytes());
                    outputStream.close();
                }
                Toast.makeText(this, "CSV saved to Downloads", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to create file.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
