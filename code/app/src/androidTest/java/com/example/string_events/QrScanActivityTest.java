package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for activity_scan_qr.xml
 * Only modifies androidTest code (no manifest/app code changes).
 * Strategy: launch app's launcher Activity and swap contentView to the layout under test.
 */
@RunWith(AndroidJUnit4.class)
public class QrScanActivityTest {

    /** Launch launcher activity and set our layout as content. */
    @Rule
    public ActivityScenarioRule<QrScanActivity> scenario = new ActivityScenarioRule<>(QrScanActivity.class);

    @Test
    public void views_areDisplayed_test() {
//        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            // top bar
            onView(withId(R.id.topBar)).check(matches(isDisplayed()));
            onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
            onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.btnFlash)).check(matches(isDisplayed()));

            // content
            onView(withId(R.id.contentContainer)).check(matches(isDisplayed()));
            onView(withId(R.id.qr_frame)).check(matches(isDisplayed()));
            onView(withId(R.id.tvInstruction)).check(matches(isDisplayed()));

            // bottom bar
            onView(withId(R.id.bottomBar)).check(matches(isDisplayed()));
            onView(withId(R.id.btnHome)).check(matches(isDisplayed()));
            onView(withId(R.id.btnCamera)).check(matches(isDisplayed()));
            onView(withId(R.id.btnNotification)).check(matches(isDisplayed()));
            onView(withId(R.id.btnProfile)).check(matches(isDisplayed()));
//        }
    }

    @Test
    public void texts_and_accessibility_areCorrect_test() {
//        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            // Title & instruction text from XML
            onView(withId(R.id.tvTitle)).check(matches(withText("QR Scanner")));
            onView(withId(R.id.tvInstruction)).check(matches(withText("Scan the event QR code to join")));

            // Content descriptions from XML
            onView(withId(R.id.btnBack)).check(matches(withContentDescription("Back")));
            onView(withId(R.id.btnFlash)).check(matches(withContentDescription("Flash")));
            onView(withId(R.id.btnHome)).check(matches(withContentDescription("Home")));
            onView(withId(R.id.btnCamera)).check(matches(withContentDescription("Camera")));
            onView(withId(R.id.btnNotification)).check(matches(withContentDescription("Notifications")));
            onView(withId(R.id.btnProfile)).check(matches(withContentDescription("Profile")));
//        }
    }

    @Test
    public void buttons_areClickable_noCrash_test() {
//        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            onView(withId(R.id.btnFlash)).check(matches(isClickable()));
            onView(withId(R.id.btnHome)).check(matches(isClickable()));
            onView(withId(R.id.btnCamera)).check(matches(isClickable()));
            onView(withId(R.id.btnNotification)).check(matches(isClickable()));
            onView(withId(R.id.btnProfile)).check(matches(isClickable()));

            // Do light interactions; success criterion: no crash.
            onView(withId(R.id.btnFlash)).perform(click());
            onView(withId(R.id.btnHome)).perform(click());
            onView(withId(R.id.btnCamera)).perform(click());
            onView(withId(R.id.btnNotification)).perform(click());
            onView(withId(R.id.btnProfile)).perform(click());
//        }
    }
}
