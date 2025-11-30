package com.example.string_events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class UserAdapterFirebaseTest {

    private static final long TIMEOUT = 20L;

    @Test
    public void loadUsers_waitlist_populatesUserList_andCleansUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        long now = System.currentTimeMillis();

        String u1 = "helper_user_" + now + "_1";
        String u2 = "helper_user_" + now + "_2";
        String eventId = "helper_event_" + now;

        DocumentReference userDoc1 = db.collection("users").document("doc_" + u1);
        DocumentReference userDoc2 = db.collection("users").document("doc_" + u2);
        DocumentReference eventRef = db.collection("events").document(eventId);

        try {
            Map<String, Object> u1data = new HashMap<>();
            u1data.put("username", u1);
            u1data.put("name", "User One");
            u1data.put("email", u1 + "@example.com");

            Map<String, Object> u2data = new HashMap<>();
            u2data.put("username", u2);
            u2data.put("name", "User Two");
            u2data.put("email", u2 + "@example.com");

            Tasks.await(userDoc1.set(u1data), TIMEOUT, TimeUnit.SECONDS);
            Tasks.await(userDoc2.set(u2data), TIMEOUT, TimeUnit.SECONDS);

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("waitlist", Arrays.asList(u1, u2));
            Tasks.await(eventRef.set(eventData), TIMEOUT, TimeUnit.SECONDS);

            Context ctx = ApplicationProvider.getApplicationContext();
            ArrayList<UserItem> list = new ArrayList<>();
            UserAdapter adapter = new UserAdapter(ctx, list);
            UserAdapterHelper helper =
                    new UserAdapterHelper(adapter, list, UserItem.Status.WAITLIST);

            helper.loadUsers(eventId);

            boolean populated = false;
            for (int i = 0; i < 40; i++) {
                if (list.size() == 2) {
                    populated = true;
                    break;
                }
                Thread.sleep(500);
            }

            assertTrue("userList should have 2 items", populated);
            assertEquals(UserItem.Status.WAITLIST, list.get(0).getStatus());
            assertEquals(UserItem.Status.WAITLIST, list.get(1).getStatus());
        } finally {
            try {
                Tasks.await(eventRef.delete(), TIMEOUT, TimeUnit.SECONDS);
            } catch (Exception ignored) {}
            try {
                Tasks.await(userDoc1.delete(), TIMEOUT, TimeUnit.SECONDS);
            } catch (Exception ignored) {}
            try {
                Tasks.await(userDoc2.delete(), TIMEOUT, TimeUnit.SECONDS);
            } catch (Exception ignored) {}
        }
    }
}
