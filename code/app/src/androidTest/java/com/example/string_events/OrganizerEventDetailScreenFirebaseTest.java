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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class OrganizerEventDetailScreenFirebaseTest {

    private static final long TIMEOUT_SECONDS = 10L;

    @Test
    public void loadEvent_existingEvent_doesNotCrash_andCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String eventId = "test_detail_event_" + System.currentTimeMillis();
        DocumentReference docRef = db.collection("events").document(eventId);

        Map<String, Object> data = new HashMap<>();
        data.put("title", "Detail Screen Test Event");
        data.put("location", "Detail Test Hall");
        data.put("startAt", Timestamp.now());
        data.put("endAt", Timestamp.now());
        data.put("creator", "detail_test_creator");

        try {
            Tasks.await(docRef.set(data), TIMEOUT_SECONDS, TimeUnit.SECONDS);

            DocumentSnapshot snap =
                    Tasks.await(docRef.get(), TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertTrue(snap.exists());

            Intent intent = new Intent(
                    ApplicationProvider.getApplicationContext(),
                    OrganizerEventDetailScreen.class
            );
            intent.putExtra(OrganizerEventDetailScreen.EVENT_ID, eventId);

            try (ActivityScenario<OrganizerEventDetailScreen> scenario =
                         ActivityScenario.launch(intent)) {
                // fail if crash
            }
        } finally {
            Tasks.await(docRef.delete(), TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
    }
}
