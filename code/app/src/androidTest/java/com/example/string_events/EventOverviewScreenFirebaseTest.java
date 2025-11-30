package com.example.string_events;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class EventOverviewScreenFirebaseTest {

    private static final String EVENTS_COLLECTION = "events";

    @Test
    public void loadEvent_bindsEventDataAndCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String eventId = "test-overview-event-" + System.currentTimeMillis();
        String title = "Overview Test Event " + System.currentTimeMillis();

        // fake event document
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("location", "Test Location");
        data.put("description", "EventOverviewScreen Firebase test");
        data.put("waitlistLimit", 12);
        data.put("attendeesCount", 3);
        data.put("startAt", Timestamp.now());
        data.put("endAt", Timestamp.now());
        data.put("regStartAt", Timestamp.now());
        data.put("regEndAt", Timestamp.now());
        data.put("categories", java.util.Arrays.asList("Games", "Arts"));

        CountDownLatch createLatch = new CountDownLatch(1);
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .set(data)
                .addOnSuccessListener(unused -> createLatch.countDown())
                .addOnFailureListener(e -> createLatch.countDown());
        createLatch.await(10, TimeUnit.SECONDS);

        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(ctx, EventOverviewScreen.class);
        intent.putExtra("event_id", eventId);

        ActivityScenario<EventOverviewScreen> scenario = ActivityScenario.launch(intent);

        try {
            AtomicBoolean bound = new AtomicBoolean(false);
            AtomicReference<String> tvTitleValue = new AtomicReference<>("");

            // poll the UI for tvEventName text to match our title
            for (int i = 0; i < 10 && !bound.get(); i++) {
                scenario.onActivity(activity -> {
                    TextView tvName = activity.findViewById(R.id.tvEventName);
                    if (tvName != null) {
                        String text = tvName.getText().toString();
                        tvTitleValue.set(text);
                        if (title.equals(text)) {
                            bound.set(true);
                        }
                    }
                });

                if (!bound.get()) {
                    Thread.sleep(700);
                }
            }

            assertTrue(
                    "EventOverviewScreen did not bind Firestore event title. Expected: "
                            + title + " but got: " + tvTitleValue.get(),
                    bound.get()
            );

        } finally {
            // clean up
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
