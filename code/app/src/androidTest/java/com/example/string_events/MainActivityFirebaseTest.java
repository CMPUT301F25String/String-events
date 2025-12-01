package com.example.string_events;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(AndroidJUnit4.class)
public class MainActivityFirebaseTest {

    private static final String EVENTS_COLLECTION = "events";

    @Test
    public void loadEventsIntoList_loadsFromFirestoreAndCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        String title = "MAIN LOAD TEST " + System.currentTimeMillis();
        String eventId = "main-load-" + System.currentTimeMillis();

        // 1. Seed one test event document
        Map<String, Object> inc = new HashMap<>();
        inc.put("title", title);
        inc.put("location", "Load Test Hall");
        Timestamp future = new Timestamp(new Date(System.currentTimeMillis() + 3600_000L));
        inc.put("startAt", future);
        inc.put("endAt", future);
        inc.put("regStartAt", new Timestamp(new Date()));
        inc.put("regEndAt", future);
        inc.put("maxAttendees", 10);
        inc.put("attendeesCount", 0);
        inc.put("categories", java.util.Arrays.asList("TestTag"));
        inc.put("visibility", true);

        CountDownLatch createLatch = new CountDownLatch(1);
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .set(inc)
                .addOnSuccessListener(unused -> createLatch.countDown())
                .addOnFailureListener(e -> createLatch.countDown());
        createLatch.await(10, TimeUnit.SECONDS);

        // 2. Launch MainActivity
        Intent intent = new Intent(ctx, MainActivity.class);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);

        try {
            // 3. Explicitly call private loadEventsIntoList(db.collection("events"))
            scenario.onActivity(activity -> {
                try {
                    Method m = MainActivity.class
                            .getDeclaredMethod("loadEventsIntoList", Query.class);
                    m.setAccessible(true);
                    Query q = FirebaseFirestore.getInstance()
                            .collection(EVENTS_COLLECTION);
                    m.invoke(activity, q);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            // 4. Poll ListView adapter count until > 0
            AtomicInteger countHolder = new AtomicInteger(0);

            for (int i = 0; i < 14 && countHolder.get() == 0; i++) { // ~10 秒
                scenario.onActivity(activity -> {
                    ListView lv = activity.findViewById(R.id.list);
                    if (lv == null) return;
                    BaseAdapter adapter = (BaseAdapter) lv.getAdapter();
                    if (adapter == null) return;
                    countHolder.set(adapter.getCount());
                });

                if (countHolder.get() > 0) break;
                Thread.sleep(700);
            }

            assertTrue("Event list did not load any items from Firestore",
                    countHolder.get() > 0);

        } finally {
            // 5. Clean up the test event document
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
