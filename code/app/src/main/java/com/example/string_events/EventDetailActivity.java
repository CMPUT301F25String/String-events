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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Displays details of a single event and lets the current user apply/cancel,
 * or accept/decline an invite. Event data is loaded from Firestore.
 */
public class EventDetailActivity extends AppCompatActivity {
    private final LotteryHelper lotteryHelper = new LotteryHelper();
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
        setContentView(R.layout.event_detail_screen); // Updated to match your latest XML file name

        Intent it = getIntent();
        eventId = it.getStringExtra("event_id");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventDocumentRef = db.collection("events").document(eventId);

        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        username = sp.getString("user", null);

        ImageView back = findViewById(getId("back_button"));
        ImageButton applyButton = findViewById(R.id.apply_button);
        back.setOnClickListener(v -> finish());

        // CSV Export functionality commented out as requested
        /*
        Button exportCsvButton = findViewById(R.id.btn_export_csv);
        if (exportCsvButton != null) {
            exportCsvButton.setOnClickListener(v -> exportEntrantsToCsv());
        }
        */

        // change the visual elements of the event details to match the event details of the clicked event
        eventDocumentRef.get()
                .addOnSuccessListener(this::bind)
                .addOnFailureListener(e -> Log.d("FirestoreCheck", "document with eventId does not exist"));

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
                // when an attendee for an event cancels, the lottery automatically selects a replacement user from the waitlist
                lotteryHelper.replaceCancelledUser(eventDocumentRef);
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
        String creator = s.getString("creator");
        Timestamp startAt = s.getTimestamp("startAt");
        Timestamp endAt = s.getTimestamp("endAt");

        int max = asInt(s.get("maxAttendees"));
        int taken = asInt(s.get("attendeesCount"));
        int waitLimit = asInt(s.get("waitlistLimit"));

        List<String> waitlist = (List<String>) s.get("waitlist");
        int currentWaitCount = waitlist != null ? waitlist.size() : 0;

        List<String> attendeesList = (List<String>) s.get("attendees");
        csvEntrants.clear();
        if (attendeesList != null) csvEntrants.addAll(attendeesList);

        // Updated Date/Time Logic: Show full Start and End Date/Time in the two TextViews
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        String startString = startAt != null ? df.format(startAt.toDate()) : "N/A";
        String endString = endAt != null ? df.format(endAt.toDate()) : "N/A";

        // Assigning Start Date+Time to tvDateLine and End Date+Time to tvTimeLine
        setText(getId("tvDateLine"), "Start: " + startString);
        setText(getId("tvTimeLine"), "End:   " + endString);

        setText(getId("tvEventTitle"), title);
        setText(getId("tvAddress"), location);
        setText(getId("tvDescription"), description);

        TextView org = findViewById(R.id.tvOrganizer);
        if (org != null && creator != null) org.setText("Hosted by: " + creator);

        setText(getId("spots_taken"), "(" + taken + "/" + max + ") Spots Taken");
        if (waitLimit > 0)
            setText(getId("waiting_list"), currentWaitCount + "/" + waitLimit + " on Waitlist");
        else
            setText(getId("waiting_list"), currentWaitCount + " on Waitlist");

        ImageView eventImage = findViewById(R.id.ivEventImage);
        String imageUrl = s.getString("imageUrl");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL(imageUrl);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    java.io.InputStream input = conn.getInputStream();
                    final android.graphics.Bitmap bmp =
                            android.graphics.BitmapFactory.decodeStream(input);

                    eventImage.post(() -> {
                        if (bmp != null) eventImage.setImageBitmap(bmp);
                        else eventImage.setImageResource(android.R.drawable.ic_menu_report_image);
                    });

                } catch (Exception e) {
                    eventImage.post(() ->
                            eventImage.setImageResource(android.R.drawable.ic_menu_report_image));
                }
            }).start();
        } else {
            eventImage.setImageResource(android.R.drawable.ic_menu_report_image);
        }
    }

    private void setText(int id, String value) {
        if (id == 0) return;
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(value == null ? "" : value);
    }

    private int getId(String name) {
        return getResources().getIdentifier(name, "id", getPackageName());
    }

    private int asInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(String.valueOf(o)); }
        catch (Exception e) { return 0; }
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
        });
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
            // when an invited user for an event declines, the lottery automatically selects a replacement user from the waitlist
            lotteryHelper.replaceCancelledUser(eventDocumentRef);
            applyButton.setBackgroundResource(R.drawable.apply_button);
            applyButton.setVisibility(View.VISIBLE);
            acceptInviteButton.setVisibility(View.GONE);
            declineInviteButton.setVisibility(View.GONE);
        });
    }

    /**
     * Exports current attendees (csvEntrants) to a CSV file in the public Downloads folder.
     */
    /*
    private void exportEntrantsToCsv() {
        if (csvEntrants.isEmpty()) {
            Toast.makeText(this, "No entrants to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Username\n");
        for (String user : csvEntrants) {
            sb.append(user).append("\n");
        }

        String fileName = "entrants_" + eventId + ".csv";
        exportCsvToDownloads(fileName, sb.toString());
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
                Toast.makeText(this, "CSV saved to Downloads", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to save CSV.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
        }
    }
    */
}