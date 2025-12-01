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
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
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

    @Rule
    public ActivityScenarioRule<NotificationScreen> scenario = new ActivityScenarioRule<>(NotificationScreen.class);


    @Test
    public void views_areDisplayed_test() {
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(withId(R.id.notifications_textView)).check(matches(isDisplayed()));
        onView(withId(R.id.notifications_recyclerView)).check(matches(isDisplayed()));
        onView(withId(R.id.bottomBar)).check(matches(isDisplayed()));
        onView(withId(R.id.btnHome)).check(matches(isDisplayed()));
        onView(withId(R.id.btnCamera)).check(matches(isDisplayed()));
        onView(withId(R.id.btnNotification)).check(matches(isDisplayed()));
        onView(withId(R.id.btnProfile)).check(matches(isDisplayed()));
    }

    @Test
    public void texts_and_accessibility_areCorrect_test() {
        // Helper text from XML
        onView(withId(R.id.notifications_textView))
                .check(matches(withText("Click on a notification to expand")));
        // Bottom bar a11y labels from XML
        onView(withId(R.id.btnHome)).check(matches(withContentDescription("Home")));
        onView(withId(R.id.btnCamera)).check(matches(withContentDescription("Camera")));
        onView(withId(R.id.btnNotification)).check(matches(withContentDescription("Notifications")));
        onView(withId(R.id.btnProfile)).check(matches(withContentDescription("Profile")));
    }

    @Test
    public void bottom_buttons_areClickable_noCrash_test() {
        onView(withId(R.id.btnHome)).check(matches(isClickable())).perform(click());
        onView(withId(R.id.btnCamera)).check(matches(isClickable())).perform(click());
        onView(withId(R.id.btnNotification)).check(matches(isClickable())).perform(click());
        onView(withId(R.id.btnProfile)).check(matches(isClickable())).perform(click());
    }
}
