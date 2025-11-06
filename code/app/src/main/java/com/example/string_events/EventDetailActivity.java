package com.example.string_events;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firestore.v1.StructuredQuery;

import java.text.DateFormat;
import java.util.Locale;

public class EventDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference eventsCollectionRef;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_detail_screen);

        ImageView back = findViewById(getId("back_button"));
        if (back != null) back.setOnClickListener(v -> finish());

        String eventId = getIntent().getStringExtra("event_id");
        String username = getIntent().getStringExtra("user");
        if (eventId == null || eventId.isEmpty()) { finish(); return; }

        db = FirebaseFirestore.getInstance();
        eventsCollectionRef = db.collection("events");

        eventsCollectionRef.document(eventId).get()
                .addOnSuccessListener(this::bind)
                .addOnFailureListener(e -> finish());

        MaterialButton applyButton = findViewById(R.id.apply_button);

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUserToWaitlist(eventId, username);
                Toast.makeText(EventDetailActivity.this, "Added to waitlist!", Toast.LENGTH_SHORT).show();
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
        int wait  = asInt(s.get("waitlistLimit"));

        DateFormat dFmt = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        DateFormat tFmt = DateFormat.getTimeInstance(DateFormat.SHORT,  Locale.getDefault());
        String dateLine = st == null ? "" : dFmt.format(st.toDate());
        String timeLine = (st==null? "" : tFmt.format(st.toDate())) +
                " - " +
                (et==null? "" : tFmt.format(et.toDate()));

        setText(getId("tvEventTitle"),  title);
        setText(getId("tvAddress"),    addr);
        setText(getId("tvDateLine"),    dateLine);
        setText(getId("tvTimeLine"),    timeLine);
        setText(getId("tvAddress"),     loc);
        setText(getId("tvDescription"), desc);

        setText(getId("spots_taken"),  "(" + taken + "/" + max + ") Spots Taken");
        setText(getId("waiting_list"), wait + " Waiting List");
    }

    public void addUserToWaitlist(String eventId, String username) {
        // eventId is the id of the event that the user wants to join the waitlist for
        // username is the user to be added to the event's waitlist
        DocumentReference eventDocumentRef = eventsCollectionRef.document(eventId);
        // arrayUnion automatically won't add the username to the waitlist if it's already in there'
        eventDocumentRef.update("waitlist", FieldValue.arrayUnion(username));
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
