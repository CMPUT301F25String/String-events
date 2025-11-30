package com.example.string_events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class LotteryDrawActivityFirebaseTest {

    private static final String EVENTS_COLLECTION = "events";
    private static final String NOTIF_COLLECTION  = "notifications";

    @Test
    public void runLottery_updatesEventAndCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        String eventId = "test-lottery-event-" + System.currentTimeMillis();
        List<String> waitlist = Arrays.asList("lot_user1", "lot_user2", "lot_user3");
        List<String> invitedInitial = new ArrayList<>();

        //seed event document
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Lottery Test Event");
        data.put("location", "Test Hall");
        Timestamp now = Timestamp.now();
        data.put("startAt", now);
        data.put("endAt", new Timestamp(new Date(System.currentTimeMillis() + 3600_000L)));
        data.put("regEndAt", new Timestamp(new Date(System.currentTimeMillis() - 3600_000L)));
        data.put("waitlist", new ArrayList<>(waitlist));
        data.put("invited", new ArrayList<>(invitedInitial));
        data.put("lotteryRolled", false);
        data.put("maxAttendees", 2);
        data.put("attendeesCount", 0);

        CountDownLatch createLatch = new CountDownLatch(1);
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .set(data)
                .addOnSuccessListener(unused -> createLatch.countDown())
                .addOnFailureListener(e -> createLatch.countDown());
        createLatch.await(10, TimeUnit.SECONDS);

        // pre-clean notifications for this event
        CountDownLatch preCleanLatch = new CountDownLatch(1);
        db.collection(NOTIF_COLLECTION)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener((QuerySnapshot snaps) -> {
                    CountDownLatch inner = new CountDownLatch(snaps.size());
                    for (DocumentSnapshot s : snaps) {
                        db.collection(NOTIF_COLLECTION)
                                .document(s.getId())
                                .delete()
                                .addOnSuccessListener(v -> inner.countDown())
                                .addOnFailureListener(e -> inner.countDown());
                    }
                    try {
                        inner.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException ignored) { }
                    preCleanLatch.countDown();
                })
                .addOnFailureListener(e -> preCleanLatch.countDown());
        preCleanLatch.await(10, TimeUnit.SECONDS);

        Intent intent = new Intent(ctx, LotteryDrawActivity.class);
        intent.putExtra("eventId", eventId);

        ActivityScenario<LotteryDrawActivity> scenario =
                ActivityScenario.launch(intent);

        try {
            // call private runLottery()
            scenario.onActivity(activity -> {
                try {
                    // Ensure eventRef in activity is our eventRef
                    Field eventRefField = LotteryDrawActivity.class
                            .getDeclaredField("eventRef");
                    eventRefField.setAccessible(true);
                    DocumentReference eventRef =
                            FirebaseFirestore.getInstance()
                                    .collection(EVENTS_COLLECTION)
                                    .document(eventId);
                    eventRefField.set(activity, eventRef);

                    Method runMethod = LotteryDrawActivity.class
                            .getDeclaredMethod("runLottery");
                    runMethod.setAccessible(true);
                    runMethod.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            // poll Firestore for updated event
            AtomicReference<List<String>> invitedAfter = new AtomicReference<>(new ArrayList<>());
            AtomicReference<List<String>> waitlistAfter = new AtomicReference<>(new ArrayList<>());
            AtomicReference<Boolean> rolled = new AtomicReference<>(false);

            for (int i = 0; i < 12 && !rolled.get(); i++) {
                CountDownLatch checkLatch = new CountDownLatch(1);
                db.collection(EVENTS_COLLECTION)
                        .document(eventId)
                        .get()
                        .addOnSuccessListener((DocumentSnapshot snap) -> {
                            if (snap.exists()) {
                                Object invitedObj = snap.get("invited");
                                Object waitlistObj = snap.get("waitlist");
                                Boolean rolledFlag = snap.getBoolean("lotteryRolled");

                                if (invitedObj instanceof List) {
                                    invitedAfter.set((List<String>) invitedObj);
                                }
                                if (waitlistObj instanceof List) {
                                    waitlistAfter.set((List<String>) waitlistObj);
                                }
                                rolled.set(Boolean.TRUE.equals(rolledFlag));
                            }
                            checkLatch.countDown();
                        })
                        .addOnFailureListener(e -> checkLatch.countDown());
                checkLatch.await(10, TimeUnit.SECONDS);
                if (!rolled.get()) {
                    Thread.sleep(700);
                }
            }

            assertTrue("Lottery was not marked as rolled", rolled.get());
            assertEquals("Invited list size should be 2",
                    2, invitedAfter.get().size());
            assertEquals("Waitlist size should be 1",
                    1, waitlistAfter.get().size());

        } finally {
            // clean up notifications for this event
            CountDownLatch cleanNotifLatch = new CountDownLatch(1);
            db.collection(NOTIF_COLLECTION)
                    .whereEqualTo("eventId", eventId)
                    .get()
                    .addOnSuccessListener((QuerySnapshot snaps) -> {
                        CountDownLatch inner = new CountDownLatch(snaps.size());
                        for (DocumentSnapshot s : snaps) {
                            db.collection(NOTIF_COLLECTION)
                                    .document(s.getId())
                                    .delete()
                                    .addOnSuccessListener(v -> inner.countDown())
                                    .addOnFailureListener(e -> inner.countDown());
                        }
                        try {
                            inner.await(10, TimeUnit.SECONDS);
                        } catch (InterruptedException ignored) { }
                        cleanNotifLatch.countDown();
                    })
                    .addOnFailureListener(e -> cleanNotifLatch.countDown());
            cleanNotifLatch.await(10, TimeUnit.SECONDS);

            CountDownLatch deleteEventLatch = new CountDownLatch(1);
            db.collection(EVENTS_COLLECTION)
                    .document(eventId)
                    .delete()
                    .addOnSuccessListener(unused -> deleteEventLatch.countDown())
                    .addOnFailureListener(e -> deleteEventLatch.countDown());
            deleteEventLatch.await(10, TimeUnit.SECONDS);

            scenario.close();
        }
    }
}
