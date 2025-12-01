package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
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
 * UI tests for item_event_organizer.xml with NO app/manifest changes.
 * Strategy:
 * 1) Launch the app's existing launcher Activity via PackageManager.
 * 2) Immediately swap its contentView to item_event_organizer.
 * 3) Run Espresso checks on the views.
 */
@RunWith(AndroidJUnit4.class)
public class ItemEventOrganizerLayoutTest {

    /** Launches the app's launcher Activity and sets our layout as content. */
    private static Intent hostIntentFor(int layoutResId) {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent i = new Intent(ctx, UiHostActivity.class);
        i.putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, layoutResId);
        return i;
    }

    @Test
    public void views_areDisplayed_test() {
        try (ActivityScenario<UiHostActivity> sc =
                     ActivityScenario.launch(hostIntentFor(R.layout.item_event_organizer))) {
            onView(withId(R.id.imgCover)).check(matches(isDisplayed()));
            onView(withId(R.id.chipStatus)).check(matches(isDisplayed()));
            onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.divider)).check(matches(isDisplayed()));
            onView(withId(R.id.ic_time)).check(matches(isDisplayed()));
            onView(withId(R.id.tvTime)).check(matches(isDisplayed()));
            onView(withId(R.id.ic_location)).check(matches(isDisplayed()));
            onView(withId(R.id.tvLocation)).check(matches(isDisplayed()));
            onView(withId(R.id.tvOrganizer)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void layout_inflates_without_crash_test() {
        try (ActivityScenario<UiHostActivity> sc =
                     ActivityScenario.launch(hostIntentFor(R.layout.item_event_organizer))) {
            onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
        }
    }
}
