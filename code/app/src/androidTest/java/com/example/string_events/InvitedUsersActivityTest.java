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
 * UI smoke tests for Invited Users screen (activity_invited_users.xml).
 * - Comments in English as requested.
 * - Uses safeScrollToId() to avoid relying on device-wide animation toggles.
 * - Verifies visibility/enabled state only; avoids triggering real business logic.
 */
@RunWith(AndroidJUnit4.class)
public class InvitedUsersActivityTest {

    @Rule
    public ActivityScenarioRule<InvitedUsersActivity> rule =
            new ActivityScenarioRule<>(InvitedUsersActivity.class);

    // ---------- Helper ----------

    /** Try scrollTo(); if it fails (e.g., due to animations), swipe the root and retry. */
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

    // ---------- Tests ----------

    /** Header is visible on first render. */
    @Test
    public void initialRender_headerVisible() {
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
    }

    /** List and bottom action button are visible/reachable. */
    @Test
    public void initialRender_listAndButtonVisible() {
        onView(withId(R.id.listInvited)).check(matches(isDisplayed()));
        safeScrollToId(R.id.btnSendInvited);
        onView(withId(R.id.btnSendInvited)).check(matches(isDisplayed()));
    }

    /** Button is enabled (no side-effect assertions). */
    @Test
    public void sendButton_enabled_noBusinessSideEffects() {
        safeScrollToId(R.id.btnSendInvited);
        onView(withId(R.id.btnSendInvited)).check(matches(isEnabled()));
    }

    /** Back button is clickable; we do not assert navigation result. */
    @Test
    public void backButton_clickable() {
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        onView(withId(R.id.btnBack)).perform(click());
    }

    /** After rotation, core views remain reachable and visible. */
    @Test
    public void orientationChange_persistsUi() {
        ActivityScenario<InvitedUsersActivity> scenario = rule.getScenario();

        scenario.onActivity(a -> a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
        safeScrollToId(R.id.btnSendInvited);
        onView(withId(R.id.btnSendInvited)).check(matches(isDisplayed()));

        scenario.onActivity(a -> a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.listInvited)).check(matches(isDisplayed()));
    }
}

