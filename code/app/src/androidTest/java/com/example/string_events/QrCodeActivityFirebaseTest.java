package com.example.string_events;

import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class QrCodeActivityFirebaseTest {

    private static final long TIMEOUT = 30L;

    @Test
    public void qrCodeActivity_generatesQr_updatesFirestore_andCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        String eventId = "test_qr_event_" + System.currentTimeMillis();
        DocumentReference eventRef = db.collection("events").document(eventId);
        StorageReference qrRef = storage.getReference().child("qr_code/" + eventId + ".png");

        try {
            try {
                Tasks.await(eventRef.delete(), 5, TimeUnit.SECONDS);
            } catch (Exception ignored) {
            }
            try {
                Tasks.await(qrRef.delete(), 5, TimeUnit.SECONDS);
            } catch (Exception ignored) {
            }

            Map<String, Object> data = new HashMap<>();
            data.put("title", "QR Test Event");
            data.put("creator", "qr_test_creator");
            Tasks.await(eventRef.set(data), TIMEOUT, TimeUnit.SECONDS);

            Intent intent = new Intent(
                    ApplicationProvider.getApplicationContext(),
                    QrCodeActivity.class
            );
            intent.putExtra(QrCodeActivity.EXTRA_EVENT_ID, eventId);

            boolean updated = false;
            String qrUrl = null;

            try (ActivityScenario<QrCodeActivity> scenario =
                         ActivityScenario.launch(intent)) {

                for (int i = 0; i < 40; i++) {
                    DocumentSnapshot snap =
                            Tasks.await(eventRef.get(), TIMEOUT, TimeUnit.SECONDS);
                    qrUrl = snap.getString("qrCodeUrl");
                    if (qrUrl != null && !qrUrl.isEmpty()) {
                        updated = true;
                        break;
                    }
                    Thread.sleep(500);
                }
            }

            assertTrue("qrCodeUrl should be set on event document", updated);
        } finally {
            try {
                Tasks.await(eventRef.delete(), TIMEOUT, TimeUnit.SECONDS);
            } catch (Exception ignored) {
            }
            try {
                Tasks.await(qrRef.delete(), TIMEOUT, TimeUnit.SECONDS);
            } catch (Exception ignored) {
            }
        }
    }
}
