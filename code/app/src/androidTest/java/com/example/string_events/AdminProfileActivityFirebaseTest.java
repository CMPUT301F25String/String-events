package com.example.string_events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(AndroidJUnit4.class)
public class AdminProfileActivityFirebaseTest {

    private static final String ADMINS_COLLECTION = "admins";

    @Test
    public void updateProfileImageInFirestore_updatesDocAndCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Create a test admin document
        String testDocId = "test-admin-" + System.currentTimeMillis();
        String testUsername = "test_admin_" + System.currentTimeMillis();
        String originalUrl = "https://example.com/original.png";
        String newUrl = "https://example.com/new-image.png";

        Map<String, Object> data = new HashMap<>();
        data.put("username", testUsername);
        data.put("email", "test_admin@example.com");
        data.put("password", "secret");
        data.put("profileimg", originalUrl);

        CountDownLatch createLatch = new CountDownLatch(1);
        db.collection(ADMINS_COLLECTION)
                .document(testDocId)
                .set(data)
                .addOnSuccessListener(unused -> createLatch.countDown())
                .addOnFailureListener(e -> createLatch.countDown());

        assertTrue("Failed to create test admin document",
                createLatch.await(5, TimeUnit.SECONDS));

        // 2. Launch AdminProfileActivity (intent itself is not critical here)
        Intent intent = new Intent(
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
                        .getTargetContext(),
                AdminProfileActivity.class
        );
        ActivityScenario<AdminProfileActivity> scenario =
                ActivityScenario.launch(intent);

        // 3. Use reflection to set docId and call private updateProfileImageInFirestore
        scenario.onActivity(activity -> {
            try {
                // Inject Firestore instance (optional, but keeps it consistent)
                Field firestoreField =
                        AdminProfileActivity.class.getDeclaredField("firestore");
                firestoreField.setAccessible(true);
                firestoreField.set(activity, db);

                // Inject our test document id
                Field docIdField =
                        AdminProfileActivity.class.getDeclaredField("docId");
                docIdField.setAccessible(true);
                docIdField.set(activity, testDocId);

                // Call private method updateProfileImageInFirestore(String)
                Method m = AdminProfileActivity.class
                        .getDeclaredMethod("updateProfileImageInFirestore", String.class);
                m.setAccessible(true);
                m.invoke(activity, newUrl);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // 4. Poll Firestore to verify that profileimg has been updated
        AtomicBoolean updated = new AtomicBoolean(false);

        for (int i = 0; i < 5 && !updated.get(); i++) {
            CountDownLatch checkLatch = new CountDownLatch(1);
            db.collection(ADMINS_COLLECTION)
                    .document(testDocId)
                    .get()
                    .addOnSuccessListener((DocumentSnapshot snap) -> {
                        if (snap.exists()) {
                            String url = snap.getString("profileimg");
                            if (newUrl.equals(url)) {
                                updated.set(true);
                            }
                        }
                        checkLatch.countDown();
                    })
                    .addOnFailureListener(e -> checkLatch.countDown());

            checkLatch.await(3, TimeUnit.SECONDS);
            if (!updated.get()) {
                // small delay before next poll
                Thread.sleep(500);
            }
        }

        assertTrue("profileimg was not updated to expected value", updated.get());

        // 5. Clean up: delete test admin document
        CountDownLatch deleteLatch = new CountDownLatch(1);
        db.collection(ADMINS_COLLECTION)
                .document(testDocId)
                .delete()
                .addOnSuccessListener(unused -> deleteLatch.countDown())
                .addOnFailureListener(e -> deleteLatch.countDown());

        deleteLatch.await(5, TimeUnit.SECONDS);

        scenario.close();
    }
}
