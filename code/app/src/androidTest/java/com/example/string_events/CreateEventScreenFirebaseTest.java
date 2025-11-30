package com.example.string_events;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Intent;
import android.net.Uri;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(AndroidJUnit4.class)
public class CreateEventScreenFirebaseTest {

    private static final String EVENTS_COLLECTION = "events";

    @Test
    public void saveEventDetailsToDatabase_createsEvent_and_alwaysCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String eventId = "test-event-" + System.currentTimeMillis();
        String creator = "tester_" + System.currentTimeMillis();
        String title = "Firebase Test Event";
        Uri photo = null;
        String description = "Event created by instrumentation test";
        ArrayList<String> tags = new ArrayList<>();
        tags.add("test_tag");

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime start = now.plusDays(1);
        ZonedDateTime end = start.plusHours(2);
        ZonedDateTime regStart = now;
        ZonedDateTime regEnd = now.plusHours(6);

        int maxAttendees = 50;
        int waitlistLimit = 10;
        boolean geoReq = false;
        boolean visible = true;

        Event event = new Event(
                eventId,
                creator,
                title,
                photo,
                description,
                tags,
                start,
                end,
                "Test Location",
                regStart,
                regEnd,
                maxAttendees,
                waitlistLimit,
                geoReq,
                visible
        );

        // Ensure no stale document
        CountDownLatch preDeleteLatch = new CountDownLatch(1);
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .delete()
                .addOnSuccessListener(unused -> preDeleteLatch.countDown())
                .addOnFailureListener(e -> preDeleteLatch.countDown());
        preDeleteLatch.await(5, TimeUnit.SECONDS);

        String imageUrl = "https://example.com/test-image-" + System.currentTimeMillis() + ".png";

        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                CreateEventScreen.class
        );

        ActivityScenario<CreateEventScreen> scenario = null;

        try {
            scenario = ActivityScenario.launch(intent);

            // Invoke the private method saveEventDetailsToDatabase(event, imageUrl)
            ActivityScenario<CreateEventScreen> finalScenario = scenario;
            finalScenario.onActivity(activity -> {
                try {
                    Field dbField = CreateEventScreen.class.getDeclaredField("db");
                    dbField.setAccessible(true);
                    dbField.set(activity, db);

                    Method saveMethod = CreateEventScreen.class
                            .getDeclaredMethod("saveEventDetailsToDatabase", Event.class, String.class);
                    saveMethod.setAccessible(true);
                    saveMethod.invoke(activity, event, imageUrl);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            AtomicBoolean exists = new AtomicBoolean(false);
            AtomicBoolean imageMatches = new AtomicBoolean(false);

            for (int i = 0; i < 8 && !exists.get(); i++) {
                CountDownLatch checkLatch = new CountDownLatch(1);
                db.collection(EVENTS_COLLECTION)
                        .document(eventId)
                        .get()
                        .addOnSuccessListener((DocumentSnapshot snap) -> {
                            if (snap.exists()) {
                                exists.set(true);
                                String storedUrl = snap.getString("imageUrl");
                                if (imageUrl.equals(storedUrl)) {
                                    imageMatches.set(true);
                                }
                            }
                            checkLatch.countDown();
                        })
                        .addOnFailureListener(e -> checkLatch.countDown());

                checkLatch.await(5, TimeUnit.SECONDS);
                if (!exists.get()) {
                    Thread.sleep(700);
                }
            }

            assertTrue("Event document was not created in Firestore", exists.get());
            assertTrue("imageUrl field on event document did not match test URL", imageMatches.get());

        } finally {
            // Mandatory cleanup: delete the event document and assert it is gone
            CountDownLatch deleteLatch = new CountDownLatch(1);
            AtomicBoolean deleteOk = new AtomicBoolean(false);

            db.collection(EVENTS_COLLECTION)
                    .document(eventId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        deleteOk.set(true);
                        deleteLatch.countDown();
                    })
                    .addOnFailureListener(e -> deleteLatch.countDown());

            deleteLatch.await(10, TimeUnit.SECONDS);

            if (!deleteOk.get()) {
                fail("Failed to delete test event document: " + eventId);
            }

            // Double-check that document no longer exists
            CountDownLatch verifyLatch = new CountDownLatch(1);
            AtomicBoolean stillExists = new AtomicBoolean(false);
            db.collection(EVENTS_COLLECTION)
                    .document(eventId)
                    .get()
                    .addOnSuccessListener(snap -> {
                        if (snap.exists()) {
                            stillExists.set(true);
                        }
                        verifyLatch.countDown();
                    })
                    .addOnFailureListener(e -> verifyLatch.countDown());
            verifyLatch.await(10, TimeUnit.SECONDS);

            if (stillExists.get()) {
                fail("Event document still exists after delete: " + eventId);
            }

            if (scenario != null) {
                scenario.close();
            }
        }
    }

    @Test
    public void generateAndUploadQrCode_setsQrCodeUrl_and_alwaysCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String eventId = "test-qr-event-" + System.currentTimeMillis();
        String creator = "qr_tester_" + System.currentTimeMillis();
        String title = "QR Test Event";

        ArrayList<String> tags = new ArrayList<>();
        tags.add("qr_test");

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime start = now.plusDays(2);
        ZonedDateTime end = start.plusHours(3);
        ZonedDateTime regStart = now;
        ZonedDateTime regEnd = now.plusDays(1);

        Event event = new Event(
                eventId,
                creator,
                title,
                null,
                "QR event description",
                tags,
                start,
                end,
                "QR Test Location",
                regStart,
                regEnd,
                20,
                5,
                false,
                true
        );

        Map<String, Object> seed = new HashMap<>();
        seed.put("creator", creator);
        seed.put("title", title);

        CountDownLatch seedLatch = new CountDownLatch(1);
        AtomicBoolean seedOk = new AtomicBoolean(false);
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .set(seed)
                .addOnSuccessListener(unused -> {
                    seedOk.set(true);
                    seedLatch.countDown();
                })
                .addOnFailureListener(e -> seedLatch.countDown());
        seedLatch.await(10, TimeUnit.SECONDS);
        if (!seedOk.get()) {
            fail("Failed to create seed event document for QR test");
        }

        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                CreateEventScreen.class
        );

        ActivityScenario<CreateEventScreen> scenario = null;

        try {
            scenario = ActivityScenario.launch(intent);

            ActivityScenario<CreateEventScreen> finalScenario = scenario;
            finalScenario.onActivity(activity -> {
                try {
                    Field dbField = CreateEventScreen.class.getDeclaredField("db");
                    dbField.setAccessible(true);
                    dbField.set(activity, db);

                    Method qrMethod = CreateEventScreen.class
                            .getDeclaredMethod("generateAndUploadQrCode", Event.class);
                    qrMethod.setAccessible(true);
                    qrMethod.invoke(activity, event);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            AtomicBoolean qrSet = new AtomicBoolean(false);
            final String[] qrUrlHolder = new String[1];

            for (int i = 0; i < 12 && !qrSet.get(); i++) {
                CountDownLatch checkLatch = new CountDownLatch(1);
                db.collection(EVENTS_COLLECTION)
                        .document(eventId)
                        .get()
                        .addOnSuccessListener((DocumentSnapshot snap) -> {
                            if (snap.exists()) {
                                String qr = snap.getString("qrCodeUrl");
                                if (qr != null && !qr.isEmpty()) {
                                    qrSet.set(true);
                                    qrUrlHolder[0] = qr;
                                }
                            }
                            checkLatch.countDown();
                        })
                        .addOnFailureListener(e -> checkLatch.countDown());

                checkLatch.await(5, TimeUnit.SECONDS);
                if (!qrSet.get()) {
                    Thread.sleep(1000);
                }
            }

            assertTrue("qrCodeUrl was not set on event document", qrSet.get());
            assertNotNull("qrCodeUrl string should not be null", qrUrlHolder[0]);

        } finally {
            // Always delete Firestore document
            CountDownLatch deleteLatch = new CountDownLatch(1);
            AtomicBoolean deleteOk = new AtomicBoolean(false);
            db.collection(EVENTS_COLLECTION)
                    .document(eventId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        deleteOk.set(true);
                        deleteLatch.countDown();
                    })
                    .addOnFailureListener(e -> deleteLatch.countDown());
            deleteLatch.await(10, TimeUnit.SECONDS);

            if (!deleteOk.get()) {
                fail("Failed to delete QR test event document: " + eventId);
            }

            // Verify Firestore doc is gone
            CountDownLatch verifyLatch = new CountDownLatch(1);
            AtomicBoolean stillExists = new AtomicBoolean(false);
            db.collection(EVENTS_COLLECTION)
                    .document(eventId)
                    .get()
                    .addOnSuccessListener(snap -> {
                        if (snap.exists()) {
                            stillExists.set(true);
                        }
                        verifyLatch.countDown();
                    })
                    .addOnFailureListener(e -> verifyLatch.countDown());
            verifyLatch.await(10, TimeUnit.SECONDS);
            if (stillExists.get()) {
                fail("QR event document still exists after delete: " + eventId);
            }

            // Always delete QR image in Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference qrRef = storage.getReference().child("qr_code/" + eventId + ".png");
            CountDownLatch qrDeleteLatch = new CountDownLatch(1);
            AtomicBoolean qrDeleted = new AtomicBoolean(false);
            qrRef.delete()
                    .addOnSuccessListener(unused -> {
                        qrDeleted.set(true);
                        qrDeleteLatch.countDown();
                    })
                    .addOnFailureListener(e -> qrDeleteLatch.countDown());
            qrDeleteLatch.await(10, TimeUnit.SECONDS);

            if (!qrDeleted.get()) {
                fail("Failed to delete QR image from Storage for event: " + eventId);
            }

            if (scenario != null) {
                scenario.close();
            }
        }
    }
}
