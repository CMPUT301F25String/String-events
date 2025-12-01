package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.pm.ActivityInfo;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI smoke tests for Admin All User Profile screen (admin_all_userprofile.xml).
 * - No scrollTo() to avoid animation-related PerformException.
 * - We "brute-force" swipe the root to bring targets into view.
 * - Only checks visibility/enabled; avoids business side effects.
 */
@RunWith(AndroidJUnit4.class)
public class AdminAllUserProfileActivityTest {

    @Rule
    public ActivityScenarioRule<AdminAllUserProfileActivity> rule =
            new ActivityScenarioRule<>(AdminAllUserProfileActivity.class);

    // ---------- Helpers (no scrollTo) ----------

    /** Swipe the root up a few times to bring lower content into viewport. */
    private void swipeRootUpTimes(int times) {
        for (int i = 0; i < times; i++) {
            onView(withId(android.R.id.content)).perform(swipeUp());
        }
    }

    /** Swipe the root down a few times to bring upper content back into viewport. */
    private void swipeRootDownTimes(int times) {
        for (int i = 0; i < times; i++) {
            onView(withId(android.R.id.content)).perform(swipeDown());
        }
    }

    // ---------- Tests ----------

    /** Header (yellow bar background, back button, title) is visible. */
    @Test
    public void header_isVisible() {
        onView(withId(R.id.header_bg)).check(matches(isDisplayed()));
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_title)).check(matches(isDisplayed()));
    }

    /** Photo card and inner image are visible. */
    @Test
    public void photoCard_isVisible() {
        onView(withId(R.id.cv_photo_container)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_photo)).check(matches(isDisplayed()));
    }

    /** Info section fields (name, email, password) are visible after swiping down/up as needed. */
    @Test
    public void infoFields_areVisible() {
        // Bring the info container roughly to the middle/bottom of the screen.
        swipeRootUpTimes(3);
        onView(withId(R.id.info_container)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_name)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_email)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_password)).check(matches(isDisplayed()));

        // Move back up a bit so other tests are not affected by extreme scroll.
        swipeRootDownTimes(1);
    }

    /** Delete button is visible and enabled without using scrollTo(). */
    @Test
    public void deleteButton_visible_andEnabled() {
        // The delete button is at the bottom; brute-force swipe to bottom.
        swipeRootUpTimes(5);
        onView(withId(R.id.btnDeleteProfile)).check(matches(isDisplayed()));
        onView(withId(R.id.btnDeleteProfile)).check(matches(isEnabled()));
        // Do not click to avoid destructive behavior.
    }

    /** Back button is clickable (navigation result not asserted). */
    @Test
    public void backButton_clickable() {
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        onView(withId(R.id.btnBack)).check(matches(isEnabled()));
        onView(withId(R.id.btnBack)).perform(click());
    }

    /** After rotation, core UI remains visible. */
    @Test
    public void orientationChange_persistsUi() {
        ActivityScenario<AdminAllUserProfileActivity> scenario = rule.getScenario();

        scenario.onActivity(a -> a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        onView(withId(R.id.profile_title)).check(matches(isDisplayed()));
        onView(withId(R.id.cv_photo_container)).check(matches(isDisplayed()));

        scenario.onActivity(a -> a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        onView(withId(R.id.profile_title)).check(matches(isDisplayed()));
        onView(withId(R.id.cv_photo_container)).check(matches(isDisplayed()));
    }
}
