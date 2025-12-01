package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class OrganizerEventDetailsTest {

    private Intent makeIntent(String eventId) {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent i = new Intent(ctx, OrganizerEventDetailScreen.class);
        i.putExtra(OrganizerEventDetailScreen.EVENT_ID, eventId);
        return i;
    }

    @Test
    public void launch_loadsEventFromFirestore_andShowsTitleAndLocation() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String eventId = "instrumented_detail_" + System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("title", "UI Test Title");
        data.put("location", "UI Test Location");
        Timestamp now = Timestamp.now();
        data.put("startAt", now);
        data.put("endAt", now);

        Tasks.await(
                db.collection("events").document(eventId).set(data),
                10, TimeUnit.SECONDS
        );

        ActivityScenario<OrganizerEventDetailScreen> scenario = null;

        try {
            scenario = ActivityScenario.launch(makeIntent(eventId));

            Thread.sleep(2000);

            onView(withId(R.id.tvEventName))
                    .check(matches(withText("UI Test Title")));

            onView(withId(R.id.tvLocation))
                    .check(matches(isDisplayed()));
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
