package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminEventManagementActivityTest {

    private static Intent makeIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        return new Intent(ctx, AdminEventManagementActivity.class)
                .putExtra("fromTest", true)
                ;
    }

    @Before
    public void initFirebase() {
        Context ctx = ApplicationProvider.getApplicationContext();
        if (FirebaseApp.getApps(ctx).isEmpty()) {
            FirebaseApp.initializeApp(ctx);
        }
    }

    @Rule
    public ActivityScenarioRule<AdminEventManagementActivity> rule =
            new ActivityScenarioRule<>(makeIntent());

    @Test
    public void showsBasicUi() {
        onView(withId(R.id.btn_back)).check(matches(isDisplayed()));
        onView(withId(R.id.event_management_text)).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));
    }
}

