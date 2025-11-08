package com.example.string_events;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity that performs a lottery draw for an event's waitlist to fill
 * available attendee slots. Shows a header before the draw and an
 * after-state summary upon completion.
 */
public class LotteryDrawActivity extends AppCompatActivity {
    private static final String COL_EVENTS = "events";
    private static final String SUB_WAITLIST = "waitlist";
    private static final String SUB_PARTICIPANTS = "participants";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String eventId;

    private TextView tvEventName, tvTime, tvLocation;
    private ImageButton btnBack;
    private MaterialButton btnRoll;

    private final DateFormat dfTime = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());

    /**
     * Reads the target {@code event_id} from the intent, inflates the
     * "before roll" layout, binds header views, loads event header data,
     * and wires the Roll button to trigger {@link #performRoll()}.
     *
     * @param savedInstanceState previously saved instance state, or {@code null}
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventId = getIntent().getStringExtra("event_id");
        setContentView(R.layout.lottery_before_roll);
        bindHeaderViews();
        loadEventHeader();
        if (btnRoll != null) {
            btnRoll.setOnClickListener(v -> performRoll());
        }
    }

    /**
     * Binds header views and sets the back button to finish the activity.
     */
    private void bindHeaderViews() {
        btnBack = findViewById(R.id.btnBack);
        tvEventName = findViewById(R.id.tvEventName);
        tvTime = findViewById(R.id.tvTime);
        tvLocation = findViewById(R.id.tvLocation);
        btnRoll = findViewById(R.id.btnRoll);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Loads event title, location, and start time from Firestore and
     * populates the header fields.
     */
    private void loadEventHeader() {
        if (eventId == null) return;
        DocumentReference eventRef = db.collection(COL_EVENTS).document(eventId);
        eventRef.get().addOnSuccessListener(d -> {
            if (d == null || !d.exists()) return;
            String title = d.getString("title");
            String location = d.getString("location");
            Timestamp startAt = d.getTimestamp("startAt");
            if (tvEventName != null) tvEventName.setText(title == null ? "" : title);
            if (tvLocation != null) tvLocation.setText(location == null ? "" : location);
            if (tvTime != null) tvTime.setText(startAt == null ? "" : dfTime.format(startAt.toDate()));
        });
    }

    /**
     * Executes the lottery:
     * <ol>
     *   <li>Determines available slots from {@code maxAttendees - attendeesCount}.</li>
     *   <li>Fetches the waitlist subcollection and randomly shuffles it.</li>
     *   <li>Selects winners up to available slots, moves them to participants, and removes them from waitlist in a batch.</li>
     *   <li>Updates event counters and timestamps, then switches UI to the "after roll" state.</li>
     * </ol>
     */
    private void performRoll() {
        if (eventId == null) return;

        DocumentReference eventRef = db.collection(COL_EVENTS).document(eventId);
        CollectionReference waitRef = eventRef.collection(SUB_WAITLIST);
        CollectionReference partRef = eventRef.collection(SUB_PARTICIPANTS);

        eventRef.get().addOnSuccessListener(eventSnap -> {
            if (eventSnap == null || !eventSnap.exists()) return;

            int maxAtt = safeInt(eventSnap.get("maxAttendees"));
            int attCnt = safeInt(eventSnap.get("attendeesCount"));
            int slots = Math.max(0, maxAtt - attCnt);
            if (slots <= 0) {
                switchToAfter(0);
                return;
            }

            waitRef.get().addOnSuccessListener((QuerySnapshot waitSnap) -> {
                List<DocumentSnapshot> waitDocs = waitSnap == null ? new ArrayList<>() : waitSnap.getDocuments();
                if (waitDocs.isEmpty()) {
                    switchToAfter(0);
                    return;
                }

                Collections.shuffle(waitDocs);
                int pick = Math.min(slots, waitDocs.size());
                List<DocumentSnapshot> winners = waitDocs.subList(0, pick);

                WriteBatch batch = db.batch();
                for (DocumentSnapshot w : winners) {
                    DocumentReference from = waitRef.document(w.getId());
                    DocumentReference to = partRef.document(w.getId());
                    Map<String, Object> data = w.getData();
                    if (data != null) {
                        data.put("selected", true);
                        data.put("selectedAt", FieldValue.serverTimestamp());
                        batch.set(to, data, SetOptions.merge());
                    } else {
                        batch.set(to, Collections.singletonMap("selected", true), SetOptions.merge());
                        batch.update(to, "selectedAt", FieldValue.serverTimestamp());
                    }
                    batch.delete(from);
                }
                batch.update(eventRef, "attendeesCount", FieldValue.increment(pick));
                batch.update(eventRef, "lastRolledAt", FieldValue.serverTimestamp());

                batch.commit()
                        .addOnSuccessListener(unused -> switchToAfter(pick))
                        .addOnFailureListener(e -> { });
            });
        });
    }

    /**
     * Switches the layout to the "after roll" screen, rebinds and reloads
     * the header, and shows the number of selected participants.
     *
     * @param selectedCount number of participants chosen by the draw
     */
    private void switchToAfter(int selectedCount) {
        setContentView(R.layout.lottery_after_roll);
        bindHeaderViews();
        loadEventHeader();

        TextView tvSelected = findViewById(R.id.tvSelectedCount);
        TextView tvNotified = findViewById(R.id.tvNotified);
        if (tvSelected != null) {
            tvSelected.setText(selectedCount + " participants have been selected !");
        }
        if (btnRoll != null) {
            btnRoll.setEnabled(false);
        }
        if (tvNotified != null) { }
    }

    /**
     * Parses an arbitrary object into an {@code int}, returning 0 on null or failure.
     *
     * @param o value to parse
     * @return parsed integer or 0 if not parsable
     */
    private int safeInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return 0; }
    }
}
