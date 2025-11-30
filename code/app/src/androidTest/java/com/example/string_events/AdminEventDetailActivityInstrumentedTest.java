package com.example.string_events;

import static org.junit.Assert.assertFalse;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class AdminEventDetailActivityInstrumentedTest {

    @Test
    public void deleteEvent_removesDocumentFromFirestore() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String eventId = "instrumented_delete_" + System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("title", "Delete Test");
        data.put("creator", "tester");

        Tasks.await(
                db.collection("events").document(eventId).set(data),
                10, TimeUnit.SECONDS
        );

        ActivityScenario<AdminEventDetailActivity> scenario = null;

        try {
            Intent intent = new Intent(
                    ApplicationProvider.getApplicationContext(),
                    AdminEventDetailActivity.class);
            intent.putExtra("event_id", eventId);

            scenario = ActivityScenario.launch(intent);

            ActivityScenario<AdminEventDetailActivity> finalScenario = scenario;
            finalScenario.onActivity(activity -> {
                try {
                    java.lang.reflect.Method m =
                            AdminEventDetailActivity.class.getDeclaredMethod("deleteEvent");
                    m.setAccessible(true);
                    m.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Thread.sleep(2000);

            DocumentSnapshot snapshot = Tasks.await(
                    db.collection("events").document(eventId).get(),
                    10, TimeUnit.SECONDS
            );
            assertFalse(snapshot.exists());
        } finally {
            if (scenario != null) {
                scenario.close();
            }
            try {
                Tasks.await(
                        db.collection("events").document(eventId).delete(),
                        5, TimeUnit.SECONDS
                );
            } catch (Exception ignore) {
            }
        }
    }
}
