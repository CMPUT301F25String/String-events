package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminProfileScreenTest {

    private static Intent makeIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        return new Intent(ctx, AdminProfileScreen.class)
                .putExtra("fromTest", true);
        // .putExtra("adminId", "test-admin");
    }

    @Before
    public void initFirebase() {
        Context ctx = ApplicationProvider.getApplicationContext();
        if (FirebaseApp.getApps(ctx).isEmpty()) {
            FirebaseApp.initializeApp(ctx);
        }
    }

    @Test
    public void backButton_doesNotCrash() {
        try (ActivityScenario<AdminProfileScreen> sc =
                     ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.btn_back)).perform(click());
        }
    }

    @Test
    public void logout_doesNotCrash() {
        try (ActivityScenario<AdminProfileScreen> sc =
                     ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.tv_logout)).perform(click());
        }
    }
}
