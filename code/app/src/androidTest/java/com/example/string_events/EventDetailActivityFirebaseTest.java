package com.example.string_events;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.widget.ImageButton;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(AndroidJUnit4.class)
public class EventDetailActivityFirebaseTest {

    private static final String EVENTS_COLLECTION = "events";

    @Test
    public void addUserToEventWaitlist_addsUsernameAndCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String eventId = "test-detail-event-" + System.currentTimeMillis();
//        String eventId = "e299ee34-3977-491e-8f2b-0ce30bd7e447";
        String username = "detail_user_abc";

//         seed Firestore with a test event document
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", "EventDetailActivity test event");
        eventData.put("waitlist", new ArrayList<String>());
        eventData.put("attendees", new ArrayList<String>());

        CountDownLatch createLatch = new CountDownLatch(1);
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .set(eventData)
                .addOnSuccessListener(unused -> createLatch.countDown())
                .addOnFailureListener(e -> createLatch.countDown());
        createLatch.await(10, TimeUnit.SECONDS);

        // set SharedPreferences userInfo.user
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ctx.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
                .edit()
                .putString("user", username)
                .commit();

        Intent intent = new Intent(ctx, EventDetailActivity.class);
        intent.putExtra("event_id", eventId);

        ActivityScenario<EventDetailActivity> scenario = ActivityScenario.launch(intent);

        try {
            // call private method addUserToEventWaitlist(ImageButton)
            scenario.onActivity(activity -> {
                try {
                    Field usernameField =
                            EventDetailActivity.class.getDeclaredField("username");
                    usernameField.setAccessible(true);
                    usernameField.set(activity, username);

                    Field dbField =
                            EventDetailActivity.class.getDeclaredField("db");
                    dbField.setAccessible(true);
                    dbField.set(activity, db);

                    Field eventRefField =
                            EventDetailActivity.class.getDeclaredField("eventDocumentRef");
                    eventRefField.setAccessible(true);
                    eventRefField.set(activity,
                            db.collection(EVENTS_COLLECTION).document(eventId));

                    ImageButton applyButton = activity.findViewById(R.id.apply_button);
                    Method addMethod = EventDetailActivity.class
                            .getDeclaredMethod("addUserToEventWaitlist", ImageButton.class);
                    addMethod.setAccessible(true);
                    addMethod.invoke(activity, applyButton);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            // poll Firestore until the waitlist contains our username
            AtomicBoolean inWaitlist = new AtomicBoolean(false);

            for (int i = 0; i < 8 && !inWaitlist.get(); i++) {
                CountDownLatch checkLatch = new CountDownLatch(1);
                db.collection(EVENTS_COLLECTION)
                        .document(eventId)
                        .get()
                        .addOnSuccessListener((DocumentSnapshot snap) -> {
                            if (snap.exists()) {
                                @SuppressWarnings("unchecked")
                                java.util.List<String> waitlist =
                                        (java.util.List<String>) snap.get("waitlist");
                                if (waitlist != null && waitlist.contains(username)) {
                                    inWaitlist.set(true);
                                }
                            }
                            checkLatch.countDown();
                        })
                        .addOnFailureListener(e -> checkLatch.countDown());

                checkLatch.await(10, TimeUnit.SECONDS);
                if (!inWaitlist.get()) {
                    Thread.sleep(2000);
                }
            }
            db.collection(EVENTS_COLLECTION)
                    .document(eventId)
                    .update("waitlist", FieldValue.arrayRemove(username)).addOnSuccessListener(aVoid -> {
                        assertTrue("Username was not added to event waitlist in Firestore", inWaitlist.get());
                    });
        } finally {
            CountDownLatch cleanupLatch = new CountDownLatch(1);
            db.collection(EVENTS_COLLECTION)
                    .document(eventId)
                    .update("waitlist", FieldValue.arrayRemove(username))
                    .addOnSuccessListener(aVoid -> {
                        cleanupLatch.countDown();
                    })
                    .addOnFailureListener(e -> {
                        cleanupLatch.countDown();
                    });
            assertTrue("Cleanup timed out: Failed to remove user from waitlist.",
                    cleanupLatch.await(10, TimeUnit.SECONDS));

//             clean up Firestore event document
            CountDownLatch deleteLatch = new CountDownLatch(1);
            db.collection(EVENTS_COLLECTION)
                    .document(eventId)
                    .delete()
                    .addOnSuccessListener(unused -> deleteLatch.countDown())
                    .addOnFailureListener(e -> deleteLatch.countDown());
            deleteLatch.await(10, TimeUnit.SECONDS);

            scenario.close();
        }
    }
}
