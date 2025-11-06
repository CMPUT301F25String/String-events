package com.example.string_events;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventDetailActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_detail_screen);

        ImageView back = findViewById(getId("back_button"));
        if (back != null) back.setOnClickListener(v -> finish());

        String eventId = getIntent().getStringExtra("event_id");
        String username = getIntent().getStringExtra("user");
        if (eventId == null || username == null || eventId.isEmpty()) { finish(); return; }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsCollectionRef = db.collection("events");

        eventsCollectionRef.document(eventId).get()
                .addOnSuccessListener(this::bind)
                .addOnFailureListener(e -> finish());

        ImageButton applyButton = findViewById(R.id.apply_button);

        AtomicBoolean userInEventWaitlist = new AtomicBoolean();
        DocumentReference eventDocumentRef = eventsCollectionRef.document(eventId);
        eventDocumentRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                @SuppressWarnings("unchecked")
                // get the waitlist of the clicked event from the database
                List<String> waitlist = (List<String>) documentSnapshot.get("waitlist");
                if (waitlist != null && waitlist.contains(username)) {
                    // user is already waitlisted in the event
                    Log.d("FirestoreCheck", "already in waitlist");
                    userInEventWaitlist.set(true);
                    applyButton.setBackgroundResource(R.drawable.cancel_apply_button);
                } else {
                    // user isn't waitlisted in the event yet
                    Log.d("FirestoreCheck", "not on waitlist");
                    userInEventWaitlist.set(false);
                    applyButton.setBackgroundResource(R.drawable.apply_button);
                }
            } else {
                Log.d("FirestoreCheck", "Document does not exist.");
            }
        }).addOnFailureListener(e -> {
            Log.e("FirestoreCheck", "Error fetching document", e);
        });

        applyButton.setOnClickListener(view -> {
            // if user isn't in the event waitlist yet
            if (!userInEventWaitlist.get()) {
                // adds the user to the event waitlist
                eventDocumentRef.update("waitlist", FieldValue.arrayUnion(username));
                Toast.makeText(EventDetailActivity.this, "Added to waitlist!", Toast.LENGTH_SHORT).show();
                userInEventWaitlist.set(true);
                applyButton.setBackgroundResource(R.drawable.cancel_apply_button);
            }
            // user is in the event waitlist already
            else {
                // removes the user from the event waitlist
                eventDocumentRef.update("waitlist", FieldValue.arrayRemove(username));
                Toast.makeText(EventDetailActivity.this, "Removed from waitlist!", Toast.LENGTH_SHORT).show();
                userInEventWaitlist.set(false);
                applyButton.setBackgroundResource(R.drawable.apply_button);
            }
        });
    }

    private void bind(DocumentSnapshot s) {
        String title = s.getString("title");
        String desc  = s.getString("description");
        String loc   = s.getString("location");
        String addr  = s.getString("address");
        Timestamp st = s.getTimestamp("startAt");
        Timestamp et = s.getTimestamp("endAt");
        int max   = asInt(s.get("maxAttendees"));
        int taken = asInt(s.get("attendeesCount"));
        int waitLimit  = asInt(s.get("waitlistLimit"));


        List<String> waitlist = (List<String>) s.get("waitlist");
        int currentWaitCount = (waitlist != null) ? waitlist.size() : 0;

        DateFormat dFmt = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        DateFormat tFmt = DateFormat.getTimeInstance(DateFormat.SHORT,  Locale.getDefault());
        String dateLine = st == null ? "" : dFmt.format(st.toDate());
        String timeLine = (st==null? "" : tFmt.format(st.toDate())) +
                " - " +
                (et==null? "" : tFmt.format(et.toDate()));

        setText(getId("tvEventTitle"),  title);
        setText(getId("tvAddress"),     addr);
        setText(getId("tvDateLine"),    dateLine);
        setText(getId("tvTimeLine"),    timeLine);
        setText(getId("tvAddress"),     loc);
        setText(getId("tvDescription"), desc);

        setText(getId("spots_taken"),  "(" + taken + "/" + max + ") Spots Taken");


        if (waitLimit > 0)
            setText(getId("waiting_list"), currentWaitCount + "/" + waitLimit + " on Waitlist");
        else
            setText(getId("waiting_list"), currentWaitCount + " on Waitlist");
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
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return 0; }
    }
}
