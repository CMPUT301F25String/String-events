package com.example.string_events;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

    private boolean fromTest = false;
    private String eventId;
    private String username;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_detail_screen);

        ImageView back = findViewById(getId("back_button"));
        if (back != null) back.setOnClickListener(v -> finish());

        // --------- 测试兜底 ----------
        Intent it = getIntent();
        fromTest = it != null && it.getBooleanExtra("fromTest", false);

        // 本页实际读取的是 "event_id"
        eventId = (it != null) ? it.getStringExtra("event_id") : null;

        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        username = sp.getString("user", null);

        if (fromTest) {
            if (eventId == null || eventId.isEmpty()) eventId = "test-event";
            if (username == null) {
                sp.edit().putString("user", "ui-tester").apply();
                username = "ui-tester";
            }
        }
        if ((eventId == null || eventId.isEmpty() || username == null) && !fromTest) {
            finish();
            return;
        }
        // ---------------------------

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsCollectionRef = db.collection("events");

        eventsCollectionRef.document(eventId).get()
                .addOnSuccessListener(this::bind)
                .addOnFailureListener(e -> {
                    if (!fromTest) finish();  // 测试下不要直接退出
                });

        ImageButton applyButton = findViewById(R.id.apply_button);

        AtomicBoolean userInEventWaitlist = new AtomicBoolean();
        DocumentReference eventDocumentRef = eventsCollectionRef.document(eventId);
        eventDocumentRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                @SuppressWarnings("unchecked")
                List<String> waitlist = (List<String>) documentSnapshot.get("waitlist");
                if (waitlist != null && waitlist.contains(username)) {
                    Log.d("FirestoreCheck", "already in waitlist");
                    userInEventWaitlist.set(true);
                    applyButton.setBackgroundResource(R.drawable.cancel_apply_button);
                } else {
                    Log.d("FirestoreCheck", "not on waitlist");
                    userInEventWaitlist.set(false);
                    applyButton.setBackgroundResource(R.drawable.apply_button);
                }
            } else {
                Log.d("FirestoreCheck", "Document does not exist.");
            }
        }).addOnFailureListener(e -> Log.e("FirestoreCheck", "Error fetching document", e));

        applyButton.setOnClickListener(view -> {
            if (fromTest) {
                // ❗测试模式：不要弹 Toast（会产生无焦点窗口），只做 UI 切换
                if (!userInEventWaitlist.get()) {
                    userInEventWaitlist.set(true);
                    applyButton.setBackgroundResource(R.drawable.cancel_apply_button);
                } else {
                    userInEventWaitlist.set(false);
                    applyButton.setBackgroundResource(R.drawable.apply_button);
                }
                return;
            }

            // 真实逻辑（保留 Toast）
            if (!userInEventWaitlist.get()) {
                eventDocumentRef.update("waitlist", FieldValue.arrayUnion(username));
                Toast.makeText(EventDetailActivity.this, "Added to waitlist!", Toast.LENGTH_SHORT).show();
                userInEventWaitlist.set(true);
                applyButton.setBackgroundResource(R.drawable.cancel_apply_button);
            } else {
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

        @SuppressWarnings("unchecked")
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
        setText(getId("tvAddress"),     loc);      // 维持你原有的写法
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
