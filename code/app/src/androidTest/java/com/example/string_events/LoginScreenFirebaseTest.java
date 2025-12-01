package com.example.string_events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.widget.CheckBox;
import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.firestore.DocumentReference;
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
public class LoginScreenFirebaseTest {

    private static final String USERS_COLLECTION = "users";

    @Test
    public void signIn_logsInUserAndCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        String username = "test_login_user_" + System.currentTimeMillis();
        String password = "TestPassword123";
        String name = "Test User";
        String email = username + "@example.com";

        // fake test user document
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("password", password);
        userData.put("name", name);
        userData.put("email", email);

        CountDownLatch createLatch = new CountDownLatch(1);
        final String[] docIdHolder = new String[1];
        db.collection(USERS_COLLECTION)
                .add(userData)
                .addOnSuccessListener((DocumentReference ref) -> {
                    docIdHolder[0] = ref.getId();
                    createLatch.countDown();
                })
                .addOnFailureListener(e -> createLatch.countDown());
        assertTrue("Failed to create test user document",
                createLatch.await(10, TimeUnit.SECONDS));

        // launch LoginScreen
        Intent intent = new Intent(ctx, LoginScreen.class);
        ActivityScenario<LoginScreen> scenario = ActivityScenario.launch(intent);

        try {
            // fill in username + password and call signIn()
            scenario.onActivity(activity -> {
                try {
                    EditText etEmail = activity.findViewById(R.id.etEmail);
                    EditText etPassword = activity.findViewById(R.id.etPassword);
                    CheckBox chkRemember = activity.findViewById(R.id.chkRemember);

                    if (etEmail != null) etEmail.setText(username);
                    if (etPassword != null) etPassword.setText(password);
                    if (chkRemember != null) chkRemember.setChecked(true);

                    // call private signIn()
                    Method signInMethod = LoginScreen.class.getDeclaredMethod("signIn");
                    signInMethod.setAccessible(true);
                    signInMethod.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            SharedPreferences sp = ctx.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            AtomicBoolean loggedIn = new AtomicBoolean(false);

            String storedUser;
            String storedRole;

            for (int i = 0; i < 20 && !loggedIn.get(); i++) {
                storedUser = sp.getString("user", null);
                storedRole = sp.getString("role", null);
                if (username.equals(storedUser) && "entrant".equals(storedRole)) {
                    loggedIn.set(true);
                    break;
                }
                Thread.sleep(500);
            }

            assertTrue("LoginScreen did not store user in SharedPreferences", loggedIn.get());
            assertEquals(username, sp.getString("username", null));

        } finally {
            // clean up
            if (docIdHolder[0] != null) {
                CountDownLatch deleteLatch = new CountDownLatch(1);
                db.collection(USERS_COLLECTION)
                        .document(docIdHolder[0])
                        .delete()
                        .addOnSuccessListener(unused -> deleteLatch.countDown())
                        .addOnFailureListener(e -> deleteLatch.countDown());
                deleteLatch.await(10, TimeUnit.SECONDS);
            }

            scenario.close();
        }
    }
}
