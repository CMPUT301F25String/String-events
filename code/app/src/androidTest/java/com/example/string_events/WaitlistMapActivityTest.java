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
import androidx.test.ext.junit.runners.AndroidJUnit4;

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
    private ActivityScenario<? extends Activity> launchWithLayout() {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent launch = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        if (launch == null) launch = new Intent(Intent.ACTION_MAIN);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ActivityScenario<? extends Activity> scenario = ActivityScenario.launch(launch);
        scenario.onActivity(a -> {
            a.setContentView(R.layout.activity_waitlist_map);
            // Optional: drive MapView minimal lifecycle to avoid NPEs on some devices
            android.view.View mv = a.findViewById(R.id.map_view);
            if (mv instanceof com.google.android.gms.maps.MapView) {
                com.google.android.gms.maps.MapView mapView = (com.google.android.gms.maps.MapView) mv;
                mapView.onCreate(new Bundle());
                mapView.onResume();
            }
        });
        return scenario;
    }

    @Test
    public void views_areDisplayed_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            onView(withId(R.id.map_view)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_refresh)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_capture_location)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_save_join_location)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_save_my_location_precise)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void texts_areCorrect_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            onView(withId(R.id.btn_save_my_location_precise))
                    .check(matches(withText("Save My Location (precise)")));
        }
    }

    @Test
    public void buttons_areClickable_noCrash_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            onView(withId(R.id.btn_refresh)).check(matches(isClickable()));
            onView(withId(R.id.btn_capture_location)).check(matches(isClickable()));
            onView(withId(R.id.btn_save_join_location)).check(matches(isClickable()));
            onView(withId(R.id.btn_save_my_location_precise)).check(matches(isClickable()));

            // Light interactions; success criterion: no crash
            onView(withId(R.id.btn_refresh)).perform(click());
            onView(withId(R.id.btn_capture_location)).perform(click());
            onView(withId(R.id.btn_save_join_location)).perform(click());
            onView(withId(R.id.btn_save_my_location_precise)).perform(click());
        }
    }
}
