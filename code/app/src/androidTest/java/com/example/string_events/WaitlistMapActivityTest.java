package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for activity_waitlist_map.xml
 * Only modifies androidTest code; no manifest/app changes.
 * Strategy: launch app's launcher Activity and swap contentView to the layout under test.
 */
@RunWith(AndroidJUnit4.class)
public class WaitlistMapActivityTest {
    /** Launches launcher Activity and sets our layout as the content view. */
    public Intent newIntent() {
        // Using ApplicationProvider.getApplicationContext() is the standard way
        // to get a context in an instrumented test.
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(ctx, WaitlistMapActivity.class);

        intent.putExtra("eventId", "ca659820-0d64-4948-9469-b91707a26212");
        return intent;
    }
    @Rule
    public ActivityScenarioRule<WaitlistMapActivity> scenario = new ActivityScenarioRule<>(newIntent());

    @Test
    public void views_areDisplayed_test() {
//        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            onView(withId(R.id.map_view)).check(matches(isDisplayed()));
//        }
    }

    @Test
    public void buttons_areClickable_noCrash_test() {
//        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            onView(withId(R.id.btnBack)).check(matches(isClickable()));

            // Light interactions; success criterion: no crash
            onView(withId(R.id.btnBack)).perform(click());
//        }
    }
}
