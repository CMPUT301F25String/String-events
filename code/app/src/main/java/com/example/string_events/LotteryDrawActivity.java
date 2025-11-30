package com.example.string_events;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

/**
 * Activity that performs a lottery draw for an event's waitlist to fill
 * available attendee slots. Shows a header before the draw and an
 * after-state summary upon completion.
 */
public class LotteryDrawActivity extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final LotteryHelper lotteryHelper = new LotteryHelper();
    private DocumentReference eventRef;
    private boolean lotteryRolled;

    ConstraintLayout lotteryLayoutBlockedRoll;
    ConstraintLayout lotteryLayoutBeforeRoll;
    ConstraintLayout lotteryLayoutAfterRoll;
    TextView tvSelectedCount;
    ImageButton btnRoll;

    /**
     * Reads the target {@code event_id} from the intent, inflates the
     * "before roll" layout, binds header views, loads event header data,
     * and wires the Roll button to trigger {@link #runLottery()} ()}.
     *
     * @param savedInstanceState previously saved instance state, or {@code null}
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lottery_before_roll);

        String eventId = getIntent().getStringExtra("eventId");
        assert eventId != null;
        eventRef = db.collection("events").document(eventId);

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvEventName = findViewById(R.id.tvEventName);
        TextView tvTime = findViewById(R.id.tvTime);
        TextView tvLocation = findViewById(R.id.tvLocation);

        lotteryLayoutBlockedRoll = findViewById(R.id.lottery_layout_blocked_roll);
        lotteryLayoutBeforeRoll = findViewById(R.id.lottery_layout_before_roll);
        lotteryLayoutAfterRoll = findViewById(R.id.lottery_layout_after_roll);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        btnRoll = findViewById(R.id.btnRoll);


        eventRef.get().addOnSuccessListener(d -> {
            // get the event details from the database and update the screen's views with them
            tvEventName.setText(d.getString("title"));
            tvLocation.setText(d.getString("location"));

            // Updated Time Logic to show Start Date/Time - End Date/Time
            Timestamp startAt = d.getTimestamp("startAt");
            Timestamp endAt = d.getTimestamp("endAt");

            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
            String startStr = startAt != null ? df.format(startAt.toDate()) : "";
            String endStr = endAt != null ? df.format(endAt.toDate()) : "";

            if (!startStr.isEmpty() && !endStr.isEmpty()) {
                tvTime.setText(String.format("%s - %s", startStr, endStr));
            } else if (!startStr.isEmpty()) {
                tvTime.setText(startStr);
            }

            int invitedListSize = ((ArrayList<String>)Objects.requireNonNull(d.get("invited"))).size();
            tvSelectedCount.setText(String.format(Locale.CANADA, "%d participant(s) have been selected!", invitedListSize));
            lotteryRolled = Boolean.TRUE.equals(d.getBoolean("lotteryRolled"));

            // if the registration time has not ended yet, the organizer cannot roll event participants
            Timestamp registerEndTime = d.getTimestamp("regEndAt");
            assert registerEndTime != null;
            if (Timestamp.now().compareTo(registerEndTime) > 0) {
                // current time is after registration deadline
                lotteryLayoutBlockedRoll.setVisibility(View.GONE);
                // check if this event's lottery has already been rolled
                // if so, the organizer cannot reroll because it would be unfair for entrants
                if (lotteryRolled) {
                    lotteryLayoutBeforeRoll.setVisibility(View.GONE);
                    lotteryLayoutAfterRoll.setVisibility(View.VISIBLE);
                    btnRoll.setBackgroundResource(R.drawable.roll_button_unavailable);
                } else {
                    lotteryLayoutBeforeRoll.setVisibility(View.VISIBLE);
                    lotteryLayoutAfterRoll.setVisibility(View.GONE);
                    btnRoll.setBackgroundResource(R.drawable.roll_button_available);
                }
                // the roll button only does something if the event's lottery hasn't been rolled yet
                btnRoll.setOnClickListener(v -> {
                    if (!lotteryRolled) {
                        runLottery();
                    }
                });
            }
            else {
                // current time is before registration deadline
                lotteryLayoutBlockedRoll.setVisibility(View.VISIBLE);
                btnRoll.setBackgroundResource(R.drawable.roll_button_unavailable);
            }
            btnRoll.setVisibility(View.VISIBLE);
        });
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Executes the lottery:
     * <ol>
     * <li>Determines available slots from {@code maxAttendees - attendeesCount}.</li>
     * <li>Fetches the waitlist subcollection and randomly shuffles it.</li>
     * <li>Selects winners up to available slots, moves them to participants, and removes them from waitlist in a batch.</li>
     * <li>Updates event counters and timestamps, then switches UI to the "after roll" state.</li>
     * </ol>
     */

    @SuppressWarnings("unchecked")
    private void runLottery() {
        eventRef.get().addOnSuccessListener(eventSnap -> {
            // get the waitlist array of the event and count how many people are on it;
            ArrayList<String> waitlist = (ArrayList<String>) eventSnap.get("waitlist");
            assert waitlist != null;
            int waitlistCount = waitlist.size();
            // get the max attendees number for the event
            int maxAttendees = Integer.parseInt(String.valueOf(eventSnap.get("maxAttendees")));

            // maxAttendees >= waitlistCount means we don't need to roll at all and everyone gets an invite
            ArrayList<String> inviteList;
            if (maxAttendees >= waitlistCount) {
                // add everyone to invited list and clear the waitlist
                inviteList = new ArrayList<>(waitlist);
                waitlist.clear();
            }
            // otherwise, we shuffle the waitlist randomly and pick the first maxAttendees number as winners
            else {
                Collections.shuffle(waitlist);
                inviteList = new ArrayList<>(waitlist.subList(0, maxAttendees));
                waitlist.removeAll(inviteList);
            }
            lotteryHelper.sendLotteryNotifications(eventRef, inviteList, waitlist);
            // update the event's waitlist, invited list, and lotteryRolled boolean in the database
            db.runTransaction(transaction -> {
                transaction.update(eventRef, "waitlist", waitlist);
                transaction.update(eventRef, "invited", inviteList);
                transaction.update(eventRef, "lotteryRolled", true);
                lotteryRolled = true;
                return null;
            }).addOnSuccessListener(o -> Log.d("FirestoreCheck", "database transaction successful"));

            // change the look of the screen to show that rolling has been completed
            tvSelectedCount.setText(String.format(Locale.CANADA,"%d participant(s) have been selected!", inviteList.size()));
            lotteryLayoutBeforeRoll.setVisibility(View.GONE);
            lotteryLayoutAfterRoll.setVisibility(View.VISIBLE);
            btnRoll.setBackgroundResource(R.drawable.roll_button_unavailable);
        });
    }
}