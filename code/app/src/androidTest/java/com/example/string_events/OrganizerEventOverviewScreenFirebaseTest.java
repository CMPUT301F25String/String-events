package com.example.string_events;

import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class OrganizerEventOverviewScreenFirebaseTest {

    private static final long TIMEOUT_SECONDS = 10L;

    @Test
    public void deleteEvent_removesDocument_andCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String eventId = "test_delete_event_" + System.currentTimeMillis();
        DocumentReference docRef = db.collection("events").document(eventId);

        Map<String, Object> data = new HashMap<>();
        data.put("title", "Delete Me");
        data.put("location", "Test Location");
        data.put("startAt", Timestamp.now());
        data.put("endAt", Timestamp.now());
        data.put("creator", "test_delete_creator");
        data.put("waitlist", new ArrayList<String>());
        data.put("attendees", new ArrayList<String>());
        Tasks.await(docRef.set(data), TIMEOUT_SECONDS, TimeUnit.SECONDS);

        try (ActivityScenario<OrganizerEventOverviewScreen> scenario =
                     ActivityScenario.launch(
                             new Intent(
                                     ApplicationProvider.getApplicationContext(),
                                     OrganizerEventOverviewScreen.class
                             ).putExtra("event_id", eventId)
                     )) {

            scenario.onActivity(activity -> {
                try {
                    Method m = OrganizerEventOverviewScreen.class
                            .getDeclaredMethod("deleteEvent", String.class);
                    m.setAccessible(true);
                    m.invoke(activity, eventId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        boolean deleted = false;
        try {
            for (int i = 0; i < 10; i++) {
                DocumentSnapshot snap =
                        Tasks.await(docRef.get(), TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (!snap.exists()) {
                    deleted = true;
                    break;
                }
                Thread.sleep(300);
            }
        } finally {
            Tasks.await(docRef.delete(), TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }

        assertTrue("Event document should be deleted", deleted);
    }

    @Test
    public void loadEvent_existingEvent_doesNotCrash_andCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String eventId = "test_load_overview_event_" + System.currentTimeMillis();
        DocumentReference docRef = db.collection("events").document(eventId);

        Map<String, Object> data = new HashMap<>();
        data.put("title", "Overview Screen Test Event");
        data.put("location", "Overview Test Hall");
        data.put("description", "Overview description");
        data.put("startAt", Timestamp.now());
        data.put("endAt", Timestamp.now());
        data.put("waitlistLimit", 10);
        data.put("attendeesCount", 0);
        data.put("creator", "overview_test_creator");
        data.put("waitlist", new ArrayList<String>());
        data.put("attendees", new ArrayList<String>());

        try {
            Tasks.await(docRef.set(data), TIMEOUT_SECONDS, TimeUnit.SECONDS);

            Intent intent = new Intent(
                    ApplicationProvider.getApplicationContext(),
                    OrganizerEventOverviewScreen.class
            );
            intent.putExtra("event_id", eventId);

            try (ActivityScenario<OrganizerEventOverviewScreen> scenario =
                         ActivityScenario.launch(intent)) {
            }
        } finally {
            Tasks.await(docRef.delete(), TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
    }
}
