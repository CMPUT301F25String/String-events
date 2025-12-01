package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
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
 * UI tests for item_notification_message.xml
 * - Only changes test code.
 * - Launches the app's launcher Activity and swaps its content view to the layout under test.
 * - Verifies visibility and default texts from XML.
 */
@RunWith(AndroidJUnit4.class)
public class ItemNotificationMessageLayoutTest {

    /** Launch launcher activity and set our layout as content. */
    private ActivityScenario<? extends Activity> launchWithLayout() {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent launchIntent = ctx.getPackageManager()
                .getLaunchIntentForPackage(ctx.getPackageName());
        if (launchIntent == null) {
            // Fallback to a generic MAIN intent if needed
            launchIntent = new Intent(Intent.ACTION_MAIN);
        }
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ActivityScenario<? extends Activity> scenario = ActivityScenario.launch(launchIntent);
        scenario.onActivity(a -> a.setContentView(R.layout.item_notification_message));
        return scenario;
    }

    @Test
    public void views_areDisplayed_test() {
        try (ActivityScenario<? extends Activity> scenario = launchWithLayout()) {
            // All key views should be visible
            onView(withId(R.id.notification_item)).check(matches(isDisplayed()));
            onView(withId(R.id.imgStatus)).check(matches(isDisplayed()));
            onView(withId(R.id.imgThumb)).check(matches(isDisplayed()));
            onView(withId(R.id.imgOpen)).check(matches(isDisplayed()));
            onView(withId(R.id.tvMessage)).check(matches(isDisplayed()));
            onView(withId(R.id.tvEventName)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void default_texts_areCorrect_test() {
        try (ActivityScenario<? extends Activity> scenario = launchWithLayout()) {
            // Assert default hard-coded texts defined in XML
            onView(withId(R.id.tvMessage)).check(matches(withText("Message Regarding")));
            onView(withId(R.id.tvEventName)).check(matches(withText("Event Name")));
        }
    }
}

