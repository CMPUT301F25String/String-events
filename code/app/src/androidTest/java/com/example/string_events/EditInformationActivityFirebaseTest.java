package com.example.string_events;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Intent;
import android.widget.ImageButton;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(AndroidJUnit4.class)
public class EditInformationActivityFirebaseTest {

    private static final String USERS_COLLECTION = "users";

    @Test
    public void doneButton_updatesUser_and_alwaysCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String username = "edit_info_user_" + System.currentTimeMillis();
        String email = username + "@example.com";

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("name", "Original Name");
        userData.put("email", email);
        userData.put("password", "originalPassword");

        CountDownLatch createLatch = new CountDownLatch(1);
        final String[] docIdHolder = new String[1];
        AtomicBoolean created = new AtomicBoolean(false);

        db.collection(USERS_COLLECTION)
                .add(userData)
                .addOnSuccessListener(ref -> {
                    docIdHolder[0] = ref.getId();
                    created.set(true);
                    createLatch.countDown();
                })
                .addOnFailureListener(e -> createLatch.countDown());

        createLatch.await(10, TimeUnit.SECONDS);
        if (!created.get()) {
            fail("Failed to create test user document for EditInformationActivity");
        }

        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                EditInformationActivity.class
        );
        intent.putExtra("user", username);

        ActivityScenario<EditInformationActivity> scenario = null;

        try {
            scenario = ActivityScenario.launch(intent);

            String newName = "Updated Name " + System.currentTimeMillis();

            ActivityScenario<EditInformationActivity> finalScenario = scenario;
            finalScenario.onActivity(activity -> {
                TextInputEditText etName = activity.findViewById(R.id.et_new_name);
                MaterialButton btnDone = activity.findViewById(R.id.btn_done);

                if (etName != null) {
                    etName.setText(newName);
                }
                if (btnDone != null) {
                    btnDone.performClick();
                }
            });

            AtomicBoolean updated = new AtomicBoolean(false);

            for (int i = 0; i < 10 && !updated.get(); i++) {
                CountDownLatch checkLatch = new CountDownLatch(1);
                db.collection(USERS_COLLECTION)
                        .document(docIdHolder[0])
                        .get()
                        .addOnSuccessListener((DocumentSnapshot snap) -> {
                            if (snap.exists()) {
                                String storedName = snap.getString("name");
                                if (newName.equals(storedName)) {
                                    updated.set(true);
                                }
                            }
                            checkLatch.countDown();
                        })
                        .addOnFailureListener(e -> checkLatch.countDown());

                checkLatch.await(5, TimeUnit.SECONDS);
                if (!updated.get()) {
                    Thread.sleep(700);
                }
            }

            assertTrue("User name was not updated in Firestore", updated.get());

        } finally {
            // Always delete the test user document and verify
            CountDownLatch deleteLatch = new CountDownLatch(1);
            AtomicBoolean deleteOk = new AtomicBoolean(false);

            db.collection(USERS_COLLECTION)
                    .document(docIdHolder[0])
                    .delete()
                    .addOnSuccessListener(unused -> {
                        deleteOk.set(true);
                        deleteLatch.countDown();
                    })
                    .addOnFailureListener(e -> deleteLatch.countDown());

            deleteLatch.await(10, TimeUnit.SECONDS);

            if (!deleteOk.get()) {
                fail("Failed to delete user document after doneButton test: " + docIdHolder[0]);
            }

            CountDownLatch verifyLatch = new CountDownLatch(1);
            AtomicBoolean stillExists = new AtomicBoolean(false);

            db.collection(USERS_COLLECTION)
                    .document(docIdHolder[0])
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
                fail("User document still exists after delete (doneButton test): " + docIdHolder[0]);
            }

            if (scenario != null) {
                scenario.close();
            }
        }
    }

    @Test
    public void deleteProfileButton_deletesUser_and_alwaysCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String username = "delete_info_user_" + System.currentTimeMillis();
        String email = username + "@example.com";

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("name", "Delete Me");
        userData.put("email", email);
        userData.put("password", "toBeDeleted");

        CountDownLatch createLatch = new CountDownLatch(1);
        final String[] docIdHolder = new String[1];
        AtomicBoolean created = new AtomicBoolean(false);

        db.collection(USERS_COLLECTION)
                .add(userData)
                .addOnSuccessListener(ref -> {
                    docIdHolder[0] = ref.getId();
                    created.set(true);
                    createLatch.countDown();
                })
                .addOnFailureListener(e -> createLatch.countDown());

        createLatch.await(10, TimeUnit.SECONDS);
        if (!created.get()) {
            fail("Failed to create user document for deleteProfileButton test");
        }

        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                EditInformationActivity.class
        );
        intent.putExtra("user", username);

        ActivityScenario<EditInformationActivity> scenario = null;

        try {
            scenario = ActivityScenario.launch(intent);

            ActivityScenario<EditInformationActivity> finalScenario = scenario;
            finalScenario.onActivity(activity -> {
                ImageButton deleteButton =
                        activity.findViewById(R.id.delete_profile_button);
                if (deleteButton != null) {
                    deleteButton.performClick();
                }
            });

            AtomicBoolean deleted = new AtomicBoolean(false);

            for (int i = 0; i < 10 && !deleted.get(); i++) {
                CountDownLatch checkLatch = new CountDownLatch(1);
                db.collection(USERS_COLLECTION)
                        .document(docIdHolder[0])
                        .get()
                        .addOnSuccessListener((DocumentSnapshot snap) -> {
                            if (!snap.exists()) {
                                deleted.set(true);
                            }
                            checkLatch.countDown();
                        })
                        .addOnFailureListener(e -> checkLatch.countDown());

                checkLatch.await(5, TimeUnit.SECONDS);
                if (!deleted.get()) {
                    Thread.sleep(700);
                }
            }

            assertTrue("User document was not deleted by EditInformationActivity", deleted.get());

        } finally {
            // Final safety: if doc still exists for any reason, force delete and assert
            CountDownLatch checkLatch = new CountDownLatch(1);
            AtomicBoolean exists = new AtomicBoolean(false);
            db.collection(USERS_COLLECTION)
                    .document(docIdHolder[0])
                    .get()
                    .addOnSuccessListener(snap -> {
                        exists.set(snap.exists());
                        checkLatch.countDown();
                    })
                    .addOnFailureListener(e -> checkLatch.countDown());
            checkLatch.await(10, TimeUnit.SECONDS);

            if (exists.get()) {
                CountDownLatch forceDeleteLatch = new CountDownLatch(1);
                AtomicBoolean forceOk = new AtomicBoolean(false);
                db.collection(USERS_COLLECTION)
                        .document(docIdHolder[0])
                        .delete()
                        .addOnSuccessListener(unused -> {
                            forceOk.set(true);
                            forceDeleteLatch.countDown();
                        })
                        .addOnFailureListener(e -> forceDeleteLatch.countDown());
                forceDeleteLatch.await(10, TimeUnit.SECONDS);

                if (!forceOk.get()) {
                    fail("Failed to force delete user document: " + docIdHolder[0]);
                }

                CountDownLatch verifyLatch = new CountDownLatch(1);
                AtomicBoolean stillExists = new AtomicBoolean(false);
                db.collection(USERS_COLLECTION)
                        .document(docIdHolder[0])
                        .get()
                        .addOnSuccessListener(snap2 -> {
                            if (snap2.exists()) {
                                stillExists.set(true);
                            }
                            verifyLatch.countDown();
                        })
                        .addOnFailureListener(e -> verifyLatch.countDown());
                verifyLatch.await(10, TimeUnit.SECONDS);

                if (stillExists.get()) {
                    fail("User document still exists after forced delete: " + docIdHolder[0]);
                }
            }

            if (scenario != null) {
                scenario.close();
            }
        }
    }
}
