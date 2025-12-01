package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.pm.ActivityInfo;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI smoke tests for NotificationMessageDetailActivity (activity_notification_message_detail.xml).
 * - Comments are in English as requested.
 * - Uses safeScrollToId() to avoid depending on device animation settings.
 * - Only verifies visibility/enabled state; avoids triggering business logic.
 */
@RunWith(AndroidJUnit4.class)
public class NotificationMessageDetailActivityTest {

    @Rule
    public ActivityScenarioRule<NotificationMessageDetailActivity> rule =
            new ActivityScenarioRule<>(NotificationMessageDetailActivity.class);

    /** Try scrollTo(); if it fails (e.g., animations), swipe the root and retry. */
    private void safeScrollToId(int viewId) {
        final int MAX_ATTEMPTS = 6;
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            try {
                onView(withId(viewId)).perform(ViewActions.scrollTo());
                return; // success
            } catch (PerformException e) {
                onView(withId(android.R.id.content)).perform(swipeUp());
            } catch (Exception e) {
                onView(withId(android.R.id.content)).perform(swipeUp());
            }
        }
        onView(withId(viewId)).perform(ViewActions.scrollTo());
    }

    /** Header (back button + title) is visible on first render. */
    @Test
    public void header_visible() {
        onView(withId(R.id.header_container)).check(matches(isDisplayed()));
        onView(withId(R.id.back_button)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
    }

    /** Event summary card is reachable and key contents are visible. */
    @Test
    public void eventSummary_visible() {
        safeScrollToId(R.id.ivEventImage);
        onView(withId(R.id.ivEventImage)).check(matches(isDisplayed()));

        onView(withId(R.id.tvEventTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.tvLocation)).check(matches(isDisplayed()));
        onView(withId(R.id.tvDateLine)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTimeLine)).check(matches(isDisplayed()));
    }

    /** Message section (header + content) is reachable and visible. */
    @Test
    public void messageSection_visible() {
        safeScrollToId(R.id.tvMessageHeader);
        onView(withId(R.id.tvMessageHeader)).check(matches(isDisplayed()));

        safeScrollToId(R.id.tvMessageContent);
        onView(withId(R.id.tvMessageContent)).check(matches(isDisplayed()));
    }

    /** Back button is clickable (no navigation assertion). */
    @Test
    public void backButton_clickable() {
        onView(withId(R.id.back_button)).check(matches(isDisplayed()));
        onView(withId(R.id.back_button)).check(matches(isEnabled()));
        onView(withId(R.id.back_button)).perform(click());
    }

    /** After rotation, core views remain reachable and visible. */
    @Test
    public void orientationChange_persistsUi() {
        ActivityScenario<NotificationMessageDetailActivity> scenario = rule.getScenario();

        scenario.onActivity(a -> a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
        safeScrollToId(R.id.tvMessageContent);
        onView(withId(R.id.tvMessageContent)).check(matches(isDisplayed()));

        scenario.onActivity(a -> a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
        safeScrollToId(R.id.ivEventImage);
        onView(withId(R.id.ivEventImage)).check(matches(isDisplayed()));
    }
}
