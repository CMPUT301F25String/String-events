package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for admin_item_notification.xml
 * NOTE: Layout uses tools:text in XML, so we only check visibility.
 * This test touches test code only (no manifest or app code changes).
 */
@RunWith(AndroidJUnit4.class)
public class AdminItemNotificationLayoutTest {

    /** Launches the app's launcher Activity and swaps content to the layout under test. */
    private ActivityScenario<? extends Activity> launchWithLayout() {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent launchIntent = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        if (launchIntent == null) launchIntent = new Intent(Intent.ACTION_MAIN);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ActivityScenario<? extends Activity> scenario = ActivityScenario.launch(launchIntent);
        scenario.onActivity(a -> a.setContentView(R.layout.admin_item_notification));
        return scenario;
    }

    @Test
    public void views_areDisplayed_test() {
        try (ActivityScenario<? extends Activity> scenario = launchWithLayout()) {
            onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.tvEventName)).check(matches(isDisplayed()));
            onView(withId(R.id.ivArrow)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void views_areCompletelyDisplayed_test() {
        // Extra safety: ensure key views are fully laid out within bounds
        try (ActivityScenario<? extends Activity> scenario = launchWithLayout()) {
            onView(withId(R.id.tvTitle)).check(matches(isCompletelyDisplayed()));
            onView(withId(R.id.tvEventName)).check(matches(isCompletelyDisplayed()));
            onView(withId(R.id.ivArrow)).check(matches(isCompletelyDisplayed()));
        }
    }
}
