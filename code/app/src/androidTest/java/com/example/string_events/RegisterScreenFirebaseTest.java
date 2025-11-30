package com.example.string_events;

import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class RegisterScreenFirebaseTest {

    private static final long TIMEOUT = 20L;

    @Test
    public void register_createsUserDocument_andCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String email = "testuser_" + System.currentTimeMillis() + "@example.com";

        try (ActivityScenario<RegisterScreen> scenario =
                     ActivityScenario.launch(
                             new Intent(
                                     ApplicationProvider.getApplicationContext(),
                                     RegisterScreen.class
                             ))) {

            scenario.onActivity(activity -> {
                TextInputEditText etFullName = activity.findViewById(R.id.etFullName);
                TextInputEditText etEmail = activity.findViewById(R.id.etEmail);
                TextInputEditText etPassword = activity.findViewById(R.id.etPassword);
                TextInputEditText etPhone = activity.findViewById(R.id.etPhone);

                etFullName.setText("Test User");
                etEmail.setText(email);
                etPassword.setText("pass1234");
                etPhone.setText("1234567890");

                try {
                    Method m = RegisterScreen.class.getDeclaredMethod("register");
                    m.setAccessible(true);
                    m.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        boolean created = false;
        QuerySnapshot snap = null;

        for (int i = 0; i < 20; i++) {
            snap = Tasks.await(
                    db.collection("users")
                            .whereEqualTo("email", email)
                            .get(),
                    TIMEOUT,
                    TimeUnit.SECONDS
            );
            if (!snap.isEmpty()) {
                created = true;
                break;
            }
            Thread.sleep(500);
        }

        assertTrue("User document with this email should exist after register()", created);

        if (snap != null && !snap.isEmpty()) {
            for (DocumentSnapshot doc : snap.getDocuments()) {
                try {
                    Tasks.await(
                            doc.getReference().delete(),
                            TIMEOUT,
                            TimeUnit.SECONDS
                    );
                } catch (Exception ignored) {
                }
            }
        }
    }
}
