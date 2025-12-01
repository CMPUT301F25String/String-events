package com.example.string_events;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class OrganizerEventScreenFirebaseTest {

    private static final long TIMEOUT_SECONDS = 10L;

    @Test
    public void loadMyEvents_existingEvents_doesNotCrash_andCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Context appContext = ApplicationProvider.getApplicationContext();

        String testCreator = "test_my_events_user_" + System.currentTimeMillis();

        String eventId1 = "test_my_events_1_" + System.currentTimeMillis();
        String eventId2 = "test_my_events_2_" + System.currentTimeMillis();

        DocumentReference doc1 = db.collection("events").document(eventId1);
        DocumentReference doc2 = db.collection("events").document(eventId2);

        Map<String, Object> baseData = new HashMap<>();
        baseData.put("location", "My Events Location");
        baseData.put("startAt", Timestamp.now());
        baseData.put("endAt", Timestamp.now());
        baseData.put("creator", testCreator);

        try {
            Map<String, Object> data1 = new HashMap<>(baseData);
            data1.put("title", "My Event One");
            Map<String, Object> data2 = new HashMap<>(baseData);
            data2.put("title", "My Event Two");

            Tasks.await(doc1.set(data1), TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Tasks.await(doc2.set(data2), TIMEOUT_SECONDS, TimeUnit.SECONDS);

            SharedPreferences sp =
                    appContext.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            sp.edit().putString("user", testCreator).commit();

            Intent intent = new Intent(appContext, OrganizerEventScreen.class);

            try (ActivityScenario<OrganizerEventScreen> scenario =
                         ActivityScenario.launch(intent)) {
                //fail if crash
            }

            assertTrue(Tasks.await(doc1.get(), TIMEOUT_SECONDS, TimeUnit.SECONDS).exists());
            assertTrue(Tasks.await(doc2.get(), TIMEOUT_SECONDS, TimeUnit.SECONDS).exists());
        } finally {
            Tasks.await(doc1.delete(), TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Tasks.await(doc2.delete(), TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
    }
}
