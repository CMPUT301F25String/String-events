package com.example.string_events;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Locale;

public class EventDetailActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        ImageView back = findViewById(getId("back_button"));
        if (back != null) back.setOnClickListener(v -> finish());

        String eventId = getIntent().getStringExtra("event_id");
        if (eventId == null || eventId.isEmpty()) { finish(); return; }

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(this::bind)
                .addOnFailureListener(e -> finish());
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
