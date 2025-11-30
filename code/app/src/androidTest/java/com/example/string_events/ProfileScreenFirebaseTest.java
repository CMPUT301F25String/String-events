package com.example.string_events;

import static android.content.Context.MODE_PRIVATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class ProfileScreenFirebaseTest {

    private static final long TIMEOUT = 20L;

    @Test
    public void updateProfileImage_updatesUserDocument_andCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Context appContext = ApplicationProvider.getApplicationContext();

        String username = "profile_test_user_" + System.currentTimeMillis();
        String email = username + "@example.com";

        DocumentReference userRef = db.collection("users").document("doc_" + username);

        try {
            Map<String, Object> doc = new HashMap<>();
            doc.put("username", username);
            doc.put("email", email);
            doc.put("name", "Profile Test User");
            doc.put("profileimg", "");
            Tasks.await(userRef.set(doc), TIMEOUT, TimeUnit.SECONDS);

            SharedPreferences sp =
                    appContext.getSharedPreferences("userInfo", MODE_PRIVATE);
            sp.edit()
                    .putString("user", username)
                    .putString("role", "entrant")
                    .putString("name", "Profile Test User")
                    .putString("email", email)
                    .apply();

            try (ActivityScenario<ProfileScreen> scenario =
                         ActivityScenario.launch(
                                 new Intent(appContext, ProfileScreen.class))) {

                scenario.onActivity(activity -> {
                    try {
                        Method m = ProfileScreen.class
                                .getDeclaredMethod("updateProfileImageInFirestore", String.class);
                        m.setAccessible(true);
                        m.invoke(activity, "https://example.com/profile_test.png");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            boolean updated = false;
            String url = null;
            for (int i = 0; i < 20; i++) {
                DocumentSnapshot snap =
                        Tasks.await(userRef.get(), TIMEOUT, TimeUnit.SECONDS);
                url = snap.getString("profileimg");
                if (url != null && !url.isEmpty()) {
                    updated = true;
                    break;
                }
                Thread.sleep(500);
            }

            assertTrue("profileimg should be updated", updated);
            assertEquals("https://example.com/profile_test.png", url);
        } finally {
            try {
                Tasks.await(userRef.delete(), TIMEOUT, TimeUnit.SECONDS);
            } catch (Exception ignored) {
            }
        }
    }
}
