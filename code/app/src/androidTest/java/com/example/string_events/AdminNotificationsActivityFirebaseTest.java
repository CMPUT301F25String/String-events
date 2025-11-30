package com.example.string_events;

import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(AndroidJUnit4.class)
public class AdminNotificationsActivityFirebaseTest {

    private static final String NOTIF_COLLECTION = "notifications";

    @Test
    public void loadNotifications_loadsTestNotificationAndCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Create a test notification document
        String testUsername = "test_notif_user_" + System.currentTimeMillis();
        String testEventId = "test_event_id_" + System.currentTimeMillis();
        String testEventName = "Test Notification Event";
        boolean selectedStatus = true; // to trigger lottery notification branch

        Map<String, Object> notifData = new HashMap<>();
        notifData.put("username", testUsername);
        notifData.put("eventId", testEventId);
        notifData.put("eventName", testEventName);
        notifData.put("imageUrl", "");
        notifData.put("selectedStatus", selectedStatus);
        notifData.put("isMessage", false);
        notifData.put("messageText", null);

        CountDownLatch createLatch = new CountDownLatch(1);
        final String[] createdDocId = new String[1];

        db.collection(NOTIF_COLLECTION)
                .add(notifData)
                .addOnSuccessListener((DocumentReference ref) -> {
                    createdDocId[0] = ref.getId();
                    createLatch.countDown();
                })
                .addOnFailureListener(e -> createLatch.countDown());

        assertTrue("Failed to create test notification document",
                createLatch.await(5, TimeUnit.SECONDS));

        // 2. Launch AdminNotificationsActivity
        Intent intent = new Intent(
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
                        .getTargetContext(),
                AdminNotificationsActivity.class
        );
        ActivityScenario<AdminNotificationsActivity> scenario =
                ActivityScenario.launch(intent);

        // 3. Poll private notifList via reflection and check our test notification is present
        AtomicBoolean found = new AtomicBoolean(false);

        for (int i = 0; i < 8 && !found.get(); i++) {
            scenario.onActivity(activity -> {
                try {
                    Field notifListField = AdminNotificationsActivity.class
                            .getDeclaredField("notifList");
                    notifListField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    ArrayList<Notification> notifList =
                            (ArrayList<Notification>) notifListField.get(activity);

                    if (notifList != null) {
                        for (Notification n : notifList) {
                            // Depending on your Notification class, adjust getter if needed
                            if (testEventName.equals(n.getEventName())
                                    && testUsername.equals(n.getUsername())) {
                                found.set(true);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            if (found.get()) break;
            Thread.sleep(700);
        }

        assertTrue("Test notification was not loaded into notifList", found.get());

        // 4. Clean up: delete created notification document
        if (createdDocId[0] != null) {
            CountDownLatch deleteLatch = new CountDownLatch(1);
            db.collection(NOTIF_COLLECTION)
                    .document(createdDocId[0])
                    .delete()
                    .addOnSuccessListener(unused -> deleteLatch.countDown())
                    .addOnFailureListener(e -> deleteLatch.countDown());

            deleteLatch.await(5, TimeUnit.SECONDS);
        }

        scenario.close();
    }
}
