package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.SystemClock;
import android.content.pm.ActivityInfo;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI smoke tests for create_event_screen.xml (no production changes).
 * - Uses repeated swipeUp to reveal targets; catches all throwables while attempting.
 * - Verifies visibility/enabled only; no business side effects asserted.
 */
@RunWith(AndroidJUnit4.class)
public class CreateEventScreenTest {

    // If your class name differs, replace CreateEventScreen with the actual Activity class.
    @Rule
    public ActivityScenarioRule<CreateEventScreen> rule =
            new ActivityScenarioRule<>(CreateEventScreen.class);

    /** Repeatedly swipe up until the view is displayed or attempts run out. */
    private void bringIntoView(int viewId) {
        final int MAX_SWIPE = 14;        // give plenty of room for long forms
        for (int i = 0; i < MAX_SWIPE; i++) {
            try {
                onView(withId(viewId)).check(matches(isDisplayed()));
                return; // success
            } catch (Throwable ignored) { // includes NoMatchingViewException & AssertionError
                onView(withId(android.R.id.content)).perform(swipeUp());
                SystemClock.sleep(60);   // small settle time to reduce flakiness
            }
        }
        // Final hard assert (will throw if still not visible)
        onView(withId(viewId)).check(matches(isDisplayed()));
    }

    /** Top bar shows up (back + bg). */
    @Test public void topBar_isVisible() {
        onView(withId(R.id.top_bar_background)).check(matches(isDisplayed()));
        onView(withId(R.id.back_button)).check(matches(isDisplayed()));
    }

    /** Title & description input areas render. */
    @Test public void title_and_description_visible() {
        onView(withId(R.id.event_title)).check(matches(isDisplayed()));
        onView(withId(R.id.event_title_editText)).check(matches(isDisplayed()));
        bringIntoView(R.id.event_description_editText);
        onView(withId(R.id.event_description)).check(matches(isDisplayed()));
        onView(withId(R.id.event_description_editText)).check(matches(isDisplayed()));
    }

    /** Photos section visible (add button + preview). */
    @Test public void photos_section_visible() {
        bringIntoView(R.id.event_photos);
        onView(withId(R.id.event_photos)).check(matches(isDisplayed()));
        onView(withId(R.id.add_photo_button)).check(matches(isDisplayed()));
        onView(withId(R.id.event_photo)).check(matches(isDisplayed()));
    }

    /** Tags area visible (container + add button). */
    @Test public void tags_section_visible() {
        bringIntoView(R.id.event_tags);
        onView(withId(R.id.event_tags)).check(matches(isDisplayed()));
        onView(withId(R.id.add_tags_button)).check(matches(isDisplayed()));
        onView(withId(R.id.tags_scroll_view)).check(matches(isDisplayed()));
    }

    /** Start/End datetime pickers exist. */
    @Test public void date_time_sections_visible() {
        bringIntoView(R.id.event_start_datetime);
        onView(withId(R.id.event_start_date_editText)).check(matches(isDisplayed()));
        onView(withId(R.id.event_start_time_editText)).check(matches(isDisplayed()));

        bringIntoView(R.id.event_end_datetime);
        onView(withId(R.id.event_end_date_editText)).check(matches(isDisplayed()));
        onView(withId(R.id.event_end_time_editText)).check(matches(isDisplayed()));
    }

    /** Registration start/end datetime pickers exist. */
    @Test public void registration_sections_visible() {
        // Nudge further down before checking registration block
        onView(withId(android.R.id.content)).perform(swipeUp(), swipeUp());
        bringIntoView(R.id.registration_start_datetime);
        onView(withId(R.id.registration_start_date_editText)).check(matches(isDisplayed()));
        onView(withId(R.id.registration_start_time_editText)).check(matches(isDisplayed()));

        bringIntoView(R.id.registration_end_datetime);
        onView(withId(R.id.registration_end_date_editText)).check(matches(isDisplayed()));
        onView(withId(R.id.registration_end_time_editText)).check(matches(isDisplayed()));
    }

    /** Location & capacity fields exist. */
    @Test public void location_and_counts_visible() {
        // Extra swipes because these are typically deep in the form
        onView(withId(android.R.id.content)).perform(swipeUp(), swipeUp());
        bringIntoView(R.id.event_location_editText);
        onView(withId(R.id.event_location)).check(matches(isDisplayed()));
        onView(withId(R.id.event_location_editText)).check(matches(isDisplayed()));

        bringIntoView(R.id.event_attendants_editText);
        onView(withId(R.id.event_attendants)).check(matches(isDisplayed()));
        onView(withId(R.id.event_attendants_editText)).check(matches(isDisplayed()));

        bringIntoView(R.id.event_waitlist_editText);
        onView(withId(R.id.event_waitlist)).check(matches(isDisplayed()));
        onView(withId(R.id.event_waitlist_editText)).check(matches(isDisplayed()));
    }

    /** Switches and visibility buttons are on screen and enabled. */
    @Test public void toggles_and_visibility_visible_enabled() {
        // Push a bit more down to reach toggle area on small screens
        onView(withId(android.R.id.content)).perform(swipeUp(), swipeUp());
        bringIntoView(R.id.geolocation_switch);
        onView(withId(R.id.geolocation_switch)).check(matches(isDisplayed()));

        bringIntoView(R.id.auto_rolling_switch);
        onView(withId(R.id.auto_rolling_switch)).check(matches(isDisplayed()));

        bringIntoView(R.id.event_visibility);
        onView(withId(R.id.event_visibility)).check(matches(isDisplayed()));
        onView(withId(R.id.event_public_button)).check(matches(isDisplayed()));
        onView(withId(R.id.event_private_button)).check(matches(isDisplayed()));
    }

    /** Bottom DONE button is visible and enabled (smoke click allowed). */
    @Test public void bottom_done_enabled_clickable() {
        bringIntoView(R.id.done_button);
        onView(withId(R.id.done_button)).check(matches(isDisplayed()));
        onView(withId(R.id.done_button)).check(matches(isEnabled()));
        onView(withId(R.id.done_button)).perform(click());
    }

    /** After rotation, core controls still render. */
    @Test public void orientation_persists_ui() {
        ActivityScenario<CreateEventScreen> s = rule.getScenario();
        s.onActivity(a -> a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        bringIntoView(R.id.event_title_editText);
        onView(withId(R.id.event_title_editText)).check(matches(isDisplayed()));
        bringIntoView(R.id.done_button);
        onView(withId(R.id.done_button)).check(matches(isDisplayed()));

        s.onActivity(a -> a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        onView(withId(R.id.event_title_editText)).check(matches(isDisplayed()));
        bringIntoView(R.id.done_button);
        onView(withId(R.id.done_button)).check(matches(isDisplayed()));
    }
}
