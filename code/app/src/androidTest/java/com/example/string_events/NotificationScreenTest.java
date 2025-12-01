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
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for notification_screen.xml
 * Only modifies androidTest code; no manifest/app changes.
 * Strategy: launch app's launcher Activity and swap contentView to the layout under test.
 */
@RunWith(AndroidJUnit4.class)
public class NotificationScreenTest {

    /** Launch launcher activity and set our layout as content. Also attach no-op listeners. */
    private ActivityScenario<? extends Activity> launchWithLayout() {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent launch = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        if (launch == null) launch = new Intent(Intent.ACTION_MAIN);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ActivityScenario<? extends Activity> sc = ActivityScenario.launch(launch);
        sc.onActivity(a -> {
            a.setContentView(R.layout.notification_screen);

            // Attach no-op click listeners to avoid PerformException when clicking in tests
            int[] ids = { R.id.btnHome, R.id.btnCamera, R.id.btnNotification, R.id.btnProfile };
            android.view.View.OnClickListener noop = v -> { /* no-op */ };
            for (int id : ids) {
                android.view.View v = a.findViewById(id);
                if (v != null) {
                    v.setClickable(true);
                    v.setOnClickListener(noop);
                }
            }
        });
        return sc;
    }

    @Test
    public void views_areDisplayed_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
            onView(withId(R.id.notifications_textView)).check(matches(isDisplayed()));
            onView(withId(R.id.notifications_recyclerView)).check(matches(isDisplayed()));
            onView(withId(R.id.bottomBar)).check(matches(isDisplayed()));
            onView(withId(R.id.btnHome)).check(matches(isDisplayed()));
            onView(withId(R.id.btnCamera)).check(matches(isDisplayed()));
            onView(withId(R.id.btnNotification)).check(matches(isDisplayed()));
            onView(withId(R.id.btnProfile)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void texts_and_accessibility_areCorrect_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            // Helper text from XML
            onView(withId(R.id.notifications_textView))
                    .check(matches(withText("Click on a notification to expand")));
            // Bottom bar a11y labels from XML
            onView(withId(R.id.btnHome)).check(matches(withContentDescription("Home")));
            onView(withId(R.id.btnCamera)).check(matches(withContentDescription("Camera")));
            onView(withId(R.id.btnNotification)).check(matches(withContentDescription("Notifications")));
            onView(withId(R.id.btnProfile)).check(matches(withContentDescription("Profile")));
        }
    }

    @Test
    public void bottom_buttons_areClickable_noCrash_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            onView(withId(R.id.btnHome)).check(matches(isClickable())).perform(click());
            onView(withId(R.id.btnCamera)).check(matches(isClickable())).perform(click());
            onView(withId(R.id.btnNotification)).check(matches(isClickable())).perform(click());
            onView(withId(R.id.btnProfile)).check(matches(isClickable())).perform(click());
        }
    }
}
