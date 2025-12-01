package com.example.string_events;

import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class AdminImageManagementActivityInstrumentedTest {

    @Test
    public void loadImages_populatesEventImagesWithImageUrl() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String eventId = "instrumented_image_" + System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("title", "Image Test");
        data.put("imageUrl", "https://example.com/test.png");

        Tasks.await(
                db.collection("events").document(eventId).set(data),
                10, TimeUnit.SECONDS
        );

        ActivityScenario<AdminImageManagementActivity> scenario = null;

        try {
            scenario = ActivityScenario.launch(AdminImageManagementActivity.class);

            ActivityScenario<AdminImageManagementActivity> finalScenario = scenario;
            finalScenario.onActivity(activity -> {
                try {
                    java.lang.reflect.Method m =
                            AdminImageManagementActivity.class.getDeclaredMethod("loadImages");
                    m.setAccessible(true);
                    m.invoke(activity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Thread.sleep(2000);

            scenario.onActivity(activity -> {
                try {
                    Field f = AdminImageManagementActivity.class.getDeclaredField("eventImages");
                    f.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    ArrayList<AdminImageAdapter.EventImage> list =
                            (ArrayList<AdminImageAdapter.EventImage>) f.get(activity);

                    boolean found = false;
                    if (list != null) {
                        for (AdminImageAdapter.EventImage img : list) {
                            if (eventId.equals(img.id)) {
                                found = true;
                                break;
                            }
                        }
                    }
                    assertTrue(found);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } finally {
            if (scenario != null) {
                scenario.close();
            }
            try {
                Tasks.await(
                        db.collection("events").document(eventId).delete(),
                        5, TimeUnit.SECONDS
                );
            } catch (Exception ignore) {
            }
        }
    }
}
