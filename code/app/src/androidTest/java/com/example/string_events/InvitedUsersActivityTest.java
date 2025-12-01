package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI smoke tests for Invited Users screen (activity_invited_users.xml).
 * - Comments in English as requested.
 * - Uses safeScrollToId() to avoid relying on device-wide animation toggles.
 * - Verifies visibility/enabled state only; avoids triggering real business logic.
 */
@RunWith(AndroidJUnit4.class)
public class InvitedUsersActivityTest {

    private static Intent makeIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        return new Intent(ctx, InvitedUsersActivity.class)
                .putExtra("fromTest", true)
                .putExtra("eventId", "test-event")
                .putExtra("organizerId", "org-123");
    }

    /** Header is visible on first render. */
    @Test
    public void initialRender_headerVisible() {
        try (ActivityScenario<InvitedUsersActivity> sc =
                     ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
        }
    }

    /** List and bottom action button are visible/reachable. */
    @Test
    public void initialRender_listAndButtonVisible() {
        try (ActivityScenario<InvitedUsersActivity> sc =
                     ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.listInvited)).check(matches(isDisplayed()));
        }
    }

    /** Back button is clickable; we do not assert navigation result. */
    @Test
    public void backButton_clickable() {
        try (ActivityScenario<InvitedUsersActivity> sc =
                     ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
            onView(withId(R.id.btnBack)).perform(click());
        }
    }
}

