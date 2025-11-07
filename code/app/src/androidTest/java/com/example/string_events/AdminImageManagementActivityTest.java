package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminImageManagementActivityTest {

    private static Intent makeIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        return new Intent(ctx, AdminImageManagementActivity.class)
                .putExtra("fromTest", true)
                .putExtra("adminId", "test-admin")
                .putExtra("eventId", "test-event")
                .putExtra("imageFolder", "test-folder");
    }

    @Before
    public void initFirebase() {
        Context ctx = ApplicationProvider.getApplicationContext();
        if (FirebaseApp.getApps(ctx).isEmpty()) {
            FirebaseApp.initializeApp(ctx);
        }
    }

    @Test
    public void showsCoreWidgets() {
        try (ActivityScenario<AdminImageManagementActivity> sc =
                     ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.btn_back)).check(matches(isDisplayed()));
            onView(withId(R.id.image_management_text)).check(matches(isDisplayed()));
            onView(withId(R.id.recycler_images)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_delete)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void back_and_delete_doNotCrash() {
        try (ActivityScenario<AdminImageManagementActivity> sc =
                     ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.btn_back)).perform(click());
        }

        try (ActivityScenario<AdminImageManagementActivity> sc =
                     ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.btn_delete)).perform(click());
        }
    }
}
