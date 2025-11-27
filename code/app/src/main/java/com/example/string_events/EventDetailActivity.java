package com.example.string_events;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Displays details of a single event and lets the current user apply/cancel,
 * or accept/decline an invite. Event data is loaded from Firestore.
 */
public class EventDetailActivity extends AppCompatActivity {

    private String username;
    private String eventId; 

    private final AtomicBoolean userInEventWaitlist = new AtomicBoolean(false);
    private final AtomicBoolean userInEventInvited = new AtomicBoolean(false);
    private final AtomicBoolean userInEventAttendees = new AtomicBoolean(false);

    
    private final List<String> csvEntrants = new ArrayList<>();

    /**
     * Initializes UI, resolves intent extras, fetches the event document,
     * configures action buttons (apply / accept / decline), and sets up
     * membership state checks for attendees and waitlist.
     *
     * @param savedInstanceState previously saved instance state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_detail_screen);

        Intent it = getIntent();
        eventId = it.getStringExtra("event_id");
        assert eventId != null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsCollectionRef = db.collection("events");
        DocumentReference eventDocumentRef = eventsCollectionRef.document(eventId);

        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        username = sp.getString("user", null);

        ImageView back = findViewById(getId("back_button"));
        ImageButton applyButton = findViewById(R.id.apply_button);
        back.setOnClickListener(v -> finish());

        // Export CSV button ()
        Button exportCsvButton = findViewById(R.id.btn_export_csv);
        if (exportCsvButton != null) {
            exportCsvButton.setOnClickListener(v -> exportEntrantsToCsv());
        }

        // change the visual elements of the event details to match the event details of the clicked event
        eventDocumentRef.get()
                .addOnSuccessListener(this::bind)
                .addOnFailureListener(e -> {
                    Log.d("FirestoreCheck", "document with eventId does not exist");
                });

        // set the variables userInEventWaitlist and userInEventAttendees using the database
        // also change appearance of the apply button to reflect the user's status in the event
        getUserStatusFromDatabase(applyButton, eventDocumentRef);

        applyButton.setOnClickListener(view -> {
            // user has not applied for this event yet
            if (!userInEventWaitlist.get() && !userInEventAttendees.get()) {
                eventDocumentRef.update("waitlist", FieldValue.arrayUnion(username));
                Toast.makeText(EventDetailActivity.this, "Added to waitlist!", Toast.LENGTH_SHORT).show();
                userInEventWaitlist.set(true);
                applyButton.setBackgroundResource(R.drawable.cancel_apply_button);
            }
            // user has applied for the event and is not an attendee yet (not been accepted yet)
            else if (userInEventWaitlist.get() && !userInEventAttendees.get()) {
                eventDocumentRef.update("waitlist", FieldValue.arrayRemove(username));
                Toast.makeText(EventDetailActivity.this, "Removed from waitlist!", Toast.LENGTH_SHORT).show();
                userInEventWaitlist.set(false);
                applyButton.setBackgroundResource(R.drawable.apply_button);
            }
            // user is already an attendee and wants to cancel their appearance
            else {
                eventDocumentRef.update("attendees", FieldValue.arrayRemove(username));
                Toast.makeText(EventDetailActivity.this, "Removed from attendees!", Toast.LENGTH_SHORT).show();
                userInEventAttendees.set(false);
                applyButton.setBackgroundResource(R.drawable.apply_button);
            }
        });
    }

    /**
     * Populates UI fields with document values and formats date/time and waitlist
     * counters for display.
     *
     * @param s Firestore document snapshot of the event
     */
    private void bind(DocumentSnapshot s) {
        String title = s.getString("title");
        String description = s.getString("description");
        String location = s.getString("location");
        String address = s.getString("address");
        Timestamp startAt = s.getTimestamp("startAt");
        Timestamp endAt = s.getTimestamp("endAt");
        int max = asInt(s.get("maxAttendees"));
        int taken = asInt(s.get("attendeesCount"));
        int waitLimit = asInt(s.get("waitlistLimit"));

        @SuppressWarnings("unchecked")
        List<String> waitlist = (List<String>) s.get("waitlist");
        int currentWaitCount = (waitlist != null) ? waitlist.size() : 0;

        // 获取 attendees 列表，用于导出 CSV
        @SuppressWarnings("unchecked")
        List<String> attendeesList = (List<String>) s.get("attendees");
        csvEntrants.clear();
        if (attendeesList != null) {
            csvEntrants.addAll(attendeesList);
        }

        DateFormat dFmt = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        DateFormat tFmt = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        String dateLine = startAt == null ? "" : dFmt.format(startAt.toDate());
        String timeLine = (startAt == null ? "" : tFmt.format(startAt.toDate())) +
                " - " +
                (endAt == null ? "" : tFmt.format(endAt.toDate()));

        setText(getId("tvEventTitle"), title);
        setText(getId("tvAddress"), address);
        setText(getId("tvDateLine"), dateLine);
        setText(getId("tvTimeLine"), timeLine);
        setText(getId("tvAddress"), location);
        setText(getId("tvDescription"), description);

        setText(getId("spots_taken"), "(" + taken + "/" + max + ") Spots Taken");

        if (waitLimit > 0)
            setText(getId("waiting_list"), currentWaitCount + "/" + waitLimit + " on Waitlist");
        else
            setText(getId("waiting_list"), currentWaitCount + " on Waitlist");
    }

    /**
     * Safely sets text on a {@link TextView} identified by ID.
     *
     * @param id    resource id of the target view
     * @param value text to display; empty string used if {@code null}
     */
    private void setText(int id, String value) {
        if (id == 0) return;
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(value == null ? "" : value);
    }

    /**
     * Resolves a view ID by name using {@link android.content.res.Resources#getIdentifier}.
     *
     * @param name id name in the "id" resource type
     * @return resolved identifier or {@code 0} if not found
     */
    private int getId(String name) {
        return getResources().getIdentifier(name, "id", getPackageName());
    }

    /**
     * Converts an arbitrary object to an {@code int} with safe fallbacks.
     *
     * @param o object to convert (may be {@link Number} or parsable string)
     * @return parsed integer value or {@code 0} on failure/null
     */
    private int asInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    private void getUserStatusFromDatabase(ImageButton applyButton, DocumentReference eventDocumentRef) {
        // setting the value of userInEventWaitlist (checking if user is in the event's waitlist)
        // and the value of userInEventAttendees (checking if user is in the event's attendees)
        eventDocumentRef.get().addOnSuccessListener(documentSnapshot -> {
            ArrayList<String> waitlist = (ArrayList<String>) documentSnapshot.get("waitlist");
            ArrayList<String> invitedList = (ArrayList<String>) documentSnapshot.get("invited");
            ArrayList<String> attendeesList = (ArrayList<String>) documentSnapshot.get("attendees");

            if (waitlist != null && waitlist.contains(username)) {
                Log.d("FirestoreCheck", "already in waitlist");
                userInEventWaitlist.set(true);
                applyButton.setBackgroundResource(R.drawable.cancel_apply_button);
            } else if (invitedList != null && invitedList.contains(username)) {
                // user has been invited to the event and needs to either confirm or decline attendance
                Log.d("FirestoreCheck", "already in attendees");
                userInEventInvited.set(true);
                eventPendingUserConfirmation(applyButton, eventDocumentRef);
            } else if (attendeesList != null && attendeesList.contains(username)) {
                Log.d("FirestoreCheck", "already in attendees");
                userInEventAttendees.set(true);
                // TODO make this a leave event button instead of cancel apply
                applyButton.setBackgroundResource(R.drawable.cancel_apply_button);
            } else {
                // user is not on any of the lists
                applyButton.setBackgroundResource(R.drawable.apply_button);
            }
        }).addOnFailureListener(e -> Log.e("FirestoreCheck", "Error fetching document", e));
    }

    private void eventPendingUserConfirmation(ImageButton applyButton, DocumentReference eventDocumentRef) {
        // this instance of event details was opened from the notification screen
        ImageButton acceptInviteButton = findViewById(R.id.accept_invite_button);
        ImageButton declineInviteButton = findViewById(R.id.decline_invite_button);

        applyButton.setVisibility(View.GONE);
        acceptInviteButton.setVisibility(View.VISIBLE);
        declineInviteButton.setVisibility(View.VISIBLE);

        acceptInviteButton.setOnClickListener(view -> {
            eventDocumentRef.update("attendees", FieldValue.arrayUnion(username));
            eventDocumentRef.update("invited", FieldValue.arrayRemove(username));
            Toast.makeText(EventDetailActivity.this, "You've confirmed your attendance!", Toast.LENGTH_SHORT).show();
            userInEventAttendees.set(true);
            applyButton.setBackgroundResource(R.drawable.cancel_apply_button);
            applyButton.setVisibility(View.VISIBLE);
            acceptInviteButton.setVisibility(View.GONE);
            declineInviteButton.setVisibility(View.GONE);
        });

        declineInviteButton.setOnClickListener(view -> {
            eventDocumentRef.update("invited", FieldValue.arrayRemove(username));
            Toast.makeText(EventDetailActivity.this, "You've declined your attendance!", Toast.LENGTH_SHORT).show();
            applyButton.setBackgroundResource(R.drawable.apply_button);
            applyButton.setVisibility(View.VISIBLE);
            acceptInviteButton.setVisibility(View.GONE);
            declineInviteButton.setVisibility(View.GONE);
        });
    }

    /**
     * Exports current attendees (csvEntrants) to a CSV file in the public Downloads folder.
     */
    private void exportEntrantsToCsv() {
        if (csvEntrants.isEmpty()) {
            Toast.makeText(this, "No entrants to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        // header
        sb.append("Username\n");
        for (String user : csvEntrants) {
            sb.append(escapeCsv(user)).append("\n");
        }

        String safeEventId = (eventId == null ? "event" : eventId);
        String fileName = "entrants_" + safeEventId + ".csv";

        exportCsvToDownloads(fileName, sb.toString());
    }

    /**
     * Simple CSV escaping for commas, quotes, and newlines.
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        String v = value;
        if (v.contains("\"")) {
            v = v.replace("\"", "\"\"");
        }
        if (v.contains(",") || v.contains("\n")) {
            v = "\"" + v + "\"";
        }
        return v;
    }

    private void exportCsvToDownloads(String fileName, String csvContent) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        ContentResolver resolver = getContentResolver();
        Uri downloadUri = MediaStore.Files.getContentUri("external");
        Uri uri = resolver.insert(downloadUri, values);

        try {
            if (uri != null) {
                OutputStream outputStream = resolver.openOutputStream(uri);
                if (outputStream != null) {
                    outputStream.write(csvContent.getBytes());
                    outputStream.close();
                }
                Toast.makeText(this, "CSV saved to Downloads ✅", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to save CSV.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
        }
    }


}
