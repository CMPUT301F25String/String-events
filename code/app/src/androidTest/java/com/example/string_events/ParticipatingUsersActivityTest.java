package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
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
 * UI tests for activity_participating_users.xml
 * - Only touches androidTest code; no manifest/app code changes.
 * - Verifies header, list, action buttons, and that btnDeleteUser is GONE.
 */
@RunWith(AndroidJUnit4.class)
public class ParticipatingUsersActivityTest {

    /** Launch launcher activity and set our layout as content. Also add no-op listeners. */
    private static Intent makeIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        return new Intent(ctx, ParticipatingUsersActivity.class)
                .putExtra("fromTest", true)
                .putExtra("eventId", "test-event")
                .putExtra("organizerId", "org-123");
    }

    @Test
    public void views_areDisplayed_test() {
        try (ActivityScenario<ParticipatingUsersActivity> sc =
                     ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
            onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.tvTitle)).check(matches(withText("Participating Users")));

            onView(withId(R.id.listParticipating)).check(matches(isDisplayed()));

            onView(withId(R.id.btnSendParticipating)).check(matches(isDisplayed()));
            onView(withId(R.id.btnExportParticipating)).check(matches(isDisplayed()));

            onView(withId(R.id.btnDeleteUser))
                    .check(matches(withEffectiveVisibility(Visibility.GONE)));
        }
    }

    @Test
    public void buttons_areClickable_noCrash_test() {
        try (ActivityScenario<ParticipatingUsersActivity> sc =
                     ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.btnExportParticipating)).check(matches(isClickable())).perform(click());
            onView(withId(R.id.btnSendParticipating)).check(matches(isClickable())).perform(click());
            onView(withId(R.id.btnBack)).check(matches(isClickable())).perform(click());
        }
    }
}
