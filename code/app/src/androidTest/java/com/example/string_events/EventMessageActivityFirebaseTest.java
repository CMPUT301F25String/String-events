package com.example.string_events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(AndroidJUnit4.class)
public class EventMessageActivityFirebaseTest {

    private static final String EVENTS_COLLECTION = "events";
    private static final String NOTIF_COLLECTION  = "notifications";

    @Test
    public void sendMessage_createsNotificationsAndCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String eventId = "test-msg-event-" + System.currentTimeMillis();
        String username = "msg_user_" + System.currentTimeMillis();
        String message = "Test broadcast message " + System.currentTimeMillis();

        // fake event
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", "EventMessageActivity test event");
        eventData.put("imageUrl", "https://example.com/test_img.png");
        eventData.put("attendees", Collections.singletonList(username));

        CountDownLatch createLatch = new CountDownLatch(1);
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .set(eventData)
                .addOnSuccessListener(unused -> createLatch.countDown())
                .addOnFailureListener(e -> createLatch.countDown());
        createLatch.await(10, TimeUnit.SECONDS);

        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Intent intent = new Intent(ctx, EventMessageActivity.class);
        intent.putExtra(OrganizerEventDetailScreen.EVENT_ID, eventId);
        intent.putExtra("target_group", "participating");

        ActivityScenario<EventMessageActivity> scenario =
                ActivityScenario.launch(intent);

        try {
            scenario.onActivity(activity -> {
                try {
                    // Inject Firestore instance
                    Field dbField = EventMessageActivity.class.getDeclaredField("db");
                    dbField.setAccessible(true);
                    dbField.set(activity, db);

                    EditText etMessage = activity.findViewById(R.id.etMessage);
                    if (etMessage != null) {
                        etMessage.setText(message);
                    }

                    Method sendMethod = EventMessageActivity.class
                            .getDeclaredMethod("sendMessage", String.class, String.class);
                    sendMethod.setAccessible(true);
                    sendMethod.invoke(activity, eventId, "participating");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            // poll notifications collection for our message
            AtomicInteger foundCount = new AtomicInteger(0);
            String[] foundDocId = new String[1];

            for (int i = 0; i < 10 && foundCount.get() == 0; i++) {
                CountDownLatch queryLatch = new CountDownLatch(1);
                db.collection(NOTIF_COLLECTION)
                        .whereEqualTo("eventId", eventId)
                        .whereEqualTo("ismessage", true)
                        .whereEqualTo("message", message)
                        .get()
                        .addOnSuccessListener((QuerySnapshot snaps) -> {
                            if (!snaps.isEmpty()) {
                                DocumentSnapshot snap = snaps.getDocuments().get(0);
                                foundCount.set(snaps.size());
                                foundDocId[0] = snap.getId();
                            }
                            queryLatch.countDown();
                        })
                        .addOnFailureListener(e -> queryLatch.countDown());

                queryLatch.await(10, TimeUnit.SECONDS);
                if (foundCount.get() == 0) {
                    Thread.sleep(700);
                }
            }

            assertTrue("No notification was created for the message", foundCount.get() > 0);

            // back to the notification to verify username
            CountDownLatch readLatch = new CountDownLatch(1);
            final String[] storedUsername = new String[1];
            db.collection(NOTIF_COLLECTION)
                    .document(foundDocId[0])
                    .get()
                    .addOnSuccessListener(snap -> {
                        storedUsername[0] = snap.getString("username");
                        readLatch.countDown();
                    })
                    .addOnFailureListener(e -> readLatch.countDown());
            readLatch.await(10, TimeUnit.SECONDS);

            assertEquals("Notification username mismatch", username, storedUsername[0]);

            // clean up
            CountDownLatch deleteNotifLatch = new CountDownLatch(1);
            db.collection(NOTIF_COLLECTION)
                    .document(foundDocId[0])
                    .delete()
                    .addOnSuccessListener(unused -> deleteNotifLatch.countDown())
                    .addOnFailureListener(e -> deleteNotifLatch.countDown());
            deleteNotifLatch.await(10, TimeUnit.SECONDS);

        } finally {
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
