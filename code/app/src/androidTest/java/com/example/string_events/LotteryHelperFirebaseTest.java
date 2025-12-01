package com.example.string_events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(AndroidJUnit4.class)
public class LotteryHelperFirebaseTest {

    private static final String EVENTS_COLLECTION = "events";
    private static final String NOTIF_COLLECTION  = "notifications";

    @Test
    public void sendLotteryNotifications_createsDocsAndCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        LotteryHelper helper = new LotteryHelper();

        String eventId = "test-lottery-helper-" + System.currentTimeMillis();

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", "Helper Test Event");
        eventData.put("imageUrl", "https://example.com/helper.png");

        CountDownLatch createLatch = new CountDownLatch(1);
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .set(eventData)
                .addOnSuccessListener(unused -> createLatch.countDown())
                .addOnFailureListener(e -> createLatch.countDown());
        createLatch.await(10, TimeUnit.SECONDS);

        DocumentReference eventRef =
                db.collection(EVENTS_COLLECTION).document(eventId);

        // Pre-clean notifications for this event
        CountDownLatch preCleanLatch = new CountDownLatch(1);
        db.collection(NOTIF_COLLECTION)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(snaps -> {
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

        //Prepare invite and wait lists
        ArrayList<String> inviteList = new ArrayList<>();
        inviteList.add("helper_invitee1");
        inviteList.add("helper_invitee2");

        ArrayList<String> waitList = new ArrayList<>();
        waitList.add("helper_wait1");

        try {
            // call helper.sendLotteryNotifications
            helper.sendLotteryNotifications(eventRef, inviteList, waitList);

            // notifications
            AtomicInteger notifCount = new AtomicInteger(0);

            for (int i = 0; i < 12 && notifCount.get() == 0; i++) {
                CountDownLatch queryLatch = new CountDownLatch(1);
                db.collection(NOTIF_COLLECTION)
                        .whereEqualTo("eventId", eventId)
                        .get()
                        .addOnSuccessListener((QuerySnapshot snaps) -> {
                            notifCount.set(snaps.size());
                            queryLatch.countDown();
                        })
                        .addOnFailureListener(e -> queryLatch.countDown());
                queryLatch.await(10, TimeUnit.SECONDS);
                if (notifCount.get() == 0) {
                    Thread.sleep(700);
                }
            }

            assertTrue("No notifications were created for helper test",
                    notifCount.get() > 0);

            CountDownLatch readLatch = new CountDownLatch(1);
            final int[] winners = {0};
            final int[] losers = {0};
            db.collection(NOTIF_COLLECTION)
                    .whereEqualTo("eventId", eventId)
                    .get()
                    .addOnSuccessListener(snaps -> {
                        for (DocumentSnapshot s : snaps) {
                            Boolean selected = s.getBoolean("selectedStatus");
                            if (Boolean.TRUE.equals(selected)) {
                                winners[0]++;
                            } else {
                                losers[0]++;
                            }
                        }
                        readLatch.countDown();
                    })
                    .addOnFailureListener(e -> readLatch.countDown());
            readLatch.await(10, TimeUnit.SECONDS);

            assertEquals(inviteList.size(), winners[0]);
            assertEquals(waitList.size(), losers[0]);

        } finally {
            //clean up notifications
            CountDownLatch cleanNotifLatch = new CountDownLatch(1);
            db.collection(NOTIF_COLLECTION)
                    .whereEqualTo("eventId", eventId)
                    .get()
                    .addOnSuccessListener(snaps -> {
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

            // 7. Clean up event document
            CountDownLatch deleteEventLatch = new CountDownLatch(1);
            db.collection(EVENTS_COLLECTION)
                    .document(eventId)
                    .delete()
                    .addOnSuccessListener(unused -> deleteEventLatch.countDown())
                    .addOnFailureListener(e -> deleteEventLatch.countDown());
            deleteEventLatch.await(10, TimeUnit.SECONDS);
        }
    }
}
